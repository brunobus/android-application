package hr.bpervan.novaeva.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.views.snackbar
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


/**
 * Created by vpriscan on 16.10.17..
 */

fun <T> Single<T>.networkRequest(onSuccess: (T) -> Unit, onError: (Throwable) -> Unit): Disposable {
    return subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError {
                Log.e("evaNetworkError", it.message, it)
            }
            .subscribe(onSuccess, onError)
}

fun <T> Observable<T>.subscribeThrottled(consumer: (T) -> Unit): Disposable {
    return throttleFirst(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe(consumer)
}

inline fun <T> MutableList<T>.addIfNoneExistingMatch(toAdd: T, predicate: (existingElement: T) -> Boolean) {
    if (none { predicate(it) }) add(toAdd)
}

operator fun CompositeDisposable.minusAssign(oldDisposable: Disposable) {
    remove(oldDisposable)
}

operator fun CompositeDisposable.plusAssign(oldDisposable: Disposable) {
    add(oldDisposable)
}

inline fun <T, R> T?.ifPresent(block: (T) -> R): R? {
    return this?.let(block)
}

fun <T> Collection<T>?.notNullNorEmpty(): Boolean = orEmpty().isNotEmpty()
fun String?.notNullNorEmpty(): Boolean = orEmpty().isNotEmpty()

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

fun Context.networkConnectionExists(): Boolean {
    val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
    return activeNetwork?.isConnectedOrConnecting == true
}

fun View.dataErrorSnackbar() {
    val networkExists = context?.networkConnectionExists() ?: false
    this.snackbar(if (networkExists) R.string.error_fetching_data else R.string.network_unavailable, Snackbar.LENGTH_SHORT)
}