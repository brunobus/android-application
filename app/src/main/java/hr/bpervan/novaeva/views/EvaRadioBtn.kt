package hr.bpervan.novaeva.views

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.main.databinding.RadioButtonBinding
import hr.bpervan.novaeva.views.EvaRadioBtn.RadioBtnState.BUFFERING
import hr.bpervan.novaeva.views.EvaRadioBtn.RadioBtnState.LOADING
import hr.bpervan.novaeva.views.EvaRadioBtn.RadioBtnState.NOT_PLAYING
import hr.bpervan.novaeva.views.EvaRadioBtn.RadioBtnState.PLAYING

/**
 *
 */
class EvaRadioBtn(context: Context?, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

//    private val viewBinding: RadioButtonBinding = RadioButtonBinding.inflate(LayoutInflater.from(context), this, true)

    enum class RadioBtnState {
        NOT_PLAYING, LOADING, BUFFERING, PLAYING
    }

    var radioBtnState: RadioBtnState = NOT_PLAYING
        set(value) {
            field = value

            val btnRadio = findViewById<View>(R.id.btnRadio)
            val btnRadioOptions = findViewById<View>(R.id.btnRadioOptions)
            val radioLoadingCircle = findViewById<View>(R.id.radioLoadingCircle)

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

        val btnRadio = findViewById<View>(R.id.btnRadio)
        val btnRadioOptions = findViewById<View>(R.id.btnRadioOptions)

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