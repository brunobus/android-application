package hr.bpervan.novaeva.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.webkit.WebSettings.LayoutAlgorithm
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.nostra13.universalimageloader.core.ImageLoader
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.ContentData
import hr.bpervan.novaeva.services.BackgroundPlayerService
import hr.bpervan.novaeva.services.NovaEvaService
import hr.bpervan.novaeva.utilities.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_vijest.*
import kotlinx.android.synthetic.main.vijest_fake_action_bar.view.*

class VijestActivity : Activity(), View.OnClickListener, MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {
    /** ------------------------------------------FIELDS------------------------------------------ */

    /**
     * Used to store Font size
     */
    private lateinit var prefs: SharedPreferences

    /**
     * Pure 'Vijest' data
     */
    private var contentId: Long = 0

    private var colourSet: Int = 0

    /**
     * Google analytics API
     */
    private lateinit var mGaTracker: Tracker

    /**
     * Database handler for bookmarks, resource handler for category colours
     */
    private lateinit var dbHandler: BookmarksDBHandlerV2

    /**
     * Image loader
     */
    private lateinit var imageLoader: ImageLoader
    private lateinit var imageLoaderConfigurator: ImageLoaderConfigurator

    /**
     * Retrofit
     */
    private var thisContentData: ContentData? = null

    /**
     * Exoplayer
     */
    private var exoPlayer: SimpleExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dbHandler = BookmarksDBHandlerV2(this)
        contentId = intent.getLongExtra("contentId", -1)
        colourSet = intent.getIntExtra("colourSet", Constants.CAT_PROPOVJEDI)

        startService(Intent(this, BackgroundPlayerService::class.java))

        prefs = getSharedPreferences("hr.bpervan.novaeva", Context.MODE_PRIVATE)

        mGaTracker = (application as NovaEvaApp).getTracker(NovaEvaApp.TrackerName.APP_TRACKER)
        mGaTracker.send(
                HitBuilders.EventBuilder()
                        .setCategory("Vijesti")
                        .setAction("OtvorenaVijest")
                        .setLabel(contentId.toString() + "")
                        .build()
        )

        if (ConnectionChecker.hasConnection(this)) {
            getContentData(contentId)
        } else {
            Toast.makeText(this, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show()
            return
        }

        imageLoaderConfigurator = ImageLoaderConfigurator(this)
        imageLoader = ImageLoader.getInstance()
        if (!imageLoader.isInited) {
            ImageLoaderConfigurator(this).doInit()
        }
        initUI()
    }

    private fun streamAudio(audioUri: String) {
        val RENDER_COUNT = 1
        val minBufferMs = 1000
        val minRebufferMs = 5000

        val streamingUri = Uri.parse(audioUri)

        val bandwidthMeter = DefaultBandwidthMeter()
        val dataSourceFactory = DefaultDataSourceFactory(
                this,
                Util.getUserAgent(this, resources.getString(R.string.app_name)),
                bandwidthMeter)
        val extractorsFactory = DefaultExtractorsFactory()
        val mediaSource = ExtractorMediaSource(streamingUri, dataSourceFactory, extractorsFactory, 1, Handler(), null, audioUri)

        exoPlayer!!.prepare(mediaSource)
    }

    private fun createPlayer() {
        val bandwidthMeter = DefaultBandwidthMeter()
        val factory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(factory)

        this.exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector)

        player_view?.requestFocus()
        player_view?.player = exoPlayer
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        initUI()

        /** Test and set various attachments */
        setAttachments()

        thisContentData?.let { contentData ->
            if (contentData.hasImage()) {
                if (imageLoader.isInited && headerImage != null) {
                    imageLoader.displayImage(contentData.image!![0].size640, headerImage, imageLoaderConfigurator.doConfig(false))
                }
            } else {
                val url = prefs.getString("hr.bpervan.novaeva.categoryheader." + contentData.cid, null)
                if (url != null && headerImage != null && imageLoader.isInited) {
                    imageLoader.displayImage(url, headerImage, imageLoaderConfigurator.doConfig(true))
                }
            }
            vijestWebView.reload()
            vijestWebView.loadDataWithBaseURL(null, contentData.tekst, "text/html", "utf-8", null)
        }
    }

    /**
     * Initializes User interface, makes connections and references to
     * widgets on User interface, sets category specified colour
     */
    private fun initUI() {
        setContentView(R.layout.activity_vijest)

        /** Basic data  */
        NovaEvaApp.openSansBold?.let {
            tvNaslov.typeface = it
        }

        /** Lets try displaying news using WebView  */
        vijestWebView.reload()
        if (thisContentData != null) {
            vijestWebView.loadDataWithBaseURL(null, thisContentData!!.tekst, "text/html", "UTF-8", null)
        }

        vijestWebView.isFocusable = false
        vijestWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.reload()
                view.loadDataWithBaseURL(null, thisContentData!!.tekst, "text/html", "utf-8", null)
                return false
            }
        }

        vijestWebView.settings.layoutAlgorithm = LayoutAlgorithm.SINGLE_COLUMN

        /* Prevent C/P */
        vijestWebView.setOnLongClickListener { true }
        vijestWebView.isLongClickable = false

        /** Is this 'Duhovni poziv' or 'Odgovori' category?  */
        if (intent.getLongExtra("directoryId", Constants.CAT_PROPOVJEDI.toLong()) == Constants.CAT_POZIV.toLong()) {
            setQuestionButton()
        }

        seekArc.setOnSeekBarChangeListener(this)

        NovaEvaApp.openSansRegular?.let {
            tvDuration.typeface = it
            tvElapsed.typeface = it
        }

        btnTextPlus.setOnClickListener(this)

        fakeActionBar.btnHome.setOnClickListener(this)
        fakeActionBar.btnFace.setOnClickListener(this)
        fakeActionBar.btnMail.setOnClickListener(this)
        fakeActionBar.btnBookmark.setOnClickListener(this)
        fakeActionBar.btnSearch.setOnClickListener(this)
        fakeActionBar.btnBack.setOnClickListener(this)

        /** Set category name and set text size */
        vijestWebView.settings.defaultFontSize = prefs.getInt("hr.bpervan.novaeva.velicinateksta", 14)

        this.setCategoryTypeColour()
        /*if(mPlayer != null){
			mp3Player.setVisibility(View.VISIBLE);
			btnPlay.setOnClickListener(VijestActivity.this);
			btnPause.setOnClickListener(VijestActivity.this);

			seekArc.setMax(mPlayer.getDuration());

            int seconds = (int) (mPlayer.getDuration() / 1000) % 60 ;
            int minutes = (int) ((mPlayer.getDuration() / (1000 * 60)) % 60);

            if(mPlayer.isPlaying()){
            	seekArc.setProgress(mPlayer.getCurrentPosition());
				btnPlay.setVisibility(View.INVISIBLE);
				btnPause.setVisibility(View.VISIBLE);
            }

            tvDuration.setText(String.format("%02d:%02d", minutes, seconds));

            tvElapsed.setText(String.format("%02d:%02d",
            		(int) (mPlayer.getCurrentPosition() / 1000) % 60,
            		(int) ((mPlayer.getCurrentPosition() / (1000 * 60)) % 60)));
		}*/

        if (dbHandler.nidExists(contentId.toInt())) //todo accept long
            fakeActionBar.btnBookmark.setImageResource(R.drawable.vijest_button_bookmarked)

        window.decorView.setBackgroundColor(this.resources.getColor(android.R.color.background_light))
    }

    var contentDataDisposable: Disposable? = null

    private fun getContentData(contentId: Long) {
        Log.d("getContentData", "getting content data for contentId: " + contentId)
        contentDataDisposable?.dispose()
        contentDataDisposable = (NovaEvaService.instance
                .getContentData(contentId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ contentData ->
                    if (contentData.audio != null) {
                        imgMp3.setImageResource(R.drawable.vijest_ind_mp3_active)

                        createPlayer()
                        streamAudio(contentData.audio)
                    }
                    if (contentData.prilozi != null) {
                        imgText.setImageResource(R.drawable.vijest_ind_txt_active)
                        imgText.setOnClickListener(this@VijestActivity)
                    }
                    if (contentData.youtube != null) {
                        imgLink.setImageResource(R.drawable.vijest_ind_www_active)
                        imgLink.setOnClickListener(this@VijestActivity)
                    }

                    if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        if (contentData.image != null) {
                            if (imageLoader.isInited) {
                                imageLoader.displayImage(contentData.image[0].size640, headerImage, imageLoaderConfigurator.doConfig(false))
                            }
                        } else {
                            val url = prefs.getString("hr.bpervan.novaeva.categoryheader." + contentData.cid, null)
                            if (url != null) {
                                if (imageLoader.isInited) {
                                    imageLoader.displayImage(url, headerImage, imageLoaderConfigurator.doConfig(true))
                                }
                            }
                        }
                    }
                    if (contentData.cid == Constants.CAT_POZIV) {
                        setQuestionButton()
                    }

                    tvNaslov.text = contentData.naslov
                    vijestWebView.reload()
                    vijestWebView.loadDataWithBaseURL(null, contentData.tekst, "text/html", "UTF-8", null)

                    this@VijestActivity.thisContentData = contentData

                }) { t -> Log.e("vijestiError", t.message, t) })
    }

    override fun onResume() {
        //LocalBroadcastManager.getInstance(this).registerReceiver(serviceMessageReceiver, new IntentFilter(BackgroundPlayerService.INTENT_CLASS));
        super.onResume()
    }

    override fun onPause() {
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceMessageReceiver);
        super.onPause()


        /*if(mPlayer != null){
			if(mPlayer.isPlaying()){
				mPlayer.stop();
			}
			mPlayer.release();
			seekArc.removeCallbacks(onEverySecond);
		}
		mPlayer = null;*/
    }

    override fun onDestroy() {
        super.onDestroy()

        contentDataDisposable?.dispose()
    }

    public override fun onStart() {
        super.onStart()
        GoogleAnalytics.getInstance(this).reportActivityStart(this)
    }

    public override fun onStop() {
        super.onStop()
        GoogleAnalytics.getInstance(this).reportActivityStop(this)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    private fun setCategoryTypeColour() {
        val resources = ResourceHandler.getVijestResource(colourSet)

        fakeActionBar.setBackgroundResource(resources[0])
        imgNaslovnaTraka.setImageResource(resources[1])
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnPoziv -> {
                val text: String
                val mail = arrayOfNulls<String>(1)
                val i: Intent
                when (intent.getIntExtra("kategorija", 11)) {
                    Constants.CAT_POZIV -> {
                        text = "Hvaljen Isus i Marija, javljam Vam se jer razmišljam o duhovnom pozivu."
                        mail[0] = "duhovnipoziv@gmail.com"
                        i = Intent(Intent.ACTION_SEND)
                        i.type = "message/rfc822"
                        i.putExtra(Intent.EXTRA_SUBJECT, "Duhovni poziv")
                        i.putExtra(Intent.EXTRA_TEXT, text)
                        i.putExtra(Intent.EXTRA_EMAIL, mail)
                        startActivity(Intent.createChooser(i, "Odaberite aplikaciju"))
                    }
                }
            }
            R.id.btnPlay -> {
                Log.d(TAG, "btnPlay")
                if (BackgroundPlayerService.isRunning) {
                    val messageIntent = Intent(this@VijestActivity, BackgroundPlayerService::class.java)
                    messageIntent.putExtra(BackgroundPlayerService.KEY_DIRECTIVE, BackgroundPlayerService.DIRECTIVE_SET_SOURCE_PLAY)
                    messageIntent.putExtra(BackgroundPlayerService.KEY_PATH, thisContentData!!.audio)
                    messageIntent.putExtra(BackgroundPlayerService.KEY_TITLE, thisContentData!!.naslov)
                    startService(messageIntent)
                    btnPlay.visibility = View.INVISIBLE
                    btnPause.visibility = View.VISIBLE
                    btnPause.isEnabled = false
                    Log.d(TAG, "Sent MSG_SET_SOURCE_AND_PLAY")
                }
            }
            R.id.btnPause -> {
                Log.d(TAG, "btnPlay")
                val pauseIntent = Intent(this@VijestActivity, BackgroundPlayerService::class.java)
                pauseIntent.putExtra(BackgroundPlayerService.KEY_DIRECTIVE, BackgroundPlayerService.DIRECTIVE_PAUSE)
                startService(pauseIntent)
                /*if(mPlayer.isPlaying()){
				Log.d(TAG, "btnPlay");
				mPlayer.pause();
				seekArc.removeCallbacks(onEverySecond);
				btnPause.setVisibility(View.INVISIBLE);
				btnPlay.setVisibility(View.VISIBLE);
			}*/
                btnPause.visibility = View.INVISIBLE
                btnPlay.visibility = View.VISIBLE
                btnPlay.isEnabled = false
            }
            R.id.btnHome -> NovaEvaApp.goHome(this)
            R.id.btnSearch ->
                if (ConnectionChecker.hasConnection(this))
                    showSearchPopup()
            R.id.btnBookmark ->
                if (dbHandler.nidExists(contentId.toInt())) { //todo accept long
                    dbHandler.deleteVijest(contentId.toInt()) //todo accept long
                    fakeActionBar.btnBookmark.setImageResource(R.drawable.vijest_button_bookmark)
                } else {
                    dbHandler.insertArticle(thisContentData)
                    fakeActionBar.btnBookmark.setImageResource(R.drawable.vijest_button_bookmarked)
                }
            R.id.btnFace -> {
                val temp = "http://novaeva.com/node/" + contentId
                val faceIntent = Intent(Intent.ACTION_SEND)
                faceIntent.type = "text/plain"
                faceIntent.putExtra(Intent.EXTRA_TEXT, temp)
                startActivity(Intent.createChooser(faceIntent, "Facebook"))
            }
            R.id.btnMail -> {
                val temp2 = "http://novaeva.com/node/" + contentId
                val mailIntent = Intent(Intent.ACTION_SEND)
                mailIntent.type = "message/rfc822"
                mailIntent.putExtra(Intent.EXTRA_SUBJECT, thisContentData!!.naslov)
                mailIntent.putExtra(Intent.EXTRA_TEXT, temp2)
                startActivity(Intent.createChooser(mailIntent, "Odaberite aplikaciju"))
            }
            R.id.btnTextPlus -> {
                var mCurrentSize = prefs.getInt("hr.bpervan.novaeva.velicinateksta", 14)
                mCurrentSize += 2
                if (mCurrentSize >= 28) {
                    mCurrentSize = 12
                }

                prefs.edit().putInt("hr.bpervan.novaeva.velicinateksta", mCurrentSize).commit()
                vijestWebView.settings.defaultFontSize = mCurrentSize
            }
            R.id.btnBack -> this@VijestActivity.onBackPressed()
            R.id.imgLink -> if (thisContentData!!.youtube != null) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(thisContentData!!.youtube)))
            }
            R.id.imgText -> if (thisContentData!!.prilozi != null) {
                startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW, Uri.parse(thisContentData!!.prilozi!![0].url)),
                        "Otvaranje dokumenta " + thisContentData!!.prilozi!![0].naziv))
            }
        }
    }

    private fun showSearchPopup() {
        val search = AlertDialog.Builder(this)
        search.setTitle("Pretraga")
        val et = EditText(this)
        search.setView(et)
        search.setPositiveButton("Pretraži") { dialog, which ->
            val search = et.text.toString()
            NovaEvaApp.goSearch(search, this@VijestActivity)
        }
        search.setNegativeButton("Odustani") { dialog, whichButton -> }
        search.show()
    }

    private fun setQuestionButton() {
        btnPoziv.visibility = View.VISIBLE
        btnPoziv.setOnClickListener(this@VijestActivity)
    }

    private fun setAttachments() {
        thisContentData?.let {
            if (it.audio != null) {
                imgMp3.setImageResource(R.drawable.vijest_ind_mp3_active)
            }
            if (it.youtube != null) {
                imgLink.setImageResource(R.drawable.vijest_ind_www_active)
                imgLink.setOnClickListener(this)
            }
            if (it.prilozi != null) {
                imgText.setImageResource(R.drawable.vijest_ind_txt_active)
                imgText.setOnClickListener(this)
            }
        }
    }

    override fun onProgressChanged(seekArc: SeekBar, progress: Int,
                                   fromUser: Boolean) {
        /*if(mPlayer != null && fromUser){
			mPlayer.seekTo(progress);
		}*/
    }

    override fun onStartTrackingTouch(seekArc: SeekBar) {}

    override fun onStopTrackingTouch(seekArc: SeekBar) {}


    override fun onCompletion(mp: MediaPlayer) {
        Log.d(TAG, "onCompletion")
        btnPlay.visibility = View.VISIBLE
        btnPause.visibility = View.INVISIBLE

        seekArc.progress = 0
        tvElapsed.text = "00:00"
        //mPlayer.seekTo(0);
    }

    /*private Runnable onEverySecond = new Runnable(){
		@Override
		public void run() {
			if(mPlayer != null){
	            int seconds = (int) (mPlayer.getCurrentPosition() / 1000) % 60 ;
	            int minutes = (int) ((mPlayer.getCurrentPosition() / (1000*60)) % 60);

	            tvElapsed.setText(String.format("%02d:%02d", minutes, seconds));
				seekArc.setProgress(mPlayer.getCurrentPosition());
	        }
	        seekArc.postDelayed(this, 1000);
		}
	};*/

    private fun showErrorPopup() {
        val error = AlertDialog.Builder(this)
        error.setTitle("Greška")

        val tv = TextView(this)
        tv.text = "Greška pri dohvaćanju podataka sa poslužitelja"

        NovaEvaApp.openSansRegular?.let {
            tv.typeface = it
        }
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        error.setView(tv)

        error.setPositiveButton("Pokušaj ponovno") { dialog, which ->
            getContentData(contentId)
        }
        error.setNegativeButton("Povratak") { dialog, whichButton ->
            NovaEvaApp.goHome(this)
        }
        error.show()
    }

    companion object {
        private val TAG = "VijestActivity"
    }
}