package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.RxEventBus
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.BackgroundReplaceEvent
import hr.bpervan.novaeva.model.BackgroundType
import hr.bpervan.novaeva.services.NovaEvaService
import hr.bpervan.novaeva.utilities.ImageLoaderConfigurator
import hr.bpervan.novaeva.utilities.subscribeAsync
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.collapsing_content_header.view.*
import kotlinx.android.synthetic.main.eva_simple_content.*

/**
 * Created by vpriscan on 26.11.17..
 */
class BreviaryContentFragment : EvaBaseFragment() {

    companion object : EvaFragmentFactory<BreviaryContentFragment, Int> {

        private const val BREVIARY_ID_KEY = "breviaryId"
        override fun newInstance(initializer: Int): BreviaryContentFragment {
            return BreviaryContentFragment().apply {
                arguments = Bundle().apply {
                    putInt(BREVIARY_ID_KEY, initializer)
                }
            }
        }
    }

    private var breviaryId: Int = -1
    private lateinit var breviaryName: String
    private var coverImageUrl: String? = null

    private var breviaryText: String? = null

    private var loadBreviaryDisposable: Disposable? = null
        set(value) {
            field = safeReplaceDisposable(field, value)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inState = savedInstanceState ?: arguments!!
        breviaryId = inState.getInt(BREVIARY_ID_KEY, 4)
        coverImageUrl = prefs.getString("hr.bpervan.novaeva.brevijarheaderimage", null)

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

        savedInstanceState ?: savedInstanceState ?: NovaEvaApp.defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("Brevijar")
                        .setAction("OtvorenBrevijar")
                        .setLabel(breviaryName)
                        .setValue(breviaryId.toLong())
                        .build())

        fetchBreviary()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val ctw = ContextThemeWrapper(activity, R.style.BreviaryTheme)
        val localInflater = inflater.cloneInContext(ctw)
        return localInflater.inflate(R.layout.eva_simple_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        RxEventBus.appBackground.onNext(BackgroundReplaceEvent(R.drawable.brevijar_backbrevijar, BackgroundType.DRAWABLE))
        RxEventBus.navigationAndStatusBarColor.onNext(R.color.Transparent)

        initUI(savedInstanceState)
    }

    private fun initUI(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            webView.apply {
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                setOnLongClickListener { true }
                isLongClickable = false
            }
            if (breviaryText != null) {
                loadBreviary()
            }
        }

        evaCollapsingBar.collapsingToolbar.title = breviaryName

        val coverImageView = evaCollapsingBar.coverImage

        if (coverImageUrl != null && coverImageView != null) {
            imageLoader.displayImage(coverImageUrl, coverImageView, ImageLoaderConfigurator.createDefaultDisplayImageOptions(true))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(BREVIARY_ID_KEY, breviaryId)

        webView.saveState(outState)

        super.onSaveInstanceState(outState)
    }

    private fun fetchBreviary() {
        Log.d("fetchingBreviary", "fetching breviary: " + breviaryId)


        loadBreviaryDisposable = NovaEvaService.instance
                .getBreviary(breviaryId.toString())
                .subscribeAsync({ breviary ->
                    view ?: return@subscribeAsync

                    breviaryText = breviary.text
                    loadBreviary()
                }) {
                    context?.let { ctx ->
                        NovaEvaApp.showFetchErrorSnackbar(it, ctx, view)
                    }
                }
    }

    private fun loadBreviary() {
        webView.loadDataWithBaseURL(null, breviaryText, "text/html", "utf-8", "")
    }
}
