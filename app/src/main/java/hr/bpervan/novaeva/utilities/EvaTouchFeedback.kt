package hr.bpervan.novaeva.utilities

import android.graphics.PorterDuff
import android.view.MotionEvent
import android.view.View

class EvaTouchFeedback(val view: View, val touchColor: Int) : View.OnTouchListener {
    private val waitScrollTimeout = 200L
    private val afterClickReleaseTimeout = 200L

    private var cancelDelayedJob = false

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                view.postDelayed({
                    if (!cancelDelayedJob) {
                        setThemedColorFilter(view)
                    }
                }, waitScrollTimeout)
                cancelDelayedJob = false
            }
            MotionEvent.ACTION_CANCEL -> {
                clearColorFilter(view)
                cancelDelayedJob = true
            }
            MotionEvent.ACTION_UP -> {
                cancelDelayedJob = true
                setThemedColorFilter(view)
                view.postDelayed({
                    view.background.clearColorFilter()
                }, afterClickReleaseTimeout)
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