package hr.bpervan.novaeva.storage

import android.util.Log
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.realm.Realm
import io.realm.RealmObject

/**
 * Created by vpriscan on 26.10.17..
 */

fun handleError(throwable: Throwable) {
    Log.e("error", throwable.message, throwable)
}

fun <T : RealmObject> loadManyAsync(realm: Realm, tClazz: Class<T>, predicate: (T) -> Boolean,
                                    subscriber: (T) -> Unit): Disposable {
    return realm.where(tClazz)
            .findAllAsync()
            .asFlowable()
            .onBackpressureBuffer()
            .filter { it.isLoaded }
            .flatMap { Flowable.fromIterable(it) }
            .filter(predicate)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(subscriber, ::handleError)
}

fun <T : RealmObject> loadOne(realm: Realm, tClazz: Class<T>, field: String, value: Long): T? {
    return realm.where(tClazz)
            .equalTo(field, value)
            .findFirst()
}

fun <T : RealmObject> loadOneAsync(realm: Realm, tClazz: Class<T>, field: String, value: Long, consumer: (T?) -> Unit): Disposable {
    return realm.where(tClazz)
            .equalTo(field, value)
            .findFirstAsync()
            .asFlowable<T>()
            .onBackpressureBuffer()
            .filter { it.isLoaded }
            .firstOrError()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ if (it.isValid) consumer(it) else consumer(null) }, ::handleError)
}

fun <T : RealmObject> subscribeToUpdatesAsync(realm: Realm, tClazz: Class<T>, field: String, value: Long, consumer: (T) -> Unit): Disposable {
    return realm.where(tClazz)
            .equalTo(field, value)
            .findFirstAsync()
            .asFlowable<T>()
            .onBackpressureBuffer()
            .filter { it.isLoaded && it.isValid }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(consumer, ::handleError)
}