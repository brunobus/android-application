package hr.bpervan.novaeva.rest

import hr.bpervan.novaeva.rest.Region.CROATIA
import hr.bpervan.novaeva.rest.Region.POLAND
import okhttp3.Credentials
import okhttp3.OkHttpClient

enum class Region(val id: Long) {
    CROATIA(1L),
    POLAND(2L)
}

enum class EvaDomain(val domainEndpoint: String, val legacyId: Long) {
    SPIRITUALITY("spirituality", 354),
    TRENDING("trending", 9),
    QUOTES("quotes", 1),
    MULTIMEDIA("multimedia", 10),
    GOSPEL("gospel", 4),
    SERMONS("sermons", 7),
    VOCATION("vocations", 8),
    ANSWERS("answers", 11),
    SONGBOOK("songbook", 355),
    RADIO("radio", 473),
    PRAYERS("prayers", -1);
}

val region = CROATIA

object NovaEvaService {
    val v2 by lazy {
        val serverUrl = novaEvaUrl

        ServiceBuilder.build<NovaEvaApiV2>(serverUrl)
    }

    val v3 by lazy {
        val serverUrl = when (region) {
            CROATIA -> mockServiceUrl
            POLAND -> mockServiceUrl
        }
        val credentials = Credentials.basic("galadriel1", "3SKyK8")

        ServiceBuilder.build<NovaEvaApiV3>(serverUrl) { builder ->
            builder.client(OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        chain.proceed(chain.request()
                                .newBuilder()
                                .header("Authorization", credentials)
                                .build())
                    }
                    .build())
        }
    }
}