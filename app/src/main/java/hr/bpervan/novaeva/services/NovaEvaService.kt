package hr.bpervan.novaeva.services

import hr.bpervan.novaeva.model.Article
import hr.bpervan.novaeva.model.Taxonomy
import hr.bpervan.novaeva.services.temp.HttpRequestsTemp
import io.reactivex.Single
import io.reactivex.SingleEmitter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Branimir on 28.4.2017..
 */

interface NovaEvaService {

    @GET("json?api=2")
    fun getNewsList(@Query("cid") directoryId: Long): Single<Taxonomy>

    @GET("json?api=2")
    fun getArticle(@Query("nid") contentId: Long): Single<Article>

    companion object {

        val instance by lazy {
            create()
        }

        fun create(): NovaEvaService {
            val retrofit = Retrofit.Builder()
                    .baseUrl("http://novaeva.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
            return retrofit.create(NovaEvaService::class.java)
        }
    }
}

//TODO USE RETROFIT2 AND MOVE INSIDE NovaEvaService
fun getBreviar(brevCat: String): Single<String> {
    return Single.create { it: SingleEmitter<String> ->
        try {
            it.onSuccess(HttpRequestsTemp.getBreviar(brevCat))
        } catch (e: Exception) {
            it.onError(e)
        }
    }
}

//TODO USE RETROFIT2 AND MOVE INSIDE NovaEvaService
fun getMenuElements(categoryId: String, date: String? = null): Single<String> {
    return Single.create { it: SingleEmitter<String> ->
        try {
            it.onSuccess(HttpRequestsTemp.getMenuElements(categoryId, date))
        } catch (e: Exception) {
            it.onError(e);
        }
    }
}