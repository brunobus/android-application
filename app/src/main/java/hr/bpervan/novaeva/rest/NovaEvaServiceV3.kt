package hr.bpervan.novaeva.rest

import hr.bpervan.novaeva.model.CategoryDto
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NovaEvaServiceV3 {

    @GET("{endpointRoot}/categories/{categoryId}/include-subs")
    fun categoryContent(@Path("endpointRoot") endpointRoot: String,
                        @Path("categoryId") categoryId: Long,
                        @Query("page") page: Long,
                        @Query("region") region: Long): Single<CategoryDto>
}