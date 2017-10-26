package hr.bpervan.novaeva.storage

import android.util.Log
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

fun <T : RealmObject> loadNullableOnceAsync(realm: Realm, tClazz: Class<T>, idField: String, id: Long, consumer: (T?) -> Unit) {
    realm.where(tClazz)
            .equalTo(idField, id)
            .findFirstAsync()
            .asFlowable<T>()
            .filter { it.isLoaded }
            .firstOrError()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ if (it.isValid) consumer(it) else consumer(null) }, ::handleError)
}

fun <T : RealmObject> loadOnceAsync(realm: Realm, tClazz: Class<T>, idField: String, id: Long, consumer: (T) -> Unit) {
    loadNullableOnceAsync(realm, tClazz, idField, id, { if (it != null) consumer(it) })
}

fun <T : RealmObject> subscribeToChangesAsync(realm: Realm, tClazz: Class<T>, idField: String, id: Long, consumer: (T) -> Unit): Disposable {
    return realm.where(tClazz)
            .equalTo(idField, id)
            .findFirstAsync()
            .asFlowable<T>()
            .filter { it.isLoaded && it.isValid }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(consumer, ::handleError)
}