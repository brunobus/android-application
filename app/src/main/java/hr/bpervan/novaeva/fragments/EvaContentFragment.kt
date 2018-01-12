package hr.bpervan.novaeva.fragments

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.SeekBar
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import hr.bpervan.novaeva.CacheService
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.actions.sendEmailIntent
import hr.bpervan.novaeva.actions.shareIntent
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaCategory
import hr.bpervan.novaeva.model.EvaContent
import hr.bpervan.novaeva.services.NovaEvaService
import hr.bpervan.novaeva.storage.EvaContentDbAdapter
import hr.bpervan.novaeva.storage.RealmConfigProvider
import hr.bpervan.novaeva.utilities.ImageLoaderConfigurator
import hr.bpervan.novaeva.utilities.subscribeAsync
import io.reactivex.disposables.Disposable
import io.realm.Realm
import kotlinx.android.synthetic.main.collapsing_content_header.view.*
import kotlinx.android.synthetic.main.fragment_eva_content.view.*
import kotlinx.android.synthetic.main.toolbar_eva_content.view.*

/**
 * Created by vpriscan on 04.12.17..
 */
class EvaContentFragment : EvaBaseFragment(), SeekBar.OnSeekBarChangeListener {
    companion object {
        private val CONTENT_ID_KEY = "contentId"
        private val CATEGORY_ID_KEY = "categoryId"
        fun newInstance(contentId: Long, categoryId: Long): EvaContentFragment {
            return EvaContentFragment().apply {
                arguments = Bundle().apply {
                    putLong(CONTENT_ID_KEY, contentId)
                    putLong(CATEGORY_ID_KEY, categoryId)
                }
            }
        }
    }

    private val exoPlayer: SimpleExoPlayer by lazy {
        NovaEvaApp.exoPlayerHolder.exoPlayer
    }

    private lateinit var realm: Realm

    private var contentId: Long = 0
    private var categoryId: Long = 0
    private var evaContent: EvaContent? = null

    private var fetchFromServerDisposable: Disposable? = null
    private var evaContentChangesDisposable: Disposable? = null
    private var evaContentMetadataChangesDisposable: Disposable? = null

    private var showTools = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        val inState = savedInstanceState ?: arguments!!
        contentId = inState.getLong(CONTENT_ID_KEY)
        categoryId = inState.getLong(CATEGORY_ID_KEY)

        realm = Realm.getInstance(RealmConfigProvider.evaDBConfig)

        fetchContentFromServer(contentId)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(CONTENT_ID_KEY, contentId)

        super.onSaveInstanceState(outState)
    }

    private fun createIfMissingAndSubscribeToEvaContentUpdates(view: View) {
        EvaContentDbAdapter.createIfMissingEvaContentAsync(realm, contentId) {
            subscribeToEvaContentUpdates(view)
        }
    }

    private var previousPlayerView: SimpleExoPlayerView? = null

    private fun subscribeToEvaContentUpdates(view: View) {
        evaContentMetadataChangesDisposable?.dispose()
        evaContentMetadataChangesDisposable = EvaContentDbAdapter
                .subscribeToEvaContentMetadataUpdatesAsync(realm, contentId) { evaContentMetadata ->
                    view.options.btnBookmark.setImageResource(
                            if (evaContentMetadata.bookmark) R.drawable.action_button_bookmarked
                            else R.drawable.action_button_bookmark)
                }
        evaContentChangesDisposable?.dispose()
        evaContentChangesDisposable = EvaContentDbAdapter.subscribeToEvaContentUpdatesAsync(realm, contentId) { evaContent ->

            view.evaCollapsingBar.collapsingToolbar.title = evaContent.contentMetadata!!.title


            val coverImageInfo = evaContent.image
            val coverImageView = view.evaCollapsingBar.coverImage

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
            view.vijestWebView.reload()
            view.vijestWebView.loadDataWithBaseURL(null, evaContent.text, "text/html", "utf-8", null)

            evaContent.audioURL?.let { audioUrl ->
                view.imgMp3.setImageResource(R.drawable.vijest_ind_mp3_active)
                if (audioUrl != this.evaContent?.audioURL) {
                    prepareAudioStream(audioUrl)
                }
                view.player_view?.let { playerView ->
                    playerView.visibility = View.VISIBLE
                    playerView.requestFocus()
                    SimpleExoPlayerView.switchTargetView(exoPlayer, previousPlayerView, playerView)
                    previousPlayerView = playerView
                }
            }

            evaContent.videoURL?.let { videoUrl ->
                view.imgLink.setImageResource(R.drawable.vijest_ind_www_active)
                view.imgLink.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)))
                }
            }
            if (evaContent.attachments.isNotEmpty()) {
                view.imgText.setImageResource(R.drawable.vijest_ind_txt_active)
                view.imgText.setOnClickListener {
                    startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW, Uri.parse(evaContent.attachments[0]!!.url)),
                            "Otvaranje dokumenta " + evaContent.attachments[0]!!.name))
                }
            }

            view.loadingCircle.visibility = View.GONE

            this.evaContent = evaContent
        }
    }

    private fun fetchContentFromServer(contentId: Long) {
        fetchFromServerDisposable?.dispose()
        fetchFromServerDisposable = NovaEvaService.instance
                .getContentData(contentId)
                .subscribeAsync({ contentDataDTO ->
                    CacheService.cache(realm, contentDataDTO)
                }) {
                    context?.let { ctx ->
                        NovaEvaApp.showFetchErrorSnackbar(it, ctx, view)
                    }
                }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_eva_content, container, false).apply {

            loadingCircle.visibility = View.VISIBLE

            createIfMissingAndSubscribeToEvaContentUpdates(this@apply)

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

            vijestWebView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN

            /* Prevent C/P */
            vijestWebView.setOnLongClickListener { true }
            vijestWebView.isLongClickable = false

            /** Is this 'Duhovni poziv' or 'Odgovori' category?  */
            if (categoryId == EvaCategory.POZIV.id.toLong()) {
                btnPoziv.visibility = View.VISIBLE
                btnPoziv.setOnClickListener {
                    val text = "Hvaljen Isus i Marija, javljam Vam se jer razmišljam o duhovnom pozivu."
                    sendEmailIntent(context, "Duhovni poziv", text, arrayOf("duhovnipoziv@gmail.com"))
                }
            }

//            seekArc.setOnSeekBarChangeListener(this@EvaContentFragment)
//
//            NovaEvaApp.openSansRegular?.let {
//                tvDuration.typeface = it
//                tvElapsed.typeface = it
//            }

            applyFakeActionBarVisibility(this@apply)

            btnToggleActionBar.setOnClickListener {
                showTools = !showTools
                applyFakeActionBarVisibility(this@apply)
            }

            options.btnSearch.setOnClickListener {
                showSearchPopup()
            }
            options.btnBookmark.setOnClickListener {
                val evaContent = this@EvaContentFragment.evaContent
                if (evaContent != null) {
                    val evaContentMetadata = evaContent.contentMetadata!!
                    EvaContentDbAdapter.updateEvaContentMetadataAsync(realm, evaContentMetadata.contentId) {
                        it.bookmark = !it.bookmark
                    }
                }
            }

            options.btnShare.setOnClickListener {
                shareIntent(context, "http://novaeva.com/node/$contentId")
            }

            options.btnMail.setOnClickListener {
                sendEmailIntent(context, evaContent!!.contentMetadata!!.title, "http://novaeva.com/node/$contentId")
            }

            /** Set category name and set text size */
            vijestWebView.settings.defaultFontSize = prefs.getInt("hr.bpervan.novaeva.velicinateksta", 14)

            //todo check what is this
            /*if(mPlayer != null){
                mp3Player.setVisibility(View.VISIBLE);
                btnPlay.setOnClickListener(EvaContentActivity.this);
                btnPause.setOnClickListener(EvaContentActivity.this);

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
        }
    }

    private fun applyFakeActionBarVisibility(view: View) {
        if (showTools) {
            view.options.visibility = View.VISIBLE
            view.btnToggleActionBar.setImageResource(R.drawable.action_button_toolbar_hide)
        } else {
            view.options.visibility = View.GONE
            view.btnToggleActionBar.setImageResource(R.drawable.action_button_toolbar_show)
        }
    }

    private fun prepareAudioStream(audioUri: String) {
        exoPlayer.stop()

        val RENDER_COUNT = 1
        val minBufferMs = 1000
        val minRebufferMs = 5000

        val streamingUri = Uri.parse(audioUri)

        val bandwidthMeter = DefaultBandwidthMeter()
        val dataSourceFactory = DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, resources.getString(R.string.app_name)),
                bandwidthMeter)
        val extractorsFactory = DefaultExtractorsFactory()
        val mediaSource = ExtractorMediaSource(streamingUri, dataSourceFactory, extractorsFactory, Handler(), null, audioUri)

        exoPlayer.prepare(mediaSource)
    }

    override fun onDetach() {
        evaContentChangesDisposable?.dispose()
        evaContentMetadataChangesDisposable?.dispose()
        super.onDetach()
    }

    override fun onDestroy() {
        fetchFromServerDisposable?.dispose()
        previousPlayerView = null
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