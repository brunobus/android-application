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
import hr.bpervan.novaeva.main.databinding.FragmentEvaContentBinding
import hr.bpervan.novaeva.model.EvaContent
import hr.bpervan.novaeva.model.OpenContentEvent
import hr.bpervan.novaeva.model.toDbModel
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

    private var _viewBinding: FragmentEvaContentBinding? = null
    private val viewBinding get() = _viewBinding!!

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

        viewBinding.evaCollapsingBar.collapsingToolbar.title = evaContent.title

        val coverImageInfo = evaContent.image
        val coverImageView = viewBinding.evaCollapsingBar.coverImage

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
        viewBinding.vijestWebView.loadHtmlText(evaContent.text)

        evaContent.audioURL?.let { audioUrl ->
            viewBinding.imgMp3.setImageResource(R.drawable.vijest_ind_mp3_active)

            NovaEvaApp.evaPlayer.prepareAudioStream(
                    audioUrl, evaContent.id.toString(),
                    evaContent.title,
                    isRadio = false,
                    doAutoPlay = false,
                    auth = null)

            viewBinding.playerView.apply {
                NovaEvaApp.evaPlayer.supplyPlayerToView(this, evaContent.id.toString())
                applyEvaConfiguration()
                requestFocus()
                showController()

            }
            viewBinding.imgMp3.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, audioUrl.toUri()))
            }
        }


        evaContent.videoURL?.let { videoUrl ->
            viewBinding.imgLink.setImageResource(R.drawable.vijest_ind_www_active)
            viewBinding.imgLink.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, videoUrl.toUri()))
            }
        }
        if (evaContent.attachments.isNotEmpty()) {
            viewBinding.imgText.setImageResource(R.drawable.vijest_ind_txt_active)
            viewBinding.imgText.setOnClickListener {
                startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW,
                        evaContent.attachments[0]!!.url.toUri()), evaContent.attachments[0]!!.name))
            }
        }

        viewBinding.loadingCircle.isGone = true

        when (enumValueOrNull<EvaDomain>(evaContent.domain)) {
            EvaDomain.VOCATION -> {
                viewBinding.btnPoziv.isVisible = true
                viewBinding.btnPoziv.setOnClickListener {
                    sendEmailIntent(context,
                            subject = getString(R.string.thinking_of_vocation),
                            text = getString(R.string.mail_preamble_praise_the_lord)
                                    + getString(R.string.mail_intro_vocation),
                            receiver = getString(R.string.vocation_email))
                }
            }
            EvaDomain.ANSWERS -> {
                viewBinding.btnPitanje.isVisible = true
                viewBinding.btnPitanje.setOnClickListener {
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

        viewBinding.vijestWebView.applyEvaConfiguration(prefs)
        viewBinding.vijestWebView.loadHtmlText(evaContent.text)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val newInflater = if (themeId != -1) inflater.cloneInContext(ContextThemeWrapper(activity, themeId)) else inflater
        _viewBinding = FragmentEvaContentBinding.inflate(newInflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.loadingCircle.isVisible = true

        EventPipelines.changeNavbarColor.onNext(R.color.Black)
        EventPipelines.changeStatusbarColor.onNext(R.color.VeryDarkGray)
        EventPipelines.changeFragmentBackgroundResource.onNext(R.color.White)

        disposables += EventPipelines.resizeText.subscribe {
            viewBinding.vijestWebView.applyConfiguredFontSize(prefs)
        }

        val evaContent = EvaContentDbAdapter.loadEvaContent(realm, contentId)

        if ((evaContent == null || savedInstanceState == null) && domain.isLegacy()) {
            fetchContentFromServer_legacy(contentId)
        } else {
            if (evaContent != null) {
                this.evaContent = evaContent
            } else {
                fetchContentFromServer(contentId)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        FirebaseAnalytics.getInstance(requireContext())
                .setCurrentScreen(requireActivity(), "Sadržaj '${initializer.title}'".take(36), "Content")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    override fun onDestroy() {
        realm.close()
        super.onDestroy()
    }

    private fun fetchContentFromServer_legacy(contentId: Long) {
        disposables += NovaEvaService.v2.getContentData(contentId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { contentDataDTO ->
                    contentDataDTO.domain = domain
                    EvaContentDbAdapter.addOrUpdateEvaContentAsync_legacy(realm, contentDataDTO) {
                        evaContent = EvaContentDbAdapter.loadEvaContent(realm, contentId)
                    }
                },
                onError = {
                    Log.e("fetchContentLegacy", it.message, it)
                    view?.dataErrorSnackbar()
                    //load old
                    evaContent = EvaContentDbAdapter.loadEvaContent(realm, contentId)
                })
    }

    private fun fetchContentFromServer(contentId: Long) {
        disposables += NovaEvaService.v3.content(domain.domainEndpoint, contentId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { contentDto ->
                    contentDto.domain = domain
                    evaContent = contentDto.toDbModel()
                },
                onError = {
                    Log.e("fetchContent", it.message, it)
                    view?.dataErrorSnackbar()
                }
            )
    }
}