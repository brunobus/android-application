package hr.bpervan.novaeva.storage

import hr.bpervan.novaeva.util.logError
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.realm.RealmObject
import io.realm.RealmQuery

/**
 * Created by vpriscan on 26.10.17..
 */

fun <T : RealmObject> RealmQuery<T>.loadManyAsync(predicate: (T) -> Boolean,
                                                  subscriber: (T) -> Unit): Disposable? {
    return findAllAsync()
            .asFlowable()
            .onBackpressureBuffer()
            .filter { it.isLoaded }
            .flatMap { Flowable.fromIterable(it) }
            .filter(predicate)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(subscriber, ::logError)
}

fun <T : RealmObject> RealmQuery<T>.loadOneAsync(field: String, value: Long,
                                                 consumer: (T?) -> Unit): Disposable? {
    return equalTo(field, value)
            .findFirstAsync()
            .asFlowable<T>()
            .onBackpressureBuffer()
            .filter { it.isLoaded }
            .firstOrError()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ if (it.isValid) consumer(it) else consumer(null) }, ::logError)
}

fun <T : RealmObject> RealmQuery<T>.subscribeToUpdatesAsync(field: String, value: Long,
                                                            consumer: (T) -> Unit): Disposable {
    return equalTo(field, value)
            .findFirstAsync()
            .asFlowable<T>()
            .onBackpressureBuffer()
            .filter { it.isLoaded && it.isValid }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(consumer, ::logError)
}