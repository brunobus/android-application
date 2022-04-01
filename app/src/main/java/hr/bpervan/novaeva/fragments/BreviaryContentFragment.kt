package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.main.databinding.FragmentSimpleContentBinding
import hr.bpervan.novaeva.rest.NovaEvaService
import hr.bpervan.novaeva.util.BREVIARY_IMAGE_KEY
import hr.bpervan.novaeva.util.dataErrorSnackbar
import hr.bpervan.novaeva.util.plusAssign
import hr.bpervan.novaeva.views.applyConfiguredFontSize
import hr.bpervan.novaeva.views.applyEvaConfiguration
import hr.bpervan.novaeva.views.loadHtmlText
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

/**
 * Created by vpriscan on 26.11.17..
 */
class BreviaryContentFragment : EvaBaseFragment() {

    companion object : EvaFragmentFactory<BreviaryContentFragment, Int> {

        private const val BREVIARY_ID_KEY = "breviaryId"
        override fun newInstance(initializer: Int): BreviaryContentFragment {
            return BreviaryContentFragment().apply {
                arguments = bundleOf(BREVIARY_ID_KEY to initializer)
            }
        }

        private var savedBreviaryText: String? = null
    }

    private var _viewBinding: FragmentSimpleContentBinding? = null
    private val viewBinding get() = _viewBinding!!

    private var breviaryId: Int = -1
    private lateinit var breviaryName: String
    private var breviaryText: String? = null
    private var coverImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            savedBreviaryText = null
        }

        val inState = savedInstanceState ?: arguments!!
        breviaryId = inState.getInt(BREVIARY_ID_KEY, 4)
        coverImageUrl = prefs.getString(BREVIARY_IMAGE_KEY, null)
        breviaryName = "Brevijar - " + when (breviaryId) {
            1 -> "Jučer, Jutarnja"
            2 -> "Jučer, Večernja"
            3 -> "Jučer, Povečerje"
            4 -> "Danas, Jutarnja"
            5 -> "Danas, Večernja"
            6 -> "Danas, Povečerje"
            7 -> "Sutra, Jutarnja"
            8 -> "Sutra, Večernja"
            else -> "Sutra, Povečerje"
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val newInflater = inflater.cloneInContext(ContextThemeWrapper(activity, R.style.BreviaryTheme))
        _viewBinding = FragmentSimpleContentBinding.inflate(newInflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EventPipelines.changeWindowBackgroundDrawable.onNext(NovaEvaApp.defaultBreviaryBackground)
        EventPipelines.changeNavbarColor.onNext(R.color.Transparent)
        EventPipelines.changeStatusbarColor.onNext(R.color.Transparent)
        EventPipelines.changeFragmentBackgroundResource.onNext(R.color.Transparent)

        if (savedInstanceState != null && savedBreviaryText != null) {
            breviaryText = savedBreviaryText
            showBreviary()
        } else {
            fetchBreviary()
        }

        disposables += EventPipelines.resizeText.subscribe {
            viewBinding.webView.applyConfiguredFontSize(prefs)
        }

        viewBinding.webView.applyEvaConfiguration(prefs)

        viewBinding.evaCollapsingBar.collapsingToolbar.title = breviaryName

        val coverImageView = viewBinding.evaCollapsingBar.coverImage

        if (coverImageUrl != null && coverImageView != null) {
            imageLoader.displayImage(coverImageUrl, coverImageView)
        }
    }

    override fun onResume() {
        super.onResume()

        FirebaseAnalytics.getInstance(requireContext())
                .setCurrentScreen(requireActivity(), breviaryName.take(36), "BreviaryContent")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(BREVIARY_ID_KEY, breviaryId)
        savedBreviaryText = breviaryText
        super.onSaveInstanceState(outState)
    }

    private fun fetchBreviary() {

        disposables += NovaEvaService.v2
                .getBreviary(breviaryId.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onSuccess = { breviary ->
                    view ?: return@subscribeBy
                    breviaryText = breviary.text ?: ""
                    showBreviary()
                }, onError = {
                    Log.e("breviaryError", it.message, it)
                    view?.dataErrorSnackbar()
                })
    }

    private fun showBreviary() {
        viewBinding.webView.loadHtmlText(breviaryText)
    }
}
