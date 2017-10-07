package hr.bpervan.novaeva.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Typeface
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
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
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
import hr.bpervan.novaeva.model.Article
import hr.bpervan.novaeva.services.BackgroundPlayerService
import hr.bpervan.novaeva.services.NovaEvaService
import hr.bpervan.novaeva.utilities.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_vijest.*

class VijestActivity : Activity(), View.OnClickListener, MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {
    /** ------------------------------------------FIELDS------------------------------------------ */


    internal var headerImage: ImageView? = null //todo vpriscan

    /**
     * Used to store Font size
     */
    private lateinit var prefs: SharedPreferences

    /**
     * Pure 'Vijest' data
     */
    private var nid: Int = 0
    //private Vijest ovaVijest;

    /**
     * Google analytics API
     */
    private lateinit var mGaTracker: Tracker

    /**
     * Database handler for bookmarks, resource handler for category colours
     */
    private lateinit var dbHandler: BookmarksDBHandlerV2
    private lateinit var resourceHandler: ResourceHandler

    /**
     * Image loader
     */
    private lateinit var imageLoader: ImageLoader
    private lateinit var imageLoaderConfigurator: ImageLoaderConfigurator

    /**
     * Retrofit
     */
    private var thisArticle: Article? = null
    private var progressDialog: ProgressDialog? = null

    /**
     * Exoplayer
     */
    private var exoPlayer: SimpleExoPlayer? = null

    internal var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vijest)

        dbHandler = BookmarksDBHandlerV2(this)
        nid = intent.getIntExtra("nid", 0)

        startService(Intent(this, BackgroundPlayerService::class.java))

        prefs = getSharedPreferences("hr.bpervan.novaeva", Context.MODE_PRIVATE)
        openSansBold = Typeface.createFromAsset(assets, "opensans-bold.ttf")
        openSansRegular = Typeface.createFromAsset(assets, "opensans-regular.ttf")

        mGaTracker = (application as NovaEvaApp).getTracker(NovaEvaApp.TrackerName.APP_TRACKER)
        mGaTracker.send(
                HitBuilders.EventBuilder()
                        .setCategory("Vijesti")
                        .setAction("OtvorenaVijest")
                        .setLabel(nid.toString() + "")
                        .build()
        )

        resourceHandler = ResourceHandler(this.intent.getIntExtra("kategorija", Constants.CAT_PROPOVJEDI))

        this.progressDialog = ProgressDialog(this)
        this.progressDialog!!.setMessage("Učitavam...")
        this.progressDialog!!.isIndeterminate = true
        this.progressDialog!!.setCancelable(false)
        this.progressDialog!!.setCanceledOnTouchOutside(false)
        this.progressDialog!!.setButton(DialogInterface.BUTTON_NEGATIVE, "Odustani") { dialog, which -> dialog.cancel() }

        progressDialog!!.setOnCancelListener { }
        progressDialog!!.show()

        imageLoaderConfigurator = ImageLoaderConfigurator(this)
        imageLoader = ImageLoader.getInstance()
        if (!imageLoader.isInited) {
            ImageLoaderConfigurator(this).doInit()
        }
        initUI()

        /*if(!ConnectionChecker.hasConnection(this)){
            Toast.makeText(this, "Internetska veza nije dostupna", Toast.LENGTH_SHORT).show();
			startActivity(new Intent(VijestActivity.this,DashboardActivity.class));
		} else {
			new AsyncHttpPostTask(this).execute(getIntent().getIntExtra("nid",0) + "");
		}*/
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

        thisArticle?.let {
            if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (it.hasImage()) {
                    if (imageLoader.isInited) {
                        imageLoader.displayImage(it.image.size640, headerImage, imageLoaderConfigurator.doConfig(false))
                    }
                } else {
                    val url = prefs.getString("hr.bpervan.novaeva.categoryheader." + it.cid, null)
                    if (url != null) {
                        if (imageLoader.isInited) {
                            imageLoader.displayImage(url, headerImage, imageLoaderConfigurator.doConfig(true))
                        }
                    }
                }
            }
            vijestWebView.reload()
            vijestWebView.loadDataWithBaseURL(null, thisArticle!!.tekst, "text/html", "utf-8", null)
        }
    }

    /**
     * Initializes User interface, makes connections and references to
     * widgets on User interface, sets category specified colour
     */
    private fun initUI() {

        /** Basic data  */
        if (openSansBold != null)
            tvNaslov.typeface = openSansBold

        /** Lets try displaying news using WebView  */
        vijestWebView.reload()
        if (thisArticle != null) {
            vijestWebView.loadDataWithBaseURL(null, thisArticle!!.tekst, "text/html", "UTF-8", null)
        }

        vijestWebView.isFocusable = false
        vijestWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.reload()
                view.loadDataWithBaseURL(null, thisArticle!!.tekst, "text/html", "utf-8", null)
                return false
            }
        }

        vijestWebView.settings.layoutAlgorithm = LayoutAlgorithm.SINGLE_COLUMN

        /* Prevent C/P */
        vijestWebView.setOnLongClickListener { true }
        vijestWebView.isLongClickable = false

        /** Is this 'Duhovni poziv' or 'Odgovori' category?  */
        if (intent.getIntExtra("kategorija", Constants.CAT_PROPOVJEDI) == Constants.CAT_POZIV) {
            setQuestionButton()
        }

        seekArc.setOnSeekBarChangeListener(this)
        tvDuration.typeface = openSansRegular
        tvElapsed.typeface = openSansRegular

        btnHome.setOnClickListener(this)
        btnFace.setOnClickListener(this)
        btnMail.setOnClickListener(this)
        btnBookmark.setOnClickListener(this)
        btnSearch.setOnClickListener(this)
        btnTextPlus.setOnClickListener(this)
        btnBack.setOnClickListener(this)

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

        if (dbHandler.nidExists(nid))
            btnBookmark.setImageResource(R.drawable.vijest_button_bookmarked)
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            headerImage = findViewById<View>(R.id.headerImage) as ImageView
        }
        window.decorView.setBackgroundColor(this.resources.getColor(android.R.color.background_light))
    }

    override fun onResume() {
        //LocalBroadcastManager.getInstance(this).registerReceiver(serviceMessageReceiver, new IntentFilter(BackgroundPlayerService.INTENT_CLASS));
        super.onResume()

        disposable?.dispose()
        disposable = NovaEvaService.instance
                .getArticle(intent.getIntExtra("nid", 0))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ article ->
                    if (article.audio != null) {
                        imgMp3.setImageResource(R.drawable.vijest_ind_mp3_active)

                        createPlayer()
                        streamAudio(article.audio)
                    }
                    if (article.prilozi != null) {
                        imgText.setImageResource(R.drawable.vijest_ind_txt_active)
                        imgText.setOnClickListener(this@VijestActivity)
                    }
                    if (article.youtube != null) {
                        imgLink.setImageResource(R.drawable.vijest_ind_www_active)
                        imgLink.setOnClickListener(this@VijestActivity)
                    }

                    if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        if (article.image != null) {
                            if (imageLoader.isInited) {
                                imageLoader.displayImage(article.image.size640, headerImage, imageLoaderConfigurator.doConfig(false))
                            }
                        } else {
                            val url = prefs.getString("hr.bpervan.novaeva.categoryheader." + article.cid, null)
                            if (url != null) {
                                if (imageLoader.isInited) {
                                    imageLoader.displayImage(url, headerImage, imageLoaderConfigurator.doConfig(true))
                                }
                            }
                        }
                    }
                    if (article.cid == Constants.CAT_POZIV) {
                        setQuestionButton()
                    }

                    tvNaslov.text = article.naslov
                    vijestWebView.reload()
                    vijestWebView.loadDataWithBaseURL(null, article.tekst, "text/html", "UTF-8", null)

                    this@VijestActivity.thisArticle = article
                    progressDialog!!.dismiss()

                }) { t -> Log.e("vijestiError", t.message, t) }
    }

    override fun onPause() {
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceMessageReceiver);
        super.onPause()
        disposable?.dispose() //// TODO: 07.10.17.


        /*if(mPlayer != null){
			if(mPlayer.isPlaying()){
				mPlayer.stop();
			}
			mPlayer.release();
			seekArc.removeCallbacks(onEverySecond);
		}
		mPlayer = null;*/
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
        var resources = intArrayOf(R.drawable.izbornik_navbgodgovori, R.drawable.vijest_naslovnaodgovori)
        when (this.resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> resources = resourceHandler.getVijestResource(Configuration.ORIENTATION_LANDSCAPE)
            Configuration.ORIENTATION_PORTRAIT -> resources = resourceHandler.getVijestResource(Configuration.ORIENTATION_PORTRAIT)
        }
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
                    messageIntent.putExtra(BackgroundPlayerService.KEY_PATH, thisArticle!!.audio)
                    messageIntent.putExtra(BackgroundPlayerService.KEY_TITLE, thisArticle!!.naslov)
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
            R.id.btnHome -> startActivity(Intent(this, DashboardActivity::class.java))
            R.id.btnSearch ->
                if (ConnectionChecker.hasConnection(this@VijestActivity))
                    showSearchPopup()
            R.id.btnBookmark ->
                if (dbHandler.nidExists(nid)) {
                    dbHandler.deleteVijest(nid)
                    btnBookmark.setImageResource(R.drawable.vijest_button_bookmark)
                } else {
                    dbHandler.insertArticle(thisArticle)
                    btnBookmark.setImageResource(R.drawable.vijest_button_bookmarked)
                }
            R.id.btnFace -> {
                val temp = "http://novaeva.com/node/" + intent.getIntExtra("nid", 1025)
                val faceIntent = Intent(Intent.ACTION_SEND)
                faceIntent.type = "text/plain"
                faceIntent.putExtra(Intent.EXTRA_TEXT, temp)
                startActivity(Intent.createChooser(faceIntent, "Facebook"))
            }
            R.id.btnMail -> {
                val temp2 = "http://novaeva.com/node/" + intent.getIntExtra("nid", 1025)
                val mailIntent = Intent(Intent.ACTION_SEND)
                mailIntent.type = "message/rfc822"
                mailIntent.putExtra(Intent.EXTRA_SUBJECT, thisArticle!!.naslov)
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
            R.id.imgLink -> if (thisArticle!!.youtube != null) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(thisArticle!!.youtube)))
            }
            R.id.imgText -> if (thisArticle!!.prilozi != null) {
                startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW, Uri.parse(thisArticle!!.prilozi[0].url)),
                        "Otvaranje dokumenta " + thisArticle!!.prilozi[0].naziv))
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
            val i = Intent(this@VijestActivity, SearchActivity::class.java)
            i.putExtra("searchString", search)
            startActivity(i)
        }
        search.setNegativeButton("Odustani") { dialog, whichButton -> }
        search.show()
    }

    private fun setQuestionButton() {
        btnPoziv.visibility = View.VISIBLE
        btnPoziv.setOnClickListener(this@VijestActivity)
    }

    private fun setAttachments() {
        thisArticle?.let {
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
        tv.typeface = openSansRegular
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        error.setView(tv)

        error.setPositiveButton("Pokušaj ponovno") { dialog, which ->
            //new AsyncHttpPostTask(VijestActivity.this).execute(ovaVijest.getNid() + "");
        }
        error.setNegativeButton("Povratak") { dialog, whichButton ->
            startActivity(Intent(this@VijestActivity, DashboardActivity::class.java))
            this@VijestActivity.onBackPressed()
        }
        error.show()
    }

    companion object {

        private val TAG = "VijestActivity"

        /**
         * Custom font resources
         */
        private var openSansBold: Typeface? = null
        private var openSansRegular: Typeface? = null
    }
}