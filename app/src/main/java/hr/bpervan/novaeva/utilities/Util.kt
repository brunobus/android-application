package hr.bpervan.novaeva.utilities

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


/**
 * Created by vpriscan on 16.10.17..
 */

fun <T> Single<T>.networkRequest(onSuccess: (T) -> Unit, onError: (Throwable) -> Unit): Disposable {
    return subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onSuccess, onError)
}

fun <T> Single<T>.computationRequest(onSuccess: (T) -> Unit, onError: (Throwable) -> Unit): Disposable {
    return subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onSuccess, onError)
}

fun <T> Observable<T>.screenChangeThrottle(): Observable<T> {
    return throttleFirst(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
}

inline fun <T> MutableList<T>.addIfNoneExistingMatch(toAdd: T, predicate: (existingElement: T) -> Boolean) {
    if (none { predicate(it) }) add(toAdd)
}

fun logError(throwable: Throwable) {
    Log.e("error", throwable.message, throwable)
}

fun isDarkColor(color: Int): Boolean {
    return 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255 >= 0.5
}

fun isDarkBitmap(bitmap: Bitmap): Boolean {
    val darkThreshold = bitmap.width.toFloat() * bitmap.height.toFloat() * 0.45f

    val pixels = IntArray(bitmap.width * bitmap.height)
    bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

    val darkPixels = pixels.count { isDarkColor(it) }

    return darkPixels >= darkThreshold
}

fun String.vertical() = this.asSequence().joinToString(separator = "\n")