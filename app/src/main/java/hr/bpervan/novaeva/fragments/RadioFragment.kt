package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
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
import hr.bpervan.novaeva.model.EvaCategory
import hr.bpervan.novaeva.model.EvaContent
import hr.bpervan.novaeva.model.EvaContentMetadata
import hr.bpervan.novaeva.model.toDatabaseModel
import hr.bpervan.novaeva.player.getStreamLinksFromPlaylistUri
import hr.bpervan.novaeva.player.prepareAudioStream
import hr.bpervan.novaeva.services.novaEvaService
import hr.bpervan.novaeva.util.networkRequest
import hr.bpervan.novaeva.util.plusAssign
import hr.bpervan.novaeva.views.snackbar
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
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

    private val radioStationList: MutableList<EvaContentMetadata> = mutableListOf()

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

        val recyclerView = evaRecyclerView as RecyclerView
        val linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter

        disposables += EventPipelines.chooseRadioStation
                .throttleWithTimeout(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .switchMapMaybe {
                    if (it.contentId.toString() == NovaEvaApp.evaPlayer.currentPlaybackInfo()?.id) {

                        NovaEvaApp.evaPlayer.stop()
                        Maybe.empty()
                    } else {
                        novaEvaService.getContentData(it.contentId).toMaybe()
                                .subscribeOn(Schedulers.io())
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { updateUI(it.toDatabaseModel()) }
                .observeOn(Schedulers.io())
                .switchMap { radioStation ->
                    getStreamLinksFromPlaylistUri(radioStation.audioURL!!)
                            .toObservable()
                            .map { Pair(it, radioStation) }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ stationStreams ->
                    val streamUris = stationStreams.first
                    val radioStation = stationStreams.second

                    for (streamUri in streamUris.shuffled()) {
                        try {
                            prepareAudioStream(streamUri, radioStation.contentId.toString(),
                                    radioStation.title ?: "nepoznato",
                                    isRadio = true, doAutoPlay = true)
                            break
                        } catch (e: Exception) {
                            /*continue*/
                        }
                    }
                }, { Log.e("radioError", it.message, it) })

        disposables += EventPipelines.playbackChanged.subscribe {
            if (it.player.playbackState != Player.STATE_BUFFERING) {
                adapter.radioStationPlaying = it.playbackInfo?.id?.toLongOrNull()
                adapter.notifyItemRangeChanged(0, adapter.itemCount)
            }
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
        fetchFromServerDisposable = novaEvaService.getDirectoryContent(EvaCategory.RADIO.id, null, 1000)
                .networkRequest({ evaDirectoryDTO ->
                    radioStationList.clear()
                    radioStationList.addAll(evaDirectoryDTO.contentMetadataList.map { it.toDatabaseModel() })
                    adapter.notifyDataSetChanged()
                }, onError = {
                    view?.snackbar(R.string.error_fetching_data, Snackbar.LENGTH_LONG)
                })
    }
}