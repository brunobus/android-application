package hr.bpervan.novaeva.storage

import hr.bpervan.novaeva.main.BuildConfig
import hr.bpervan.novaeva.model.EvaDomainInfo
import hr.bpervan.novaeva.rest.EvaDomain
import io.realm.RealmConfiguration

/**
 * Created by vpriscan on 19.10.17..
 */

object RealmConfigProvider {

    val evaDBConfig: RealmConfiguration by lazy {
        RealmConfiguration.Builder()
                .name(BuildConfig.DB_NAME)
                .schemaVersion(7)
                .initialData { realm ->
                    for (domain in EvaDomain.values()) {
                        val domainInfo = EvaDomainInfo(domain.name, domain.domainEndpoint, domain.rootId)
                        realm.insert(domainInfo)
                    }
                }
                .deleteRealmIfMigrationNeeded()
                .build()
    }
}