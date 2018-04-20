package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.OpenBreviaryContentEvent
import hr.bpervan.novaeva.utilities.TransitionAnimation
import kotlinx.android.synthetic.main.fragment_breviary_chooser.*
import kotlinx.android.synthetic.main.collapsing_breviary_header.*
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 */
class BreviaryChooserFragment : EvaBaseFragment() {

    companion object : EvaFragmentFactory<BreviaryChooserFragment, Unit> {
        override fun newInstance(initializer: Unit): BreviaryChooserFragment {
            return BreviaryChooserFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedInstanceState ?: NovaEvaApp.defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("Brevijar")
                        .setAction("OtvorenBrevijarIzbornik")
                        .build())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val ctw = ContextThemeWrapper(activity, R.style.BreviaryTheme)
        val localInflater = inflater.cloneInContext(ctw)
        return localInflater.inflate(R.layout.fragment_breviary_chooser, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EventPipelines.changeWindowBackgroundDrawable.onNext(NovaEvaApp.defaultBreviaryBackground)
        EventPipelines.changeNavbarColor.onNext(R.color.Transparent)
        EventPipelines.changeStatusbarColor.onNext(R.color.Transparent)
        EventPipelines.changeFragmentBackgroundResource.onNext(R.color.Transparent)

        initUI()
    }

    private fun initUI() {

        val openSansRegular = NovaEvaApp.openSansRegular
        if (openSansRegular != null) {
            txtKs.typeface = openSansRegular
            txtLaudato.typeface = openSansRegular
        }

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("hr", "HR"))
        imgDanas.text = dateFormat.format(Date())

        val headerUrl = prefs.getString("hr.bpervan.novaeva.brevijarheaderimage", null)

        if (headerUrl != null && breviaryCoverImage != null) {
            imageLoader.displayImage(headerUrl, breviaryCoverImage)
        }

        btnJucerJutarnja.setOnClickListener(BreviaryClickListener(1))
        btnJucerVecernja.setOnClickListener(BreviaryClickListener(2))
        btnJucerPovecerje.setOnClickListener(BreviaryClickListener(3))

        btnDanasJutarnja.setOnClickListener(BreviaryClickListener(4))
        btnDanasVecernja.setOnClickListener(BreviaryClickListener(5))
        btnDanasPovecerje.setOnClickListener(BreviaryClickListener(6))

        btnSutraJutarnja.setOnClickListener(BreviaryClickListener(7))
        btnSutraVecernja.setOnClickListener(BreviaryClickListener(8))
        btnSutraPovecerje.setOnClickListener(BreviaryClickListener(9))
    }

    inner class BreviaryClickListener(private val breviaryId: Int) : View.OnClickListener {

        override fun onClick(v: View?) {
            EventPipelines.openBreviaryContent.onNext(OpenBreviaryContentEvent(breviaryId, TransitionAnimation.FADE))
        }
    }
}