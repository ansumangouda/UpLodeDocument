package com.example.uplodedocument;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitNetwork {
    private static Retrofit retrofit;

    private static final String BASE_URL = "https://telemedicinepvtapi.esdinfra.com/";
    private static final String photo_URL = "https://telemedicinepvtapi.esdinfra.com/Images/";


    static HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();

    public static HttpLoggingInterceptor getInterceptor() {
        return interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    private static OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(getInterceptor())
            .build();

    private static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }


        return retrofit;
    }

    public static ApiService getApiservice() {
        return getRetrofitInstance().create(ApiService.class);
    }
}
