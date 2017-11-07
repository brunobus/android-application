package hr.bpervan.novaeva.storage

import hr.bpervan.novaeva.model.EvaAttachmentsIndicator
import hr.bpervan.novaeva.model.EvaContentMetadata
import io.realm.RealmConfiguration
import io.realm.annotations.RealmModule

/**
 * Created by vpriscan on 19.10.17..
 */

object RealmConfigProvider {

    val evaDBConfig: RealmConfiguration by lazy {
        RealmConfiguration.Builder()
                .name("novaevaDB.realm")
                .build()
    }
}