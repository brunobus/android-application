package hr.bpervan.novaeva.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import hr.bpervan.novaeva.main.R

/**
 *
 */
class EvaBookmarkView(context: Context?, attrs: AttributeSet?) : TextView(context, attrs) {
    companion object {
        private val STATE_IS_BOOKMARKED = intArrayOf(R.attr.is_bookmarked)
    }

    var bookmarked: Boolean = false
        set(value) {
            field = value
            refreshDrawableState()

            text = resources.getString(if (value) R.string.unbookmark else R.string.bookmark)
        }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (bookmarked) {
            View.mergeDrawableStates(drawableState, STATE_IS_BOOKMARKED)
        }
        return drawableState
    }
}