package hr.bpervan.novaeva.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.SeekBar
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.actions.sendEmailIntent
import hr.bpervan.novaeva.actions.shareIntent
import hr.bpervan.novaeva.cache.CacheService
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaCategory
import hr.bpervan.novaeva.model.EvaContent
import hr.bpervan.novaeva.model.OpenContentEvent
import hr.bpervan.novaeva.services.AudioPlayerService
import hr.bpervan.novaeva.services.NovaEvaService
import hr.bpervan.novaeva.storage.EvaContentDbAdapter
import hr.bpervan.novaeva.storage.RealmConfigProvider
import hr.bpervan.novaeva.utilities.ImageLoaderConfigurator
import hr.bpervan.novaeva.utilities.subscribeAsync
import io.reactivex.disposables.Disposable
import io.realm.Realm
import kotlinx.android.synthetic.main.collapsing_content_header.view.*
import kotlinx.android.synthetic.main.fragment_eva_content.*
import kotlinx.android.synthetic.main.toolbar_eva_content.view.*

/**
 * Created by vpriscan on 04.12.17..
 */
class EvaContentFragment : EvaBaseFragment(), SeekBar.OnSeekBarChangeListener {

    companion object : EvaFragmentFactory<EvaContentFragment, OpenContentEvent> {

        private const val CONTENT_ID_KEY = "contentId"
        private const val CATEGORY_ID_KEY = "categoryId"
        private const val THEME_ID_KEY = "themeId"

        override fun newInstance(initializer: OpenContentEvent): EvaContentFragment {
            return EvaContentFragment().apply {
                arguments = Bundle().apply {
                    putLong(CONTENT_ID_KEY, initializer.contentMetadata.contentId)
                    putLong(CATEGORY_ID_KEY, initializer.contentMetadata.categoryId)
                    putInt(THEME_ID_KEY, initializer.themeId)
                }
            }
        }
    }


    private var exoPlayer: ExoPlayer? = null
    private val evaPlayerEventListener = EvaPlayerEventListener()

    private lateinit var realm: Realm

    private var contentId: Long = 0
    private var categoryId: Long = 0
    private var evaContent: EvaContent? = null

    private var fetchFromServerDisposable: Disposable? = null
        set(value) {
            field = safeReplaceDisposable(field, value)
        }

    private var evaContentChangesDisposable: Disposable? = null
        set(value) {
            field = safeReplaceDisposable(field, value)
        }

    private var evaContentMetadataChangesDisposable: Disposable? = null
        set(value) {
            field = safeReplaceDisposable(field, value)
        }

    private var showTools = false

    private var themeId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inState = savedInstanceState ?: arguments!!
        contentId = inState.getLong(CONTENT_ID_KEY)
        categoryId = inState.getLong(CATEGORY_ID_KEY)
        themeId = inState.getInt(THEME_ID_KEY, -1)

        savedInstanceState ?: NovaEvaApp.defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("Vijesti")
                        .setAction("OtvorenaVijest")
                        .setLabel(contentId.toString())
                        .setValue(contentId)
                        .build())

        realm = Realm.getInstance(RealmConfigProvider.evaDBConfig)

        if (savedInstanceState == null) {
            fetchContentFromServer(contentId)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(CONTENT_ID_KEY, contentId)
        outState.putLong(CATEGORY_ID_KEY, categoryId)
        outState.putInt(THEME_ID_KEY, themeId)
        vijestWebView?.saveState(outState)

        super.onSaveInstanceState(outState)
    }

    private fun createIfMissingAndSubscribeToEvaContentUpdates() {
        EvaContentDbAdapter.createIfMissingEvaContentAsync(realm, contentId) {
            subscribeToEvaContentUpdates()
        }
    }

    private fun subscribeToEvaContentUpdates() {
        view ?: return

        evaContentMetadataChangesDisposable = EvaContentDbAdapter.subscribeToEvaContentMetadataUpdatesAsync(realm, contentId) { evaContentMetadata ->
            options.btnBookmark.setImageResource(
                    if (evaContentMetadata.bookmark) R.drawable.action_button_bookmarked
                    else R.drawable.action_button_bookmark)
        }

        evaContentChangesDisposable = EvaContentDbAdapter.subscribeToEvaContentUpdatesAsync(realm, contentId) { evaContent ->

            evaCollapsingBar.collapsingToolbar.title = evaContent.contentMetadata!!.title

            val coverImageInfo = evaContent.image
            val coverImageView = evaCollapsingBar.coverImage

            if (coverImageInfo != null) {
                if (coverImageView != null) {
                    imageLoader.displayImage(coverImageInfo.url, coverImageView,
                            ImageLoaderConfigurator.createDefaultDisplayImageOptions(false))
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

            evaContent.audioURL?.let { audioUrl ->
                imgMp3.setImageResource(R.drawable.vijest_ind_mp3_active)
                if (audioUrl != this.evaContent?.audioURL) {
                    prepareAudioStream(context!!, audioUrl)
                }
                exoPlayer?.let { player ->
                    player_view?.let { playerView ->
                        playerView.visibility = View.VISIBLE
                        playerView.requestFocus()
                        playerView.player = player
                    }
                }
            }

            evaContent.videoURL?.let { videoUrl ->
                imgLink.setImageResource(R.drawable.vijest_ind_www_active)
                imgLink.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)))
                }
            }
            if (evaContent.attachments.isNotEmpty()) {
                imgText.setImageResource(R.drawable.vijest_ind_txt_active)
                imgText.setOnClickListener {
                    startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW, Uri.parse(evaContent.attachments[0]!!.url)),
                            "Otvaranje dokumenta " + evaContent.attachments[0]!!.name))
                }
            }

            loadingCircle.visibility = View.GONE

            this.evaContent = evaContent
        }
    }

    private fun fetchContentFromServer(contentId: Long) {
        fetchFromServerDisposable = NovaEvaService.instance.getContentData(contentId)
                .subscribeAsync({ contentDataDTO ->
                    CacheService.cache(realm, contentDataDTO)
                }) {
                    context?.let { ctx ->
                        NovaEvaApp.showFetchErrorSnackbar(it, ctx, view)
                    }
                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val inflaterToUse =
                if (themeId != -1)
                    inflater.cloneInContext(ContextThemeWrapper(activity, themeId))
                else
                    inflater

        return inflaterToUse.inflate(R.layout.fragment_eva_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI(savedInstanceState)
    }

    private fun initUI(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            vijestWebView.restoreState(savedInstanceState)
        }

        val ctx = context ?: return

        loadingCircle.visibility = View.VISIBLE

        createIfMissingAndSubscribeToEvaContentUpdates()

        vijestWebView.settings.builtInZoomControls = true
        vijestWebView.settings.displayZoomControls = false

        vijestWebView.isFocusable = false
        vijestWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
                webView.reload()
                webView.loadDataWithBaseURL(null, evaContent?.text, "text/html", "utf-8", null)
                return false
            }
        }

        vijestWebView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN

        /* Prevent C/P */
        vijestWebView.setOnLongClickListener { true }
        vijestWebView.isLongClickable = false

        /** Is this 'Duhovni poziv' or 'Odgovori' category?  */
        if (categoryId == EvaCategory.POZIV.id.toLong()) {
            btnPoziv.visibility = View.VISIBLE
            btnPoziv.setOnClickListener {
                val text = "Hvaljen Isus i Marija, javljam Vam se jer razmišljam o duhovnom pozivu."
                sendEmailIntent(ctx, "Duhovni poziv", text, arrayOf("duhovnipoziv@gmail.com"))
            }
        }

        options.btnSearch.setOnClickListener {
            showSearchPopup()
        }
        options.btnBookmark.setOnClickListener {
            evaContent?.let { evaContent ->
                val evaContentMetadata = evaContent.contentMetadata!!
                EvaContentDbAdapter.updateEvaContentMetadataAsync(realm, evaContentMetadata.contentId) {
                    it.bookmark = !it.bookmark
                }
            }
        }

        options.btnShare.setOnClickListener {
            shareIntent(ctx, "http://novaeva.com/node/$contentId")
        }

        options.btnMail.setOnClickListener {
            sendEmailIntent(ctx, evaContent!!.contentMetadata!!.title, "http://novaeva.com/node/$contentId")
        }

        vijestWebView.settings.defaultFontSize = prefs.getInt("hr.bpervan.novaeva.velicinateksta", 14)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player_view?.player = null
        exoPlayer?.removeListener(evaPlayerEventListener)
        exoPlayer = null
    }

    private fun prepareAudioStream(context: Context, audioUri: String) {

        val streamingUri = Uri.parse(audioUri)

        val dataSourceFactory = DefaultDataSourceFactory(context,
                Util.getUserAgent(context, resources.getString(R.string.app_name)),
                DefaultBandwidthMeter())

//        val factory = ExtractorMediaSource.Factory(dataSourceFactory).setCustomCacheKey(audioUri)
//        val mediaSource = factory.createMediaSource(streamingUri)

        val exoPlayer = NovaEvaApp.evaPlayer.prepareIfNeededAndGetPlayer(audioUri) {
            ExtractorMediaSource(streamingUri, dataSourceFactory, DefaultExtractorsFactory(), Handler(), null, audioUri)
        }

        this.exoPlayer?.removeListener(evaPlayerEventListener)
        exoPlayer.removeListener(evaPlayerEventListener)
        exoPlayer.addListener(evaPlayerEventListener)
        this.exoPlayer = exoPlayer
    }

    private inner class EvaPlayerEventListener : Player.DefaultEventListener() {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if ((playbackState == Player.STATE_READY)) {
                if (playWhenReady) {
                    exoPlayer?.let {
                        NovaEvaApp.evaPlayer.currentAudioTrackUri = evaContent?.audioURL
                        NovaEvaApp.evaPlayer.currentPlayer = it
                        context?.startService(Intent(context, AudioPlayerService::class.java))
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        realm.close()
        super.onDestroy()
    }

    private fun showSearchPopup() {
        val searchBuilder = AlertDialog.Builder(context)
        searchBuilder.setTitle("Pretraga")
        val et = EditText(context)
        searchBuilder.setView(et)
        searchBuilder.setPositiveButton("Pretraži") { _, _ ->
            val search = et.text.toString()
            /*todo only search content text*/
        }
        searchBuilder.setNegativeButton("Odustani") { _, _ -> }
        searchBuilder.show()
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        /**/
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        /**/
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        /**/
    }
}