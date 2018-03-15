package hr.bpervan.novaeva.utilities

import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.view.Window
import hr.bpervan.novaeva.model.BackgroundType
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by vpriscan on 16.10.17..
 */

fun <T> Single<T>.subscribeAsync(onSuccess: (T) -> Unit, onError: (Throwable) -> Unit): Disposable =
        subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(onSuccess, onError)

inline fun <T> MutableList<T>.addIfNoneExistingMatch(toAdd: T, predicate: (existingElement: T) -> Boolean) {
    if (none { predicate(it) }) add(toAdd)
}

fun Window.setBackground(backgroundType: BackgroundType, resId: Int) {
    when (backgroundType) {
        BackgroundType.COLOR -> {
            setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context, resId)))
        }
        BackgroundType.DRAWABLE -> {
            setBackgroundDrawableResource(resId)
        }
    }
}