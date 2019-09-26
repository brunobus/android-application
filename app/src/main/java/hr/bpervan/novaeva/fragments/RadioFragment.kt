package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.exoplayer2.Player
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.adapters.RadioStationsAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaContent
import hr.bpervan.novaeva.model.toDbModel
import hr.bpervan.novaeva.player.getStreamLinksFromPlaylistUri
import hr.bpervan.novaeva.rest.EvaDomain
import hr.bpervan.novaeva.rest.NovaEvaService
import hr.bpervan.novaeva.rest.serverByDomain
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
    companion object : EvaBaseFragment.EvaFragmentFactory<RadioFragment, Unit> {

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

        savedInstanceState ?: NovaEvaApp.defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("Radio")
                        .setAction("OtvorenRadioIzbornik")
                        .build())

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
                .switchMapMaybe {
                    if (it.id == adapter.radioStationPlaying) {

                        NovaEvaApp.evaPlayer.stop()
                        Maybe.empty()
                    } else {
                        NovaEvaService.v2.getContentData(it.id).toMaybe()
                                .subscribeOn(Schedulers.io())
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { updateUI(it.toDbModel()) }
                .observeOn(Schedulers.io())
                .switchMap { radioStation ->
                    getStreamLinksFromPlaylistUri(radioStation.audioURL!!)
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
                                    streamUri, radioStation.contentId.toString(),
                                    radioStation.title ?: "nepoznato",
                                    isRadio = true,
                                    doAutoPlay = true,
                                    auth = serverByDomain(EvaDomain.RADIO).auth)
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

    private fun updateUI(radioStationDetails: EvaContent) {
        val coverImageInfo = radioStationDetails.image
        val coverImageView = collapsingRadioHeader.coverImage

        if (coverImageInfo != null && coverImageView != null) {
            imageLoader.displayImage(coverImageInfo.url, coverImageView)
        }
    }

    private fun fetchRadioStationsFromServer() {
        fetchFromServerDisposable = NovaEvaService.v2.getDirectoryContent(EvaDomain.RADIO.rootId, null, 1000)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onSuccess = { evaDirectoryDTO ->
                    radioStationList.clear()
                    radioStationList.addAll(evaDirectoryDTO.contentMetadataList.map { it.toDbModel() })
                    adapter.notifyDataSetChanged()
                }, onError = {
                    view?.dataErrorSnackbar()
                })
    }
}