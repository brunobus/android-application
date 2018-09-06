package hr.bpervan.novaeva.fragments

import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaCategory
import hr.bpervan.novaeva.model.EvaContent
import hr.bpervan.novaeva.model.OpenContentEvent
import hr.bpervan.novaeva.model.toDatabaseModel
import hr.bpervan.novaeva.services.novaEvaService
import hr.bpervan.novaeva.storage.EvaContentDbAdapter
import hr.bpervan.novaeva.storage.RealmConfigProvider
import hr.bpervan.novaeva.util.*
import hr.bpervan.novaeva.views.*
import io.realm.Realm
import kotlinx.android.synthetic.main.collapsing_content_header.view.*
import kotlinx.android.synthetic.main.fragment_eva_content.*

/**
 * Created by vpriscan on 04.12.17..
 */
class EvaContentFragment : EvaBaseFragment() {

    companion object : EvaFragmentFactory<EvaContentFragment, OpenContentEvent> {

        private const val CONTENT_ID_KEY = "contentId"
        private const val THEME_ID_KEY = "themeId"

        override fun newInstance(initializer: OpenContentEvent): EvaContentFragment {
            return EvaContentFragment().apply {
                arguments = bundleOf(
                        CONTENT_ID_KEY to initializer.contentMetadata.contentId,
                        THEME_ID_KEY to initializer.themeId
                )
            }
        }
    }

    private lateinit var realm: Realm

    public var contentId: Long = 0
    private var evaContent: EvaContent? = null
        set(value) {
            field = value
            if (value != null) {
                updateUI(value)
            }
        }

    private var themeId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inState = savedInstanceState ?: arguments!!
        contentId = inState.getLong(CONTENT_ID_KEY)
        themeId = inState.getInt(THEME_ID_KEY, -1)

        savedInstanceState ?: NovaEvaApp.defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("Vijesti")
                        .setAction("OtvorenaVijest")
                        .setLabel(contentId.toString())
                        .setValue(contentId)
                        .build())

        realm = Realm.getInstance(RealmConfigProvider.evaDBConfig)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(CONTENT_ID_KEY, contentId)
        outState.putInt(THEME_ID_KEY, themeId)

        super.onSaveInstanceState(outState)
    }

    private fun updateUI(evaContent: EvaContent) {

        view ?: return

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

            NovaEvaApp.evaPlayer.prepareAudioStream(
                    audioUrl, evaContent.contentId.toString(),
                    evaContent.contentMetadata?.title ?: "nepoznato",
                    isRadio = false, doAutoPlay = false)

            player_view?.apply {
                NovaEvaApp.evaPlayer.supplyPlayerToView(this, evaContent.contentId.toString())
                applyEvaConfiguration()
                requestFocus()
                showController()

            }
            imgMp3.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, audioUrl.toUri()))
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
                startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW,
                        evaContent.attachments[0]!!.url.toUri()), evaContent.attachments[0]!!.name))
            }
        }

        loadingCircle.isGone = true

        when (evaContent.contentMetadata?.categoryId) {
            EvaCategory.VOCATION.id -> {
                btnPoziv.isVisible = true
                btnPoziv.setOnClickListener {
                    val text = "Hvaljen Isus i Marija, javljam Vam se jer razmišljam o duhovnom pozivu."
                    sendEmailIntent(context, "Duhovni poziv", text, arrayOf("duhovnipoziv@gmail.com"))
                }
            }
            EvaCategory.ANSWERS.id -> {
                btnPitanje.isVisible = true
                btnPitanje.setOnClickListener {
                    val text = "Hvaljen Isus!"
                    sendEmailIntent(context, "Imam pitanje", text, arrayOf("novaevangelizacija@gmail.com"))
                }
            }
        }

        vijestWebView.applyEvaConfiguration(prefs)
        vijestWebView.loadHtmlText(evaContent.text)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val inflaterToUse =
                if (themeId != -1) inflater.cloneInContext(ContextThemeWrapper(activity, themeId))
                else inflater

        return inflaterToUse.inflate(R.layout.fragment_eva_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingCircle.isVisible = true

        EventPipelines.changeNavbarColor.onNext(R.color.Black)
        EventPipelines.changeStatusbarColor.onNext(R.color.VeryDarkGray)
        EventPipelines.changeFragmentBackgroundResource.onNext(R.color.White)

        disposables += EventPipelines.resizeText.subscribe {
            vijestWebView?.applyConfiguredFontSize(prefs)
        }

        val evaContent = EvaContentDbAdapter.loadEvaContent(realm, contentId)

        if (evaContent == null || savedInstanceState == null) {
            fetchContentFromServer(contentId)
        } else {
            this.evaContent = evaContent
        }
    }

    override fun onDestroy() {
        realm.close()
        super.onDestroy()
    }

    private fun fetchContentFromServer(contentId: Long) {
        disposables += novaEvaService.getContentData(contentId)
                .networkRequest({ contentDataDTO ->
                    EvaContentDbAdapter.addOrUpdateEvaContentAsync(realm, contentDataDTO.toDatabaseModel()) {
                        evaContent = EvaContentDbAdapter.loadEvaContent(realm, contentId)
                    }
                }) {
                    view?.dataErrorSnackbar()
                    //load old
                    evaContent = EvaContentDbAdapter.loadEvaContent(realm, contentId)
                }
    }
}