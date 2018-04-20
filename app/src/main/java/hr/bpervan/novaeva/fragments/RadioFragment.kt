package hr.bpervan.novaeva.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.analytics.HitBuilders
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.adapters.RadioStationsAdapter
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.model.TEMP_RADIO_STATIONS
import hr.bpervan.novaeva.player.EvaPlayerEventListener
import io.reactivex.rxkotlin.plusAssign
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

    private lateinit var adapter: RadioStationsAdapter
    private var selectedRadioStation: String? = null

    private var exoPlayer: ExoPlayer? = null
    private val evaPlayerEventListener = EvaPlayerEventListener({ context }, { exoPlayer }, { selectedRadioStation })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = RadioStationsAdapter(TEMP_RADIO_STATIONS)

        savedInstanceState ?: NovaEvaApp.defaultTracker
                .send(HitBuilders.EventBuilder()
                        .setCategory("Radio")
                        .setAction("OtvorenRadioIzbornik")
                        .build())
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

        baseDisposables += EventPipelines.playRadioStream.subscribe { radioStation ->
            context?.let { context ->
                val streamUri = radioStation.streamUris.firstOrNull()
                if (streamUri != null) {
                    prepareAudioStream(context, streamUri, true)
                }
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

    private fun prepareAudioStream(context: Context, audioUri: String, playWhenReady: Boolean) {

        val streamingUri = Uri.parse(audioUri)

        val dataSourceFactory = DefaultDataSourceFactory(context,
                Util.getUserAgent(context, resources.getString(R.string.app_name)),
                DefaultBandwidthMeter())

//        val factory = ExtractorMediaSource.Factory(dataSourceFactory).setCustomCacheKey(audioUri)
//        val mediaSource = factory.createMediaSource(streamingUri)

        val exoPlayer = NovaEvaApp.evaPlayer.prepareIfNeededAndGetPlayer(audioUri) {
            ExtractorMediaSource(streamingUri, dataSourceFactory, DefaultExtractorsFactory(), Handler(), null, audioUri)
        }

        this.exoPlayer?.removeListener(evaPlayerEventListener)
        exoPlayer.removeListener(evaPlayerEventListener)
        exoPlayer.addListener(evaPlayerEventListener)
        this.exoPlayer = exoPlayer

        exoPlayer.playWhenReady = playWhenReady
    }
}