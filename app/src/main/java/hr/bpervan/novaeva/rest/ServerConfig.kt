package hr.bpervan.novaeva.rest

import hr.bpervan.novaeva.rest.EvaDomain.VOCATION
import hr.bpervan.novaeva.rest.Region.CROATIA
import hr.bpervan.novaeva.rest.Region.POLAND
import okhttp3.Credentials
import okhttp3.OkHttpClient

enum class Region(val id: Long) {
    CROATIA(1L),
    POLAND(2L)
}

enum class Server(val baseUrl: String, val auth: String?) {
    LEGACY_V2(baseUrl = "http://novaeva.com", auth = null),
    MOCK_V3(baseUrl = "http://vps423121.ovh.net:8080",
            auth = Credentials.basic("galadriel1", "3SKyK8"))
}

enum class EvaDomain(val domainEndpoint: String,
                     val legacyId: Long,
                     val rootCategoryId: Long = legacyId) {

    SPIRITUALITY("spirituality", 354),
    TRENDING("trending", 9),
    QUOTES("quotes", 1),
    MULTIMEDIA("multimedia", 10),
    GOSPEL("gospel", 4),
    SERMONS("sermons", 7),
    VOCATION("vocation", 8, 3214),
    ANSWERS("answers", 11),
    SONGBOOK("songbook", 355),
    RADIO("radio", 473),
    PRAYERS("prayers", -1);
}

val THIS_REGION: Region = CROATIA

val SERVER_V2 = Server.LEGACY_V2

val SERVER_V3 = when (THIS_REGION) {
    CROATIA -> Server.MOCK_V3
    POLAND -> throw NotImplementedError()
}

fun serverByDomain(domain: EvaDomain): Server {
    return when (domain) {
        VOCATION -> SERVER_V3
        else -> SERVER_V2
    }
}

object NovaEvaService {
    val v2: NovaEvaApiV2 by lazy {
        val server = SERVER_V2
        ServiceBuilder.build<NovaEvaApiV2>(server.baseUrl)
    }

    val v3: NovaEvaApiV3 by lazy {
        val server = SERVER_V3
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