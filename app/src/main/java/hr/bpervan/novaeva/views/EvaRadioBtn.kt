package hr.bpervan.novaeva.views

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.isVisible
import hr.bpervan.novaeva.EventPipelines
import hr.bpervan.novaeva.NovaEvaApp
import hr.bpervan.novaeva.main.R
import kotlinx.android.synthetic.main.radio_button.view.*

/**
 *
 */
class EvaRadioBtn(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    var expanded: Boolean = false
        set(expand) {
            field = expand
            btnRadioOptions.isVisible = expand
            btnRadio.setBackgroundResource(if (expand) R.drawable.button_radio_2 else R.drawable.button_radio_1)
        }

    var loadingHandler = Handler() //todo

    fun initialize() {
        expanded = false

        btnRadio.setOnClickListener {
            if (expanded) {
                NovaEvaApp.evaPlayer.stop()
            } else {
                EventPipelines.playAnyRadioStation.onNext(Unit)
            }
        }
        btnRadioOptions.setOnClickListener {
            EventPipelines.openRadio.onNext(Unit)
        }
    }
}