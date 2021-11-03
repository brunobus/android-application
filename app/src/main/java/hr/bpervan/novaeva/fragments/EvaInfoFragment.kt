package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.analytics.FirebaseAnalytics
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.BuildConfig
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.main.databinding.FragmentSimpleContentBinding
import hr.bpervan.novaeva.util.plusAssign
import hr.bpervan.novaeva.views.applyConfiguredFontSize
import hr.bpervan.novaeva.views.applyEvaConfiguration

/**
 *
 */
class EvaInfoFragment : EvaBaseFragment() {
    companion object : EvaFragmentFactory<EvaInfoFragment, Unit> {

        override fun newInstance(initializer: Unit): EvaInfoFragment {
            return EvaInfoFragment()
        }
    }

    private var _viewBinding: FragmentSimpleContentBinding? = null
    private val viewBinding get() = _viewBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val newInflater = inflater.cloneInContext(ContextThemeWrapper(activity, R.style.AppTheme));
        _viewBinding = FragmentSimpleContentBinding.inflate(newInflater, container, false);
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EventPipelines.changeNavbarColor.onNext(R.color.Black)
        EventPipelines.changeStatusbarColor.onNext(R.color.VeryDarkGray)
        EventPipelines.changeFragmentBackgroundResource.onNext(R.color.White)

        disposables += EventPipelines.resizeText.subscribe {
            viewBinding.webView.applyConfiguredFontSize(prefs)
        }

        viewBinding.webView.applyEvaConfiguration(prefs)

        viewBinding.webView.loadUrl("file:///android_asset/info.html")

        val titleText = "${getString(R.string.app_name)} - v${BuildConfig.VERSION_NAME}" +
                if (BuildConfig.DEBUG) " (debug)" else ""

        viewBinding.evaCollapsingBar.collapsingToolbar.title = titleText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    override fun onResume() {
        super.onResume()

        FirebaseAnalytics.getInstance(requireContext())
                .setCurrentScreen(requireActivity(), "Informacije", "Information")
    }
}