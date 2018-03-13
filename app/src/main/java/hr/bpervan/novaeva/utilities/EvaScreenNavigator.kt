package hr.bpervan.novaeva.utilities

import android.app.Activity
import io.reactivex.disposables.CompositeDisposable

/**
 *
 */
class EvaScreenNavigator {
    private val compositeDisposable = CompositeDisposable()

    fun resume(activity: Activity) {
        compositeDisposable.clear()


    }

    fun pause() {
        compositeDisposable.clear()
    }

    fun stop() {
        compositeDisposable.dispose()
    }
}