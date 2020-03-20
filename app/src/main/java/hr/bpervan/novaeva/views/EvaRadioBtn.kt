package hr.bpervan.novaeva.views

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.views.EvaRadioBtn.RadioBtnState.*
import kotlinx.android.synthetic.main.radio_button.view.*

/**
 *
 */
class EvaRadioBtn(context: Context?, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    enum class RadioBtnState {
        NOT_PLAYING, LOADING, BUFFERING, PLAYING
    }

    var radioBtnState: RadioBtnState = NOT_PLAYING
        set(value) {
            field = value
            btnRadio.setBackgroundResource(when (value) {
                NOT_PLAYING -> R.drawable.button_radio_1
                PLAYING -> R.drawable.button_radio_2
                LOADING, BUFFERING -> R.drawable.button_radio_3
            })
            btnRadioOptions.isVisible = value == PLAYING
            radioLoadingCircle.isVisible = value == LOADING || value == BUFFERING
        }

    private var loadingHandler = Handler()

    private val reapplyCollapsedState: () -> Unit = {
        if (radioBtnState == LOADING) {
            radioBtnState = NOT_PLAYING
        }
    }

    fun initialize() {
        radioBtnState = NOT_PLAYING

        btnRadio.setOnClickListener {
            when (radioBtnState) {
                NOT_PLAYING -> {
                    loadingHandler.removeCallbacks(reapplyCollapsedState)
                    radioBtnState = LOADING
                    loadingHandler.postDelayed(reapplyCollapsedState, 5000)
                    EventPipelines.playAnyRadioStation.onNext(Unit)
                }
                PLAYING -> NovaEvaApp.evaPlayer.stop()
                else -> {
                    /*nothing*/
                }
            }
        }

        btnRadioOptions.setOnClickListener {
            EventPipelines.openRadio.onNext(Unit)
        }
    }
}