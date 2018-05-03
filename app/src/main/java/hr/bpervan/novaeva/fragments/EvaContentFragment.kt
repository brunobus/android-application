package hr.bpervan.novaeva.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.util.SCROLL_PERCENT_KEY
import hr.bpervan.novaeva.util.sendEmailIntent
import hr.bpervan.novaeva.util.EvaCache
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaCategory
import hr.bpervan.novaeva.model.EvaContent
import hr.bpervan.novaeva.model.OpenContentEvent
import hr.bpervan.novaeva.player.EvaPlayerEventListener
import hr.bpervan.novaeva.services.novaEvaService
import hr.bpervan.novaeva.storage.EvaContentDbAdapter
import hr.bpervan.novaeva.storage.RealmConfigProvider
import hr.bpervan.novaeva.util.networkRequest
import hr.bpervan.novaeva.views.*
import io.reactivex.disposables.Disposable
import io.realm.Realm
import kotlinx.android.synthetic.main.collapsing_content_header.view.*
import kotlinx.android.synthetic.main.fragment_eva_content.*

/**
 * Created by vpriscan on 04.12.17..
 */
class EvaContentFragment : EvaBaseFragment() {

    companion object : EvaFragmentFactory<EvaContentFragment, OpenContentEvent> {

        private const val CONTENT_ID_KEY = "contentId"
        private const val CATEGORY_ID_KEY = "categoryId"
        private const val THEME_ID_KEY = "themeId"

        override fun newInstance(initializer: OpenContentEvent): EvaContentFragment {
            return EvaContentFragment().apply {
                arguments = bundleOf(
                        CONTENT_ID_KEY to initializer.contentMetadata.contentId,
                        CATEGORY_ID_KEY to initializer.contentMetadata.categoryId,
                        THEME_ID_KEY to initializer.themeId
                )
            }
        }
    }


    private var exoPlayer: ExoPlayer? = null
        set(value) {
            field?.removeListener(evaPlayerEventListener)
            value?.removeListener(evaPlayerEventListener)
            value?.addListener(evaPlayerEventListener)
            field = value
        }

    private val evaPlayerEventListener = EvaPlayerEventListener({ context }, { exoPlayer }, { evaContent?.audioURL })

    private lateinit var realm: Realm

    private var contentId: Long = 0
    private var categoryId: Long = 0
    private var evaContent: EvaContent? = null

    private val handler = Handler()

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
        outState.putFloat(SCROLL_PERCENT_KEY, scrollView.calcScrollYPercent(scrollView.getChildAt(0).height))

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
            //todo move to options drawer
//            options.btnBookmark.setImageResource(
//                    if (evaContentMetadata.bookmark) R.drawable.action_button_bookmarked
//                    else R.drawable.action_button_bookmark)
        }

        evaContentChangesDisposable = EvaContentDbAdapter.subscribeToEvaContentUpdatesAsync(realm, contentId) { evaContent ->

            evaCollapsingBar.collapsingToolbar.title = evaContent.contentMetadata!!.title

            val coverImageInfo = evaContent.image
            val coverImageView = evaCollapsingBar.coverImage

            if (coverImageInfo != null) {
                if (coverImageView != null) {
                    imageLoader.displayImage(coverImageInfo.url, coverImageView)
                }
            } else {
                //TODO bring back category headers
//                val url = prefs.getString("hr.bpervan.novaeva.categoryheader." + contentData.contentMetadata!!.directoryId, null)
//                if (url != null && headerImage != null) {
//                    imageLoader.displayImage(url, headerImage)
//                }
            }
            vijestWebView.loadHtmlText(evaContent.text)

            evaContent.audioURL?.let { audioUrl ->
                imgMp3.setImageResource(R.drawable.vijest_ind_mp3_active)
                if (audioUrl != this.evaContent?.audioURL) {
                    prepareAudioStream(context!!, audioUrl)
                }
                exoPlayer?.let { player ->
                    player_view?.let { playerView ->
                        playerView.isVisible = true
                        playerView.requestFocus()
                        playerView.player = player
                    }
                }
            }

            evaContent.videoURL?.let { videoUrl ->
                imgLink.setImageResource(R.drawable.vijest_ind_www_active)
                imgLink.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, videoUrl.toUri()))
                }
            }
            if (evaContent.attachments.isNotEmpty()) {
                imgText.setImageResource(R.drawable.vijest_ind_txt_active)
                imgText.setOnClickListener {
                    startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW, evaContent.attachments[0]!!.url.toUri()),
                            "Otvaranje dokumenta " + evaContent.attachments[0]!!.name))
                }
            }

            loadingCircle.isGone = true

            this.evaContent = evaContent
        }
    }

    private fun fetchContentFromServer(contentId: Long) {
        fetchFromServerDisposable = novaEvaService.getContentData(contentId)
                .networkRequest({ contentDataDTO ->
                    EvaCache.cache(realm, contentDataDTO)
                }, onError = {
                    view?.snackbar(R.string.error_fetching_data, Snackbar.LENGTH_LONG)
                })
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

        EventPipelines.changeNavbarColor.onNext(R.color.Black)
        EventPipelines.changeStatusbarColor.onNext(R.color.VeryDarkGray)
        EventPipelines.changeFragmentBackgroundResource.onNext(R.color.White)

        val savedScrollPercent = savedInstanceState?.getFloat(SCROLL_PERCENT_KEY, 0f) ?: 0f
        if (savedScrollPercent > 0) {
            vijestWebView.afterLoadAndLayoutComplete {
                scrollView.scrollY = calcScrollYAbsolute(savedScrollPercent, scrollView.getChildAt(0).height)
            }
        }

        initUI()
    }

    private fun initUI() {
        val ctx = context ?: return

        loadingCircle.isVisible = true

        createIfMissingAndSubscribeToEvaContentUpdates()

        vijestWebView.applyEvaConfiguration(prefs)
        vijestWebView.loadHtmlText(evaContent?.text)

        /** Is this 'Duhovni poziv' or 'Odgovori' category?  */
        if (categoryId == EvaCategory.VOCATION.id.toLong()) {
            btnPoziv.isVisible = true
            btnPoziv.setOnClickListener {
                val text = "Hvaljen Isus i Marija, javljam Vam se jer razmišljam o duhovnom pozivu."
                sendEmailIntent(ctx, "Duhovni poziv", text, arrayOf("duhovnipoziv@gmail.com"))
            }
        }

        //todo move to options drawer
//        options.btnSearch.setOnClickListener {
//            showSearchPopup()
//        }
//        options.btnBookmark.setOnClickListener {
//            evaContent?.let { evaContent ->
//                val evaContentMetadata = evaContent.contentMetadata!!
//                EvaContentDbAdapter.updateEvaContentMetadataAsync(realm, evaContentMetadata.contentId) {
//                    it.bookmark = !it.bookmark
//                }
//            }
//        }
//
//        options.btnShare.setOnClickListener {
//            shareIntent(ctx, "http://novaeva.com/node/$contentId")
//        }
//
//        options.btnMail.setOnClickListener {
//            sendEmailIntent(ctx, evaContent!!.contentMetadata!!.title, "http://novaeva.com/node/$contentId")
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player_view?.player = null
        exoPlayer?.removeListener(evaPlayerEventListener)
        exoPlayer = null
    }

    private fun prepareAudioStream(context: Context, audioUri: String) {
        val dataSourceFactory = DefaultDataSourceFactory(context,
                Util.getUserAgent(context, resources.getString(R.string.app_name)),
                DefaultBandwidthMeter())

//        val factory = ExtractorMediaSource.Factory(dataSourceFactory).setCustomCacheKey(audioUri)
//        val mediaSource = factory.createMediaSource(streamingUri)

        exoPlayer = NovaEvaApp.evaPlayer.prepareIfNeededAndGetPlayer(audioUri) {
            ExtractorMediaSource(audioUri.toUri(), dataSourceFactory, DefaultExtractorsFactory(), handler, null, audioUri)
        }
    }

    override fun onDestroy() {
        realm.close()
        super.onDestroy()
    }
}