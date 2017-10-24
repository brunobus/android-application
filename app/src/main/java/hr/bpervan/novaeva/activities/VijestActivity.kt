package hr.bpervan.novaeva.activities

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.webkit.WebSettings.LayoutAlgorithm
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
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
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaContentDTO
import hr.bpervan.novaeva.model.EvaCategory
import hr.bpervan.novaeva.model.EvaContentInfo
import hr.bpervan.novaeva.services.BackgroundPlayerService
import hr.bpervan.novaeva.services.NovaEvaService
import hr.bpervan.novaeva.storage.EvaBookmarkDbAdapter
import hr.bpervan.novaeva.storage.RealmConfigProvider
import hr.bpervan.novaeva.utilities.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_vijest.*
import kotlinx.android.synthetic.main.vijest_fake_action_bar.view.*

class VijestActivity : EvaBaseActivity(), View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    /** ------------------------------------------FIELDS------------------------------------------ */

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
     * Retrofit
     */
    private var thisContent: EvaContentDTO? = null

    /**
     * Exoplayer
     */
    private var exoPlayer: SimpleExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inState = savedInstanceState ?: intent.extras
        contentId = inState.getLong("contentId", -1)
        colourSet = inState.getInt("colourSet", EvaCategory.PROPOVIJEDI.id)

        startService(Intent(this, BackgroundPlayerService::class.java)) //todo

        mGaTracker = (application as NovaEvaApp).getTracker(NovaEvaApp.TrackerName.APP_TRACKER)
        mGaTracker.send(
                HitBuilders.EventBuilder()
                        .setCategory("Vijesti")
                        .setAction("OtvorenaVijest")
                        .setLabel(contentId.toString() + "")
                        .build()
        )

        initUI()

        if (ConnectionChecker.hasConnection(this)) {
            getContentData(contentId)
        } else {
            Toast.makeText(this, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show()
            return
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong("contentId", contentId)
        outState.putInt("colourSet", colourSet)

        super.onSaveInstanceState(outState)
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
        this?.clearFindViewByIdCache() //because of a bug in library, you must use "this?." otherwise npe is thrown !!!
        super.onConfigurationChanged(newConfig)
        initUI()
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

        if (thisContent == null) {
            loadingCircle.visibility = View.VISIBLE
        } else {
            loadingCircle.visibility = View.GONE
        }

        /** Lets try displaying news using WebView  */

        thisContent?.let { contentData ->
            tvNaslov.text = contentData.title

            //if not null
            if (contentData.hasImage()) {
                if (headerImage != null) {
                    imageLoader.displayImage(contentData.images!![0].size640, headerImage, ImageLoaderConfigurator.createDefaultDisplayImageOptions(false))
                }
            } else {
                val url = prefs.getString("hr.bpervan.novaeva.categoryheader." + contentData.directoryId, null)
                if (url != null && headerImage != null) {
                    imageLoader.displayImage(url, headerImage, ImageLoaderConfigurator.createDefaultDisplayImageOptions(true))
                }
            }
            vijestWebView.reload()
            vijestWebView.loadDataWithBaseURL(null, contentData.text, "text/html", "utf-8", null)

            if (contentData.audio != null) {
                imgMp3.setImageResource(R.drawable.vijest_ind_mp3_active)
            }
            if (contentData.youtube != null) {
                imgLink.setImageResource(R.drawable.vijest_ind_www_active)
                imgLink.setOnClickListener(this)
            }
            if (contentData.attachments != null) {
                imgText.setImageResource(R.drawable.vijest_ind_txt_active)
                imgText.setOnClickListener(this)
            }
        }

        vijestWebView.isFocusable = false
        vijestWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.reload()
                view.loadDataWithBaseURL(null, thisContent!!.text, "text/html", "utf-8", null)
                return false
            }
        }

        vijestWebView.settings.layoutAlgorithm = LayoutAlgorithm.SINGLE_COLUMN

        /* Prevent C/P */
        vijestWebView.setOnLongClickListener { true }
        vijestWebView.isLongClickable = false

        /** Is this 'Duhovni poziv' or 'Odgovori' category?  */
        if (intent.getIntExtra("categoryId", EvaCategory.PROPOVIJEDI.id) == EvaCategory.POZIV.id) {
            setQuestionButton()
        }

        seekArc.setOnSeekBarChangeListener(this)

        NovaEvaApp.openSansRegular?.let {
            tvDuration.typeface = it
            tvElapsed.typeface = it
        }

        btnTextPlus.setOnClickListener(this)

        fakeActionBar.btnHome.setOnClickListener(this)
        fakeActionBar.btnShare.setOnClickListener(this)
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
        fakeActionBar.btnBookmark.setImageResource(R.drawable.action_button_bookmark)
        window.decorView.setBackgroundResource(android.R.color.background_light)
    }

    private var contentDataDisposable: Disposable? = null

    private fun getContentData(contentId: Long) {
        Log.d("getContentData", "getting content data for contentId: " + contentId)

        loadingCircle.visibility = View.VISIBLE

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
                    if (contentData.attachments != null) {
                        imgText.setImageResource(R.drawable.vijest_ind_txt_active)
                        imgText.setOnClickListener(this@VijestActivity)
                    }
                    if (contentData.youtube != null) {
                        imgLink.setImageResource(R.drawable.vijest_ind_www_active)
                        imgLink.setOnClickListener(this@VijestActivity)
                    }

                    if (contentData.hasImage()) {
                        if (headerImage != null) {
                            imageLoader.displayImage(contentData.images!![0].size640, headerImage, ImageLoaderConfigurator.createDefaultDisplayImageOptions(false))
                        }
                    } else {
                        val url = prefs.getString("hr.bpervan.novaeva.categoryheader." + contentData.directoryId, null)
                        if (url != null && headerImage != null) {
                            imageLoader.displayImage(url, headerImage, ImageLoaderConfigurator.createDefaultDisplayImageOptions(true))
                        }
                    }
                    if (contentData.directoryId == EvaCategory.POZIV.id.toLong()) {
                        setQuestionButton()
                    }

                    tvNaslov.text = contentData.title
                    loadingCircle.visibility = View.GONE
                    vijestWebView.reload()
                    vijestWebView.loadDataWithBaseURL(null, contentData.text, "text/html", "UTF-8", null)

                    thisContent = contentData

                }) { t ->
                    showErrorPopup(t) {
                        getContentData(contentId)
                    }
                })
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

    private fun setCategoryTypeColour() {
        fakeActionBar.setBackgroundResource(ResourceHandler.getFakeActionBarResourceId(colourSet))
        imgNaslovnaTraka.setImageResource(ResourceHandler.getContentTitleBarResourceId(colourSet))
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnPoziv -> {
                val text: String
                val mail = arrayOfNulls<String>(1)
                val i: Intent
                when (intent.getIntExtra("kategorija", 11)) {
                    EvaCategory.POZIV.id -> {
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
                    messageIntent.putExtra(BackgroundPlayerService.KEY_PATH, thisContent!!.audio)
                    messageIntent.putExtra(BackgroundPlayerService.KEY_TITLE, thisContent!!.title)
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
                if (thisContent != null) {
                    val contentData = thisContent!!

                    Realm.getInstance(RealmConfigProvider.bookmarksConfig).use { realm ->

                        //todo use db to fetch contentInfo
                        val evaContentInfo = EvaContentInfo(
                                contentId = contentData.contentId,
                                datetime = contentData.datetime,
                                title = contentData.title,
                                preview = "")
                        EvaBookmarkDbAdapter.storeBookmarkAsync(realm, evaContentInfo)
                    }
                    fakeActionBar.btnBookmark.setImageResource(R.drawable.action_button_bookmarked)
                } else {
                    //todo
                }
            R.id.btnShare -> {
                val faceIntent = Intent(Intent.ACTION_SEND)
                faceIntent.type = "text/plain"
                faceIntent.putExtra(Intent.EXTRA_TEXT, "http://novaeva.com/node/$contentId")
                startActivity(Intent.createChooser(faceIntent, "Share"))
            }
            R.id.btnMail -> {
                val mailIntent = Intent(Intent.ACTION_SEND)
                mailIntent.type = "message/rfc822"
                mailIntent.putExtra(Intent.EXTRA_SUBJECT, thisContent!!.title)
                mailIntent.putExtra(Intent.EXTRA_TEXT, "http://novaeva.com/node/$contentId")
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
            R.id.imgLink -> if (thisContent!!.youtube != null) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(thisContent!!.youtube)))
            }
            R.id.imgText -> if (thisContent!!.attachments != null) {
                startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW, Uri.parse(thisContent!!.attachments!![0].url)),
                        "Otvaranje dokumenta " + thisContent!!.attachments!![0].naziv))
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.move_left_in, R.anim.move_right_out)
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

    override fun onProgressChanged(seekArc: SeekBar, progress: Int,
                                   fromUser: Boolean) {
        /*if(mPlayer != null && fromUser){
			mPlayer.seekTo(progress);
		}*/
    }

    override fun onStartTrackingTouch(seekArc: SeekBar) {}

    override fun onStopTrackingTouch(seekArc: SeekBar) {}

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

    companion object {
        private val TAG = "VijestActivity"
    }
}