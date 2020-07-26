package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.exoplayer2.Player
import com.google.firebase.analytics.FirebaseAnalytics
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.adapters.RadioStationsAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaContent
import hr.bpervan.novaeva.model.toDbModel
import hr.bpervan.novaeva.player.getStreamLinksFromPlaylist
import hr.bpervan.novaeva.rest.EvaDomain
import hr.bpervan.novaeva.rest.NovaEvaService
import hr.bpervan.novaeva.util.dataErrorSnackbar
import hr.bpervan.novaeva.util.plusAssign
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.collapsing_content_header.view.*
import kotlinx.android.synthetic.main.fragment_radio.*
import java.util.concurrent.TimeUnit


/**
 *
 */
class RadioFragment : EvaBaseFragment() {
    companion object : EvaFragmentFactory<RadioFragment, Unit> {

        override fun newInstance(initializer: Unit): RadioFragment {
            return RadioFragment()
        }
    }

    private var fetchFromServerDisposable: Disposable? = null
        set(value) {
            field = safeReplaceDisposable(field, value)
        }

    private lateinit var adapter: RadioStationsAdapter

    private val radioStationList: MutableList<EvaContent> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = RadioStationsAdapter(radioStationList)

        fetchRadioStationsFromServer()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.cloneInContext(ContextThemeWrapper(activity, R.style.RadioTheme))
                .inflate(R.layout.fragment_radio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EventPipelines.changeNavbarColor.onNext(R.color.Black)
        EventPipelines.changeStatusbarColor.onNext(R.color.VeryDarkGray)
        EventPipelines.changeFragmentBackgroundResource.onNext(R.color.Transparent)

        collapsingRadioHeader.collapsingToolbar.title = getString(R.string.radio_stations)

        val recyclerView = evaRecyclerView as androidx.recyclerview.widget.RecyclerView
        val linearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        recyclerView.adapter = adapter

        disposables += EventPipelines.chooseRadioStation
                .throttleWithTimeout(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapMaybe {
                    if (it.id == adapter.radioStationPlaying) {

                        NovaEvaApp.evaPlayer.stop()
                        Maybe.empty()
                    } else {
                        Maybe.just(it)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { updateUI(it) }
                .doOnNext {
                    FirebaseAnalytics.getInstance(requireContext())
                            .logEvent("RadioStationSelected", bundleOf(
                                    "title" to it.title.ifEmpty { it.audioTitle }
                            ))
                }
                .observeOn(Schedulers.io())
                .switchMap { radioStation ->
                    getStreamLinksFromPlaylist(radioStation.audioURL!!, radioStation.audioTitle!!)
                            .toObservable()
                            .map { Pair(radioStation, it) }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ stationStreams ->
                    val radioStation = stationStreams.first
                    val streamUris = stationStreams.second

                    for (streamUri in streamUris.shuffled()) {
                        try {
                            NovaEvaApp.evaPlayer.prepareAudioStream(
                                    streamUri, radioStation.id.toString(),
                                    radioStation.title.ifEmpty { "nepoznato" },
                                    isRadio = true,
                                    doAutoPlay = true,
                                    auth = null)
                            break
                        } catch (e: Exception) {
                            /*continue*/
                        }
                    }
                }, { Log.e("radioError", it.message, it) })

        disposables += EventPipelines.playbackStartStopPause
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it.player.playbackState == Player.STATE_READY
                            && it.player.playWhenReady
                            && it.playbackInfo?.isRadio == true) {
                        adapter.radioStationPlaying = it.playbackInfo.id.toLongOrNull()
                    } else {
                        adapter.radioStationPlaying = null
                    }
                    adapter.notifyItemRangeChanged(0, adapter.itemCount)
                }
    }

    override fun onResume() {
        super.onResume()

        FirebaseAnalytics.getInstance(requireContext())
                .setCurrentScreen(requireActivity(), "Radio", "Radio")
    }

    private fun updateUI(radioStationDetails: EvaContent) {
        val coverImageInfo = radioStationDetails.image
        val coverImageView = collapsingRadioHeader.coverImage

        if (coverImageInfo != null && coverImageView != null) {
            imageLoader.displayImage(coverImageInfo.url, coverImageView)
        }
    }

    private fun fetchRadioStationsFromServer() {
        fetchFromServerDisposable = NovaEvaService.v3.categoryContent(EvaDomain.RADIO.domainEndpoint, items = 1000)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onSuccess = { categoryDto ->
                    radioStationList.clear()
                    radioStationList.addAll(categoryDto.content!!.map { it.toDbModel() })
                    adapter.notifyDataSetChanged()
                }, onError = {
                    view?.dataErrorSnackbar()
                })
    }
}