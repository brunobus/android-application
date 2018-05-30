package hr.bpervan.novaeva.util

/**
 *
 */

import android.util.DisplayMetrics
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent

open class SwipeGestureListener(private val displayMetrics: DisplayMetrics) : SimpleOnGestureListener() {

    open fun onSwipeLeft(): Boolean = false

    open fun onSwipeRight(): Boolean = false

    override fun onDown(e: MotionEvent): Boolean = false

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val distanceX = e2.x - e1.x
        val distanceY = e2.y - e1.y
        return if (Math.abs(distanceX) > Math.abs(distanceY) &&
                Math.abs(distanceX) > SWIPE_DISTANCE_MULTIPLIER * displayMetrics.widthPixels
                && Math.abs(velocityX) > SWIPE_VELOCITY_MULTIPLIER * displayMetrics.widthPixels) {

            if (distanceX > 0) onSwipeRight()
            else onSwipeLeft()

        } else false
    }

    companion object {
        private const val SWIPE_DISTANCE_MULTIPLIER = 0.33F
        private const val SWIPE_VELOCITY_MULTIPLIER = 0.4F
    }
}