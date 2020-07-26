package hr.bpervan.novaeva.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.firebase.analytics.FirebaseAnalytics
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaContent
import hr.bpervan.novaeva.model.OpenContentEvent
import hr.bpervan.novaeva.rest.EvaDomain
import hr.bpervan.novaeva.rest.NovaEvaService
import hr.bpervan.novaeva.storage.EvaContentDbAdapter
import hr.bpervan.novaeva.storage.RealmConfigProvider
import hr.bpervan.novaeva.util.dataErrorSnackbar
import hr.bpervan.novaeva.util.enumValueOrNull
import hr.bpervan.novaeva.util.plusAssign
import hr.bpervan.novaeva.util.sendEmailIntent
import hr.bpervan.novaeva.views.applyConfiguredFontSize
import hr.bpervan.novaeva.views.applyEvaConfiguration
import hr.bpervan.novaeva.views.loadHtmlText
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import kotlinx.android.synthetic.main.collapsing_content_header.view.*
import kotlinx.android.synthetic.main.fragment_eva_content.*

/**
 * Created by vpriscan on 04.12.17..
 */
class EvaContentFragment : EvaBaseFragment() {

    companion object : EvaFragmentFactory<EvaContentFragment, OpenContentEvent> {

        override fun newInstance(initializer: OpenContentEvent): EvaContentFragment {
            return EvaContentFragment().apply {
                arguments = bundleOf(
                        EvaFragmentFactory.INITIALIZER to initializer
                )
            }
        }
    }

    private lateinit var realm: Realm

    private lateinit var initializer: OpenContentEvent
    private lateinit var domain: EvaDomain
    private var themeId = -1
    public var contentId: Long = 0

    private var evaContent: EvaContent? = null
        set(value) {
            field = value
            if (value != null) {
                updateUI(value)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inState = savedInstanceState ?: arguments!!

        initializer = inState.getParcelable(EvaFragmentFactory.INITIALIZER)!!

        domain = initializer.domain
        contentId = initializer.contentId
        themeId = initializer.theme

        realm = Realm.getInstance(RealmConfigProvider.evaDBConfig)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(EvaFragmentFactory.INITIALIZER, initializer)
        super.onSaveInstanceState(outState)
    }

    private fun updateUI(evaContent: EvaContent) {

        view ?: return

        evaCollapsingBar.collapsingToolbar.title = evaContent.title

        val coverImageInfo = evaContent.image
        val coverImageView = evaCollapsingBar.coverImage

        if (coverImageInfo != null) {
            if (coverImageView != null) {
                imageLoader.displayImage(coverImageInfo.url, coverImageView)
            }
        } else {
            val url = prefs.getString("hr.bpervan.novaeva.categoryheader." + evaContent.domain, null)
            if (url != null && coverImageView != null) {
                imageLoader.displayImage(url, coverImageView)
            }
        }
        vijestWebView.loadHtmlText(evaContent.text)

        evaContent.audioURL?.let { audioUrl ->
            imgMp3.setImageResource(R.drawable.vijest_ind_mp3_active)

            NovaEvaApp.evaPlayer.prepareAudioStream(
                    audioUrl, evaContent.id.toString(),
                    evaContent.title,
                    isRadio = false,
                    doAutoPlay = false,
                    auth = null)

            player_view?.apply {
                NovaEvaApp.evaPlayer.supplyPlayerToView(this, evaContent.id.toString())
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

        when (enumValueOrNull<EvaDomain>(evaContent.domain)) {
            EvaDomain.VOCATION -> {
                btnPoziv.isVisible = true
                btnPoziv.setOnClickListener {
                    sendEmailIntent(context,
                            subject = getString(R.string.thinking_of_vocation),
                            text = "Hvaljen Isus i Marija, javljam vam se jer razmišljam o duhovnom pozivu.",
                            receiver = getString(R.string.vocation_email))
                }
            }
            EvaDomain.ANSWERS -> {
                btnPitanje.isVisible = true
                btnPitanje.setOnClickListener {
                    sendEmailIntent(context,
                            subject = getString(R.string.having_a_question),
                            text = getString(R.string.mail_preamble_praise_the_lord),
                            receiver = getString(R.string.answers_email))
                }
            }
            else -> {
                /*nothing*/
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

        if ((evaContent == null || savedInstanceState == null) && domain.isLegacy()) {
            fetchContentFromServer_legacy(contentId)
        } else {
            this.evaContent = evaContent
        }
    }

    override fun onResume() {
        super.onResume()

        FirebaseAnalytics.getInstance(requireContext())
                .setCurrentScreen(requireActivity(), "Sadržaj '${initializer.title}'".take(36), "Content")
    }

    override fun onDestroy() {
        realm.close()
        super.onDestroy()
    }

    private fun fetchContentFromServer_legacy(contentId: Long) {
        disposables += NovaEvaService.v2.getContentData(contentId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onSuccess = { contentDataDTO ->
                    contentDataDTO.domain = domain
                    EvaContentDbAdapter.addOrUpdateEvaContentAsync_legacy(realm, contentDataDTO) {
                        evaContent = EvaContentDbAdapter.loadEvaContent(realm, contentId)
                    }
                }, onError = {
                    Log.e("fetchContentLegacy", it.message, it)
                    view?.dataErrorSnackbar()
                    //load old
                    evaContent = EvaContentDbAdapter.loadEvaContent(realm, contentId)
                })
    }
}