package hr.bpervan.novaeva.rest

import hr.bpervan.novaeva.main.BuildConfig
import hr.bpervan.novaeva.rest.EvaDomain.VOCATION
import okhttp3.OkHttpClient
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

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

enum class EvaDomain(val domainEndpoint: String, val rootId: Long = 0) {

    SPIRITUALITY("spirituality"),
    TRENDING("trending"),
    QUOTES("proverbs", 1),
    MULTIMEDIA("multimedia"),
    GOSPEL("gospel", 4),
    SERMONS("sermons"),
    VOCATION("vocation"),
    ANSWERS("answers"),
    SONGBOOK("songbook", 355),
    RADIO("radio", 473),
    PRAYERS("prayers", -1);

    fun isLegacy(): Boolean {
        return this == EvaDomain.GOSPEL || this == EvaDomain.SONGBOOK || this == EvaDomain.RADIO
    }
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
            builder.client(buildHttpClient(server))
        }
    }

    private fun buildHttpClient(server: Server): OkHttpClient {

        val trustAllCerts = arrayOf<X509TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }

            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }
        })
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        val sslSocketFactory = sslContext.socketFactory

        return OkHttpClient.Builder()
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
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0])
                .hostnameVerifier { _, _ -> true }
                .build()
    }
}