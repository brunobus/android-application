package hr.bpervan.novaeva.services;

import hr.bpervan.novaeva.model.Article;
import hr.bpervan.novaeva.model.Taxonomy;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Branimir on 28.4.2017..
 */

public interface NovaEvaService {

    @GET("json?api=2")
    Call<Taxonomy> getNewsList(@Query("cid") int cid);

    @GET("json?api=2")
    Call<Article> getArticle(@Query("nid") int nid);
}
