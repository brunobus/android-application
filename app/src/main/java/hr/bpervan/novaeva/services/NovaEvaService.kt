package hr.bpervan.novaeva.services

import hr.bpervan.novaeva.model.ContentData
import hr.bpervan.novaeva.model.Breviary
import hr.bpervan.novaeva.model.DirectoryContent
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
                            @Query("date") date: String? = null): Single<DirectoryContent>

    @GET("json?api=2")
    fun getContentData(@Query("nid") contentId: Long): Single<ContentData>

    @GET("json?api=2")
    fun getBreviary(@Query("brev") breviaryId: String): Single<Breviary>

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