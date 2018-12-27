package hr.bpervan.novaeva.rest

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 *
 */
object ServiceBuilder {
    inline fun <reified T> build(baseUrl: String, modify: (Retrofit.Builder) -> Retrofit.Builder = { it }): T {
        val retrofit = Retrofit.Builder()
                .let(modify)
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        return retrofit.create(T::class.java)
    }
}