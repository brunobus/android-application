package hr.bpervan.novaeva.utilities

/**
 *
 */

import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent

open class SwipeGestureListener : SimpleOnGestureListener() {

    open fun onSwipeLeft(): Boolean = false

    open fun onSwipeRight(): Boolean = false

    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val distanceX = e2.x - e1.x
        val distanceY = e2.y - e1.y
        return if (Math.abs(distanceX) > Math.abs(distanceY) &&
                Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD
                && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

            if (distanceX > 0)
                onSwipeRight()
            else
                onSwipeLeft()
        } else false
    }

    companion object {
        private const val SWIPE_DISTANCE_THRESHOLD = 300
        private const val SWIPE_VELOCITY_THRESHOLD = 400
    }
}