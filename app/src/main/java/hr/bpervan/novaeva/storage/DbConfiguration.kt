package hr.bpervan.novaeva.storage

import hr.bpervan.novaeva.model.EvaAttachmentsIndicator
import hr.bpervan.novaeva.model.EvaContentInfo
import io.realm.RealmConfiguration
import io.realm.annotations.RealmModule

/**
 * Created by vpriscan on 19.10.17..
 */

@RealmModule(classes = arrayOf(EvaContentInfo::class, EvaAttachmentsIndicator::class))
class EvaBookmarksModule

object RealmConfigProvider {

    val bookmarksConfig: RealmConfiguration by lazy {
        RealmConfiguration.Builder()
                .name("novaeva_bookmarks.realm")
                .modules(EvaBookmarksModule())
                .build()
    }

    val cacheConfig: RealmConfiguration by lazy {
        RealmConfiguration.Builder()
                .name("novaeva_cache.realm")
                .build()
    }
}