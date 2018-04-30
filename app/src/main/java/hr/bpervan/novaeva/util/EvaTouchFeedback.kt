package hr.bpervan.novaeva.util

import android.graphics.PorterDuff
import android.view.MotionEvent
import android.view.View
import androidx.core.view.postDelayed

class EvaTouchFeedback(val view: View, val touchColor: Int) : View.OnTouchListener {
    private val waitScrollTimeout = 200L
    private val afterClickReleaseTimeout = 200L

    private var cancelDelayedJob = false

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                view.postDelayed(waitScrollTimeout) {
                    if (!cancelDelayedJob) {
                        setThemedColorFilter(view)
                    }
                }
                cancelDelayedJob = false
            }
            MotionEvent.ACTION_CANCEL -> {
                clearColorFilter(view)
                cancelDelayedJob = true
            }
            MotionEvent.ACTION_UP -> {
                cancelDelayedJob = true
                setThemedColorFilter(view)
                view.postDelayed(afterClickReleaseTimeout) {
                    view.background.clearColorFilter()
                }
                view.performClick()
            }
        }
        return true
    }

    private fun clearColorFilter(view: View) {
        view.background.clearColorFilter()
    }

    private fun setThemedColorFilter(view: View) {
        view.background.setColorFilter(touchColor, PorterDuff.Mode.DARKEN)
    }
}