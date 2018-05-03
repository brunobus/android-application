package hr.bpervan.novaeva.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import hr.bpervan.novaeva.main.R

/**
 *
 */
class EvaCategoryView(context: Context?, attrs: AttributeSet?) : TextView(context, attrs) {
    companion object {
        val STATE_INDICATE_NEWS = intArrayOf(R.attr.indicate_news)
    }

    var indicateNews: Boolean = false
        set(value) {
            field = value
            refreshDrawableState()
        }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (indicateNews) {
            View.mergeDrawableStates(drawableState, STATE_INDICATE_NEWS)
        }
        return drawableState
    }
}