package org.householdgoods.retrofit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class HouseholdGoodsRetrofit {

    private Retrofit retrofit;

    public HouseholdGoodsRetrofit(OkHttpClient okHttpClient, String householdGoodsUrl) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(householdGoodsUrl)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create());

        retrofit = builder.build();
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }
}

