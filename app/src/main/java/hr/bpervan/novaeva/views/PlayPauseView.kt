package hr.bpervan.novaeva.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import hr.bpervan.novaeva.main.R

/**
 *
 */
class PlayPauseView(context: Context?, attrs: AttributeSet?) : ImageView(context, attrs) {
    companion object {
        private val STATE_IS_PLAYING = intArrayOf(R.attr.is_playing)
    }

    var isPlaying: Boolean = false
        set(value) {
            field = value
            refreshDrawableState()
        }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (isPlaying) {
            View.mergeDrawableStates(drawableState, STATE_IS_PLAYING)
        }
        return drawableState
    }
}