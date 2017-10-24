package hr.bpervan.novaeva.services

import hr.bpervan.novaeva.model.*
import io.reactivex.Single
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
    fun getDirectoryContent(@Query("cid") directoryId: Long,
                            @Query("date") date: String? = null): Single<EvaDirectoryContentListDTO>

    @GET("json?api=2&rand=1")
    fun getRandomDirectoryContent(@Query("cid") directoryId: Long): Single<EvaDirectoryContentListDTO>

    @GET("json?api=2")
    fun getContentData(@Query("nid") contentId: Long): Single<EvaContentDTO>

    @GET("json?api=2")
    fun getBreviary(@Query("brev") breviaryId: String): Single<EvaBreviaryDTO>

    @GET("json?api=2")
    fun searchForContent(@Query("search") searchString: String): Single<EvaSearchResultDTO>

    @GET("json?api=2&indicators=1")
    fun getNewStuff(): Single<EvaIndicatorsDTO>

    companion object {
        val instance by lazy {
            create()
        }

        private fun create(): NovaEvaService {
            val retrofit = Retrofit.Builder()
                    .baseUrl("http://novaeva.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
            return retrofit.create(NovaEvaService::class.java)
        }
    }
}