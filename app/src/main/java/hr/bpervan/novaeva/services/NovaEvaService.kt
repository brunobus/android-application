package hr.bpervan.novaeva.services

import hr.bpervan.novaeva.model.Article
import hr.bpervan.novaeva.model.Taxonomy
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Created by Branimir on 28.4.2017..
 */

interface NovaEvaService {

    @GET("json?api=2")
    fun getNewsList(@Query("cid") cid: Int): Observable<Taxonomy>

    @GET("json?api=2")
    fun getArticle(@Query("nid") nid: Int): Observable<Article>

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
