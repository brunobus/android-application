package hr.bpervan.novaeva.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.adapters.RadioStationsAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.EvaCategory
import hr.bpervan.novaeva.model.EvaContentMetadata
import hr.bpervan.novaeva.model.toDatabaseModel
import hr.bpervan.novaeva.player.EvaPlayerEventListener
import hr.bpervan.novaeva.services.novaEvaService
import hr.bpervan.novaeva.util.networkRequest
import hr.bpervan.novaeva.util.plusAssign
import hr.bpervan.novaeva.views.snackbar
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.collapsing_content_header.view.*
import kotlinx.android.synthetic.main.fragment_radio.*

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
    private var selectedRadioStation: String? = null

    private var exoPlayer: ExoPlayer? = null
    private val evaPlayerEventListener = EvaPlayerEventListener({ context }, { exoPlayer }, { selectedRadioStation })

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
        val ctw = ContextThemeWrapper(activity, R.style.RadioTheme)
        val localInflater = inflater.cloneInContext(ctw)
        return localInflater.inflate(R.layout.fragment_radio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        EventPipelines.changeNavbarColor.onNext(R.color.Black)
        EventPipelines.changeStatusbarColor.onNext(R.color.VeryDarkGray)
        EventPipelines.changeFragmentBackgroundResource.onNext(R.drawable.radio_background)

        baseDisposables += EventPipelines.chooseRadioStation.subscribe { radioStation ->
            context?.let { context ->
                fetchFromServerDisposable = novaEvaService.getContentData(radioStation.contentId).networkRequest({ radioStationDetailsDto ->
                    val radioStationDetails = radioStationDetailsDto.toDatabaseModel()

                    prepareRadioStream(context, radioStationDetails.audioURL!!, true)

                    val coverImageInfo = radioStationDetails.image
                    val coverImageView = collapsingRadioHeader.coverImage

                    if (coverImageInfo != null && coverImageView != null) {
                        imageLoader.displayImage(coverImageInfo.url, coverImageView)
                    }
                }, onError = {})
            }
        }

        initUI()
    }

    private fun initUI() {

        collapsingRadioHeader.collapsingToolbar.title = getString(R.string.radio_stations)

        val recyclerView = evaRecyclerView as RecyclerView
        val linearLayoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter

    }

    override fun onDestroyView() {
        super.onDestroyView()
        exoPlayer?.removeListener(evaPlayerEventListener)
        exoPlayer = null
    }

    private val handler = Handler()

    private fun prepareRadioStream(context: Context, playlistUri: String, playWhenReady: Boolean) {

        val dataSourceFactory = DefaultDataSourceFactory(context,
                Util.getUserAgent(context, resources.getString(R.string.app_name)),
                DefaultBandwidthMeter())

//        val factory = ExtractorMediaSource.Factory(dataSourceFactory).setCustomCacheKey(playlistUri)
//        val mediaSource = factory.createMediaSource(streamingUri)

        val exoPlayer = NovaEvaApp.evaPlayer.prepareIfNeededAndGetPlayer(playlistUri) {
            HlsMediaSource(playlistUri.toUri(), dataSourceFactory, handler, null)
        }

        this.exoPlayer?.removeListener(evaPlayerEventListener)
        exoPlayer.removeListener(evaPlayerEventListener)
        exoPlayer.addListener(evaPlayerEventListener)
        this.exoPlayer = exoPlayer

        exoPlayer.playWhenReady = playWhenReady
    }

    private fun fetchRadioStationsFromServer(timestamp: Long? = null) {
        fetchFromServerDisposable = novaEvaService.getDirectoryContent(EvaCategory.RADIO.id.toLong(), timestamp, 1000)
                .networkRequest({ evaDirectoryDTO ->
                    radioStationList.clear()
                    radioStationList.addAll(evaDirectoryDTO.contentMetadataList.map { it.toDatabaseModel() })
                    adapter.notifyDataSetChanged()
                }, onError = {
                    view?.snackbar(R.string.error_fetching_data, Snackbar.LENGTH_LONG)
                })
    }
}