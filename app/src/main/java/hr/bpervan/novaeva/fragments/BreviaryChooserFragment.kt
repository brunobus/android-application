package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.analytics.FirebaseAnalytics
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.main.databinding.FragmentBreviaryChooserBinding
import hr.bpervan.novaeva.model.OpenBreviaryContentEvent
import hr.bpervan.novaeva.util.BREVIARY_IMAGE_KEY
import hr.bpervan.novaeva.util.TransitionAnimation
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 */
class BreviaryChooserFragment : EvaBaseFragment() {

    private var _viewBinding: FragmentBreviaryChooserBinding? = null
    private val viewBinding get() = _viewBinding!!

    companion object : EvaFragmentFactory<BreviaryChooserFragment, Unit> {
        override fun newInstance(initializer: Unit): BreviaryChooserFragment {
            return BreviaryChooserFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val newInflater = inflater.cloneInContext(ContextThemeWrapper(activity, R.style.BreviaryTheme))
        _viewBinding = FragmentBreviaryChooserBinding.inflate(newInflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EventPipelines.changeWindowBackgroundDrawable.onNext(NovaEvaApp.defaultBreviaryBackground)
        EventPipelines.changeNavbarColor.onNext(R.color.Transparent)
        EventPipelines.changeStatusbarColor.onNext(R.color.Transparent)
        EventPipelines.changeFragmentBackgroundResource.onNext(R.color.Transparent)

        val openSansRegular = NovaEvaApp.openSansRegular
        if (openSansRegular != null) {
            viewBinding.txtKs.typeface = openSansRegular
            viewBinding.txtLaudato.typeface = openSansRegular
        }

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("hr", "HR"))
        viewBinding.imgDanas.text = dateFormat.format(Date())

        val headerUrl = prefs.getString(BREVIARY_IMAGE_KEY, null)

        if (headerUrl != null) {
            imageLoader.displayImage(headerUrl, viewBinding.collapsingBreviaryHeader.breviaryCoverImage)
        }

        viewBinding.btnJucerJutarnja.setOnClickListener(BreviaryClickListener(1))
        viewBinding.btnJucerVecernja.setOnClickListener(BreviaryClickListener(2))
        viewBinding.btnJucerPovecerje.setOnClickListener(BreviaryClickListener(3))

        viewBinding.btnDanasJutarnja.setOnClickListener(BreviaryClickListener(4))
        viewBinding.btnDanasVecernja.setOnClickListener(BreviaryClickListener(5))
        viewBinding.btnDanasPovecerje.setOnClickListener(BreviaryClickListener(6))

        viewBinding.btnSutraJutarnja.setOnClickListener(BreviaryClickListener(7))
        viewBinding.btnSutraVecernja.setOnClickListener(BreviaryClickListener(8))
        viewBinding.btnSutraPovecerje.setOnClickListener(BreviaryClickListener(9))
    }

    override fun onResume() {
        super.onResume()

        FirebaseAnalytics.getInstance(requireContext())
                .setCurrentScreen(requireActivity(), "Brevijar izbornik", "BreviaryChooser")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    inner class BreviaryClickListener(private val breviaryId: Int) : View.OnClickListener {

        override fun onClick(v: View?) {
            EventPipelines.openBreviaryContent.onNext(OpenBreviaryContentEvent(breviaryId, TransitionAnimation.FADE))
        }
    }
}