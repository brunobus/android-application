package hr.bpervan.novaeva.utilities

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