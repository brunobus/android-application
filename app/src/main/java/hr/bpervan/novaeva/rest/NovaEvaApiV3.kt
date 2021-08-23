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
    fun latest(@Query("regionId") regionId: Long = Region.id): Single<LatestByDomainDto>


    @GET("{domain}/categories/{categoryId}/include-subs?is_active=true")
    fun categoryContent(
        @Path("domain") domain: String,
        @Path("categoryId") categoryId: Long = 0,
        @Query("page") page: Long = 1,
        @Query("items") items: Long = 20,
        @Query("regionId") regionId: Long = Region.id,
        @Query("query") searchQuery: String? = null
    ): Single<CategoryDto>

    @GET("{domain}/{contentId}")
    fun content(
        @Path("domain") domain: String,
        @Path("contentId") contentId: Long
    ): Single<ContentDto>

    @GET("{domain}/random")
    fun random(
        @Path("domain") domain: String = EvaDomain.QUOTES.domainEndpoint,
        @Query("regionId") regionId: Long = Region.id
    ): Single<ContentDto>
}