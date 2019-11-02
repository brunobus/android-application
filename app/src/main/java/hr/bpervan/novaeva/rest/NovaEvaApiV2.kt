package hr.bpervan.novaeva.rest

import hr.bpervan.novaeva.model.*
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Branimir on 28.4.2017..
 */

@Deprecated("legacy v2 api")
interface NovaEvaApiV2 {
    @GET("json?api=2")
    fun getDirectoryContent(@Query("cid") directoryId: Long,
                            @Query("date") date: Long? = null,
                            @Query("items") items: Long = 20): Single<EvaDirectoryDTO>

    @GET("json?api=2&rand=1")
    fun getRandomDirectoryContent(@Query("cid") directoryId: Long): Single<EvaDirectoryDTO>

    @GET("json?api=2")
    fun getContentData(@Query("nid") contentId: Long): Single<EvaContentDTO>

    @GET("json?api=2")
    fun getBreviary(@Query("brev") breviaryId: String): Single<EvaBreviaryDTO>

    @GET("json?api=2")
    fun searchForContent(@Query("search") searchString: String): Single<EvaSearchResultDTO>

    @GET("json?api=2&indicators=1")
    fun getNewStuff(): Single<EvaIndicatorsDTO>
}