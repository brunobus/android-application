package hr.bpervan.novaeva.storage

import io.realm.RealmConfiguration

/**
 * Created by vpriscan on 19.10.17..
 */

object RealmConfigProvider {

    val evaDBConfig: RealmConfiguration by lazy {
        RealmConfiguration.Builder()
                .name("novaEvaDb.realm")
                .schemaVersion(2)
                .deleteRealmIfMigrationNeeded()
                .build()
    }
}