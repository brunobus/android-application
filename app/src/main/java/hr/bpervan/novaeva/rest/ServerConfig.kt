package hr.bpervan.novaeva.rest

import hr.bpervan.novaeva.main.BuildConfig
import hr.bpervan.novaeva.rest.EvaDomain.VOCATION
import okhttp3.OkHttpClient

/**
 * Todo multiple regions
 */
object Region {
    const val name: String = "Croatia"
    const val id: Long = 0
}

enum class Server(val baseUrl: String, val auth: String?) {
    V2(baseUrl = BuildConfig.V2_SERVER_URI, auth = null),
    V3(baseUrl = BuildConfig.V3_SERVER_URI, auth = BuildConfig.V3_SERVER_AUTH)
}

enum class EvaDomain(val domainEndpoint: String, val rootId: Long) {

    SPIRITUALITY("spirituality", 354),
    TRENDING("trending", 9),
    QUOTES("quotes", 1),
    MULTIMEDIA("multimedia", 10),
    GOSPEL("gospel", 4),
    SERMONS("sermons", 7),
    VOCATION("vocation", BuildConfig.VOCATION_ROOT_ID),
    ANSWERS("answers", 11),
    SONGBOOK("songbook", 355),
    RADIO("radio", 473),
    PRAYERS("prayers", -1);
}

fun serverByDomain(domain: EvaDomain): Server {
    return when (domain) {
        VOCATION -> Server.V3
        else -> Server.V2
    }
}

object NovaEvaService {
    val v2: NovaEvaApiV2 by lazy {
        val server = Server.V2
        ServiceBuilder.build<NovaEvaApiV2>(server.baseUrl)
    }

    val v3: NovaEvaApiV3 by lazy {
        val server = Server.V3
        ServiceBuilder.build<NovaEvaApiV3>(server.baseUrl) { builder ->
            builder.client(OkHttpClient.Builder()
                    .apply {
                        if (server.auth != null) {
                            addInterceptor { chain ->
                                chain.proceed(chain.request()
                                        .newBuilder()
                                        .header("Authorization", server.auth)
                                        .build())
                            }
                        }
                    }
                    .build())
        }
    }
}