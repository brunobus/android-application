package hr.bpervan.novaeva.fragments

import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.adapters.RadioStationsAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaContent
import hr.bpervan.novaeva.model.EvaContentMetadata
import hr.bpervan.novaeva.model.toDatabaseModel
import hr.bpervan.novaeva.player.EvaPlayerEventListener
import hr.bpervan.novaeva.player.PlaylistExtractor
import hr.bpervan.novaeva.rest.novaEvaServiceV2
import hr.bpervan.novaeva.util.networkRequest
import hr.bpervan.novaeva.util.plusAssign
import hr.bpervan.novaeva.views.snackbar
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.collapsing_content_header.view.*
import kotlinx.android.synthetic.main.fragment_radio.*
import java.net.HttpURLConnection
import java.net.URL
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

    private var exoPlayer: ExoPlayer? = null
        set(value) {
            field?.removeListener(evaPlayerEventListener)
            field?.removeListener(radioPlayerEventListener)

            value?.addListener(evaPlayerEventListener)
            value?.addListener(radioPlayerEventListener)

            field = value
        }
    private val evaPlayerEventListener = EvaPlayerEventListener({ exoPlayer })
    private val radioPlayerEventListener = RadioPlayerEventListener()

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

        initUI()

        baseDisposables += EventPipelines.chooseRadioStation
                .throttleWithTimeout(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .switchMapMaybe {
                    if (evaPlayerEventListener.playbackId == it.contentId.toString()) {
                        evaPlayerEventListener.playbackId = null
                        exoPlayer?.playWhenReady = false
                        Maybe.empty()
                    } else {
                        novaEvaServiceV2.getContentData(it.contentId).toMaybe()
                                .subscribeOn(Schedulers.io())
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .map { it.toDatabaseModel() }
                .doOnNext { updateUI(it) }
                .observeOn(Schedulers.io())
                .switchMap { radioStation ->
                    getStreamLinksFromPlaylistUri(radioStation.audioURL!!)
                            .toObservable()
                            .map { Pair(radioStation.contentId, it) }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ stationStreams ->
                    val stationId = stationStreams.first
                    val streamUris = stationStreams.second

                    for (streamUri in streamUris.shuffled()) {
                        try {
                            prepareAndPlayRadioStream(stationId, streamUri)
                            break
                        } catch (e: Exception) {
                            /*continue*/
                        }
                    }
                }, { Log.e("radioError", it.message, it) })
    }

    private fun updateUI(radioStationDetails: EvaContent) {
        val coverImageInfo = radioStationDetails.image
        val coverImageView = collapsingRadioHeader.coverImage

        if (coverImageInfo != null && coverImageView != null) {
            imageLoader.displayImage(coverImageInfo.url, coverImageView)
        }
    }

    private fun initUI() {

        collapsingRadioHeader.collapsingToolbar.title = getString(R.string.radio_stations)

        val recyclerView = evaRecyclerView as RecyclerView
        val linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter

    }

    inner class RadioPlayerEventListener : Player.DefaultEventListener() {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            adapter.radioStationPlaying = evaPlayerEventListener.playbackId?.toLong()
            adapter.notifyItemRangeChanged(0, adapter.itemCount)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exoPlayer = null
    }

    private val handler = Handler()

    private fun getStreamLinksFromPlaylistUri(playlistFileUri: String): Single<List<String>> {
        return Single
                .create<List<String>> { emitter ->
                    val url = URL(playlistFileUri)
                    val httpConnection = url.openConnection() as HttpURLConnection

                    try {
                        if (httpConnection.responseCode == HttpURLConnection.HTTP_OK) {
                            httpConnection.inputStream.bufferedReader().useLines {
                                val streamUris = PlaylistExtractor.extractStreamLinksFromPlaylist(
                                        it.toList(), playlistFileUri)
                                emitter.onSuccess(streamUris)
                            }
                        } else throw RuntimeException("Http error ${httpConnection.responseCode} for $playlistFileUri")
                    } finally {
                        httpConnection.disconnect()
                    }
                }
    }

    private fun prepareAndPlayRadioStream(stationId: Long, streamUri: String) {
        evaPlayerEventListener.playbackId = stationId.toString()

        val context = context ?: return
        val dataSourceFactory = DefaultDataSourceFactory(context,
                Util.getUserAgent(context, resources.getString(R.string.app_name)),
                DefaultBandwidthMeter())

//        val factory = ExtractorMediaSource.Factory(dataSourceFactory).setCustomCacheKey(streamUri)
//        val mediaSource = factory.createMediaSource(streamingUri)

        exoPlayer = NovaEvaApp.evaPlayer.prepareIfNeededAndGetPlayer(stationId.toString()) {
            ExtractorMediaSource(streamUri.toUri(), dataSourceFactory, DefaultExtractorsFactory(),
                    handler, null, streamUri)
        }
        exoPlayer?.playWhenReady = true
    }

    //legacy
    private val radioDirectoryId: Long = 473

    private fun fetchRadioStationsFromServer(timestamp: Long? = null) {
        fetchFromServerDisposable = novaEvaServiceV2.getDirectoryContent(radioDirectoryId, timestamp, 1000)
                .networkRequest({ evaDirectoryDTO ->
                    radioStationList.clear()
                    radioStationList.addAll(evaDirectoryDTO.contentMetadataList.map { it.toDatabaseModel() })
                    adapter.notifyDataSetChanged()
                }, onError = {
                    view?.snackbar(R.string.error_fetching_data, Snackbar.LENGTH_LONG)
                })
    }
}