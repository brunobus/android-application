package hr.bpervan.novaeva.rest

import android.util.Log
import androidx.annotation.StringRes
import hr.bpervan.novaeva.main.BuildConfig
import hr.bpervan.novaeva.main.R
import hr.bpervan.novaeva.rest.EvaDomain.VOCATION
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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

enum class EvaDomain(@StringRes val title: Int,
                     val domainEndpoint: String,
                     val rootId: Long = 0) {

    SPIRITUALITY(R.string.spirituality, "spirituality"),
    TRENDING(R.string.trending, "trending"),
    QUOTES(R.string.quotes, "proverbs", -1),
    MULTIMEDIA(R.string.multimedia, "multimedia"),
    GOSPEL(R.string.gospel, "gospel", 4),
    SERMONS(R.string.sermons, "sermons"),
    VOCATION(R.string.vocation, "vocation"),
    ANSWERS(R.string.answers, "answers"),
    SONGBOOK(R.string.songbook, "songbook", 355),
    RADIO(R.string.radio, "radio", 473),
    PRAYERS(R.string.prayerbook, "prayers");

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

    @Deprecated("legacy v2 api")
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
                .addInterceptor(HttpLoggingInterceptor {
                    Log.i("evaHttp", it)
                }.setLevel(HttpLoggingInterceptor.Level.BODY))
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