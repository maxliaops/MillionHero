package me.lingfengsan.hero.keyword;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by maxliaops on 18-1-14.
 */

public interface KeywordsService {
    int DEFAULT_TIMEOUT = 2;
    String BASE_URL = "http://zhannei.baidu.com/";

    @GET("api/customsearch/keywords")
    Call<KeywordsResponse> getKeywords(@Query("title") String title);

    class Creator {
        public static KeywordsService newHireMeService() {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            return retrofit.create(KeywordsService.class);
        }
    }
}
