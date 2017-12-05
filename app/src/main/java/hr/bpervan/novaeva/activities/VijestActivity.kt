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
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.CacheService
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.actions.sendEmailIntent
import hr.bpervan.novaeva.actions.shareIntent
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaCategory
import hr.bpervan.novaeva.model.EvaContent
import hr.bpervan.novaeva.services.BackgroundPlayerService
import hr.bpervan.novaeva.services.NovaEvaService
import hr.bpervan.novaeva.storage.EvaContentDbAdapter
import hr.bpervan.novaeva.storage.RealmConfigProvider
import hr.bpervan.novaeva.utilities.ConnectionChecker
import hr.bpervan.novaeva.utilities.ImageLoaderConfigurator
import hr.bpervan.novaeva.utilities.subscribeAsync
import io.reactivex.disposables.Disposable
import io.realm.Realm
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_vijest.*
import kotlinx.android.synthetic.main.eva_collapsing_bar.view.*
import kotlinx.android.synthetic.main.vijest_fake_action_bar.view.*

class VijestActivity : EvaBaseActivity(), View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    /** ------------------------------------------FIELDS------------------------------------------ */


    private var contentId: Long = 0
    private var themeId: Int = 0

    private lateinit var realm: Realm
    private var evaContent: EvaContent? = null

    private var fetchFromServerDisposable: Disposable? = null
    private var evaContentChangesDisposable: Disposable? = null
    private var evaContentMetadataChangesDisposable: Disposable? = null
    /**
     * Exoplayer
     */
    private var exoPlayer: SimpleExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inState = savedInstanceState ?: intent.extras
        contentId = inState.getLong("contentId", -1L)
        themeId = inState.getInt("themeId", -1)

        if (themeId != -1) {
            setTheme(themeId)
        }

        if (contentId == -1L) {
            contentId = intent.data?.lastPathSegment?.toLongOrNull() ?: -1L
        }

        if (contentId == -1L) {
            finish()
            return
        }

        setContentView(R.layout.activity_vijest)

        startService(Intent(this, BackgroundPlayerService::class.java)) //todo

        (application as NovaEvaApp).getTracker(NovaEvaApp.TrackerName.APP_TRACKER).send(
                HitBuilders.EventBuilder()
                        .setCategory("Vijesti")
                        .setAction("OtvorenaVijest")
                        .setLabel(contentId.toString() + "")
                        .build()
        )

        realm = Realm.getInstance(RealmConfigProvider.evaDBConfig)

        createIfMissingAndSubscribeToEvaContentUpdates()
        initUI()

        if (ConnectionChecker.hasConnection(this) && savedInstanceState == null) {
            fetchContentFromServer(contentId)
        } else {
            NovaEvaApp.showFetchErrorSnackbar(null, this, vijestWebView)
        }
    }

    private fun createIfMissingAndSubscribeToEvaContentUpdates() {
        EvaContentDbAdapter.createIfMissingEvaContentAsync(realm, contentId) {
            subscribeToEvaContentUpdates()
        }
    }

    private fun subscribeToEvaContentUpdates() {
        evaContentMetadataChangesDisposable?.dispose()
        evaContentMetadataChangesDisposable = EvaContentDbAdapter
                .subscribeToEvaContentMetadataUpdatesAsync(realm, contentId) { evaContentMetadata ->
                    fakeActionBar.btnBookmark.setImageResource(
                            if (evaContentMetadata.bookmark) R.drawable.action_button_bookmarked
                            else R.drawable.action_button_bookmark)
                }
        evaContentChangesDisposable?.dispose()
        evaContentChangesDisposable = EvaContentDbAdapter.subscribeToEvaContentUpdatesAsync(realm, contentId) { evaContent ->
            this.evaContent = evaContent

            evaCollapsingBar.collapsingToolbar.title = evaContent.contentMetadata!!.title


            val coverImageInfo = evaContent.image
            val coverImageView = evaCollapsingBar.coverImage

            if (coverImageInfo != null) {
                if (coverImageView != null) {
                    imageLoader.displayImage(coverImageInfo.url, coverImageView, ImageLoaderConfigurator.createDefaultDisplayImageOptions(false))
                }
            } else {
                //todo
//                val url = prefs.getString("hr.bpervan.novaeva.categoryheader." + contentData.contentMetadata!!.directoryId, null)
//                if (url != null && headerImage != null) {
//                    imageLoader.displayImage(url, headerImage, ImageLoaderConfigurator.createDefaultDisplayImageOptions(true))
//                }
            }
            vijestWebView.reload()
            vijestWebView.loadDataWithBaseURL(null, evaContent.text, "text/html", "utf-8", null)

            if (evaContent.audioURL != null) {
                imgMp3.setImageResource(R.drawable.vijest_ind_mp3_active)

                //TODO DO ONLY ONCE
//                createPlayer()
//                streamAudio(contentDataDTO.audio)
            }
            if (evaContent.videoURL != null) {
                imgLink.setImageResource(R.drawable.vijest_ind_www_active)
                imgLink.setOnClickListener(this)
            }
            if (evaContent.attachments.isNotEmpty()) {
                imgText.setImageResource(R.drawable.vijest_ind_txt_active)
                imgText.setOnClickListener(this)
            }

            loadingCircle.visibility = View.GONE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong("contentId", contentId)
        outState.putInt("themeId", themeId)

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
        setContentView(R.layout.activity_vijest)
        createIfMissingAndSubscribeToEvaContentUpdates()
        initUI()
    }

    /**
     * Initializes User interface, makes connections and references to
     * widgets on User interface, sets category specified colour
     */
    private fun initUI() {

        /** Basic data  */
//        NovaEvaApp.openSansBold?.let {
//            tvNaslov.typeface = it
//        }

        vijestWebView.settings.builtInZoomControls = true
        vijestWebView.settings.displayZoomControls = false

        vijestWebView.isFocusable = false
        vijestWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.reload()
                view.loadDataWithBaseURL(null, evaContent?.text, "text/html", "utf-8", null)
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

        applyFakeActionBarVisibility()


        btnToggleActionBar.setOnClickListener {
            val showFakeActionBarPrefKey = "hr.bpervan.novaeva.showFakeActionBar"
            val showFakeActionBarPref = prefs.getBoolean(showFakeActionBarPrefKey, false)
            prefs.edit().putBoolean(showFakeActionBarPrefKey, !showFakeActionBarPref).apply()
            applyFakeActionBarVisibility()
        }

        fakeActionBar.btnSearch.setOnClickListener {
            if (ConnectionChecker.hasConnection(this)) {
                showSearchPopup()
            }
        }
        fakeActionBar.btnBookmark.setOnClickListener {
            val evaContent = this.evaContent
            if (evaContent != null) {
                val evaContentMetadata = evaContent.contentMetadata!!
                EvaContentDbAdapter.updateEvaContentMetadataAsync(realm, evaContentMetadata.contentId) {
                    it.bookmark = !it.bookmark
                }
            }
        }

        fakeActionBar.btnShare.setOnClickListener {
            shareIntent(this, "http://novaeva.com/node/$contentId")
        }

        fakeActionBar.btnMail.setOnClickListener {
            sendEmailIntent(this, evaContent!!.contentMetadata!!.title, "http://novaeva.com/node/$contentId")
        }

        /** Set category name and set text size */
        vijestWebView.settings.defaultFontSize = prefs.getInt("hr.bpervan.novaeva.velicinateksta", 14)
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
//        fakeActionBar.btnBookmark.setImageResource(R.drawable.action_button_bookmark)
        window.decorView.setBackgroundResource(android.R.color.background_light)
    }

    private fun applyFakeActionBarVisibility() {
        if (prefs.getBoolean("hr.bpervan.novaeva.showFakeActionBar", false)) {
            fakeActionBar.visibility = View.VISIBLE
            btnToggleActionBar.setImageResource(R.drawable.action_button_toolbar_hide)
        } else {
            fakeActionBar.visibility = View.GONE
            btnToggleActionBar.setImageResource(R.drawable.action_button_toolbar_show)
        }
    }

    private fun fetchContentFromServer(contentId: Long) {
        loadingCircle.visibility = View.VISIBLE

        fetchFromServerDisposable?.dispose()
        fetchFromServerDisposable = NovaEvaService.instance
                .getContentData(contentId)
                .subscribeAsync({ contentDataDTO ->
                    CacheService.cache(realm, contentDataDTO)
                }) {
                    NovaEvaApp.showFetchErrorSnackbar(it, this, vijestWebView)
                }
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

        fetchFromServerDisposable?.dispose()
        evaContentChangesDisposable?.dispose()
        evaContentMetadataChangesDisposable?.dispose()
        realm.close()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnPoziv -> {
                when (intent.getIntExtra("kategorija", 11)) {
                    EvaCategory.POZIV.id -> {
                        val text = "Hvaljen Isus i Marija, javljam Vam se jer razmišljam o duhovnom pozivu."
                        sendEmailIntent(this, "Duhovni poziv", text, arrayOf("duhovnipoziv@gmail.com"))
                    }
                }
            }
            R.id.btnPlay -> {
                Log.d(TAG, "btnPlay")
                if (BackgroundPlayerService.isRunning) {
                    val messageIntent = Intent(this@VijestActivity, BackgroundPlayerService::class.java)
                    messageIntent.putExtra(BackgroundPlayerService.KEY_DIRECTIVE, BackgroundPlayerService.DIRECTIVE_SET_SOURCE_PLAY)
                    messageIntent.putExtra(BackgroundPlayerService.KEY_PATH, evaContent!!.audioURL)
                    messageIntent.putExtra(BackgroundPlayerService.KEY_TITLE, evaContent!!.contentMetadata!!.title)
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
//            R.id.btnTextPlus -> {
//                var mCurrentSize = prefs.getInt("hr.bpervan.novaeva.velicinateksta", 14)
//                mCurrentSize += 2
//                if (mCurrentSize >= 28) {
//                    mCurrentSize = 12
//                }
//
//                prefs.edit().putInt("hr.bpervan.novaeva.velicinateksta", mCurrentSize).apply()
//                vijestWebView.settings.defaultFontSize = mCurrentSize
//            }
//            R.id.btnBack -> this@VijestActivity.onBackPressed()
            R.id.imgLink ->
                evaContent!!.videoURL?.let { videoUrl ->
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)))
                }
            R.id.imgText ->
                if (evaContent!!.attachments.isNotEmpty()) {
                    startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW, Uri.parse(evaContent!!.attachments[0]!!.url)),
                            "Otvaranje dokumenta " + evaContent!!.attachments[0]!!.name))
                }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.move_left_in, R.anim.move_right_out)
    }

    private fun showSearchPopup() {
        val searchBuilder = AlertDialog.Builder(this)
        searchBuilder.setTitle("Pretraga")
        val et = EditText(this)
        searchBuilder.setView(et)
        searchBuilder.setPositiveButton("Pretraži") { _, _ ->
            val search = et.text.toString()
            NovaEvaApp.goSearch(search, this)
        }
        searchBuilder.setNegativeButton("Odustani") { dialog, whichButton -> }
        searchBuilder.show()
    }

    private fun setQuestionButton() {
        btnPoziv.visibility = View.VISIBLE
        btnPoziv.setOnClickListener(this)
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