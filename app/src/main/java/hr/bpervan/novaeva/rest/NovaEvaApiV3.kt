package hr.bpervan.novaeva.rest

import hr.bpervan.novaeva.model.CategoryDto
import hr.bpervan.novaeva.model.LatestByDomainDto
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NovaEvaApiV3 {

    @GET("/general/changes")
    fun latest(): Single<LatestByDomainDto>


    @GET("{domain}/categories/{domain}/include-subs")
    fun categoryContent(@Path("domain") domain: String,
                        @Path("domain") categoryId: Long,
                        @Query("page") page: Long,
                        @Query("region") region: Long): Single<CategoryDto>
}