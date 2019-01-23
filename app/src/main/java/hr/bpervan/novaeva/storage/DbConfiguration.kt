package hr.bpervan.novaeva.storage

import hr.bpervan.novaeva.main.BuildConfig
import io.realm.RealmConfiguration

/**
 * Created by vpriscan on 19.10.17..
 */

object RealmConfigProvider {

    val evaDBConfig: RealmConfiguration by lazy {
        RealmConfiguration.Builder()
                .name(BuildConfig.DB_NAME)
                .schemaVersion(6)
                .deleteRealmIfMigrationNeeded()
                .build()
    }
}