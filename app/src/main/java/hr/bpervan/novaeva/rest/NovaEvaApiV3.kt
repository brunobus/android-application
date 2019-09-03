package hr.bpervan.novaeva.rest

import hr.bpervan.novaeva.model.CategoryDto
import hr.bpervan.novaeva.model.ContentDto
import hr.bpervan.novaeva.model.LatestByDomainDto
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NovaEvaApiV3 {

    @GET("/general/changes")
    fun latest(): Single<LatestByDomainDto>


    @GET("{domain}/categories/{categoryId}/include-subs?is_active=true")
    fun categoryContent(@Path("domain") domain: String,
                        @Path("categoryId") categoryId: Long = 0,
                        @Query("page") page: Long = 1,
                        @Query("items") items: Long = 20,
                        @Query("region") region: Long = Region.id): Single<CategoryDto>

    @GET("{domain}/random")
    fun random(@Path("domain") domain: String = EvaDomain.QUOTES.domainEndpoint,
               @Query("region") region: Long = Region.id): Single<ContentDto>
}