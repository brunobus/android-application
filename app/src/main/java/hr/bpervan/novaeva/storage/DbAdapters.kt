package hr.bpervan.novaeva.storage

import hr.bpervan.novaeva.model.CONTENT_ID_FIELD
import hr.bpervan.novaeva.model.EvaContentInfo
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort

/**
 * Created by vpriscan on 19.10.17..
 */

object EvaBookmarkDbAdapter {
    fun loadEvaBookmarksAsync(realm: Realm, bookmarksConsumer: (RealmResults<EvaContentInfo>) -> Unit) {
        realm.where(EvaContentInfo::class.java)
                .findAllSortedAsync(CONTENT_ID_FIELD, Sort.DESCENDING) //todo use date for sorting
                .onLoaded { bookmarksConsumer(it) }
    }

    fun storeBookmarkAsync(realm: Realm, bookmark: EvaContentInfo) {
        realm.executeTransactionAsync { it ->
            it.copyToRealmOrUpdate(bookmark)
        }
    }
}

object AnotherExampleAdapter {

}