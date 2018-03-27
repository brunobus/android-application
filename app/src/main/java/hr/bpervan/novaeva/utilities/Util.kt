package hr.bpervan.novaeva.utilities

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.util.Log
import android.util.TypedValue
import android.view.Window
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import android.graphics.Bitmap
import io.reactivex.Observable


/**
 * Created by vpriscan on 16.10.17..
 */

fun <T> Single<T>.subscribeAsync(onSuccess: (T) -> Unit, onError: (Throwable) -> Unit): Disposable {
    return subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onSuccess, onError)
}

fun <T> Observable<T>.subscribeThrottled(onNext: (T) -> Unit): Disposable {
    return throttleFirst(500, java.util.concurrent.TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext)
}

inline fun <T> MutableList<T>.addIfNoneExistingMatch(toAdd: T, predicate: (existingElement: T) -> Boolean) {
    if (none { predicate(it) }) add(toAdd)
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