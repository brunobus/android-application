package hr.bpervan.novaeva.storage

import io.realm.*

/**
 * Created by vpriscan on 19.10.17..
 */

inline fun <T> T.onLoaded(crossinline consumer: (T?) -> Unit) where T : RealmObject {
    this.addChangeListener { it: T ->
        if (it.isLoaded) {
            this.removeAllChangeListeners()
            consumer(if (it.isValid) it else null)
        }
    }
}

inline fun <T> RealmResults<T>.onLoaded(crossinline consumer: (RealmResults<T>) -> Unit) where T : RealmModel {
    this.addChangeListener { it: RealmResults<T> ->
        if (it.isLoaded) {
            this.removeAllChangeListeners()
            consumer(it)
        }
    }
}