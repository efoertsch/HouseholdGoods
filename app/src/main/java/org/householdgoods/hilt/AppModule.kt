package org.householdgoods.hilt;

import android.content.Context
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.householdgoods.R
import org.householdgoods.app.Repository
import org.householdgoods.retrofit.HouseholdGoodsRetrofit
import org.householdgoods.retrofit.HouseholdGoodsServerApi
import org.householdgoods.retrofit.LoggingInterceptor
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {
    @Provides
//    fun provideRepository(@ApplicationContext appContext: Context,  apiHelper :ApiHelper
//                          , @Named("apiKey") apiKey: String, @Named("apiSecret") apiSecret: String, householdGoodsServerApi: HouseholdGoodsServerApi ): Repository {
//        return Repository(appContext, apiHelper, apiKey, apiSecret , householdGoodsServerApi  )
//    }

    fun provideRepository(@ApplicationContext appContext: Context
                          , @Named("apiKey") apiKey: String
                          , @Named("apiSecret") apiSecret: String
                          , householdGoodsServerApi: HouseholdGoodsServerApi ): Repository {
        return Repository(appContext, appContext.getString(R.string.apiKey), appContext.getString(R.string.apiSecret) , householdGoodsServerApi  )
    }

    @Provides
    @Named("householdGoodsUrl")
    fun provideHouseholdGoodsUrl(@ApplicationContext appContext: Context) :String {
        return appContext.getString(R.string.householdgoods_url)
    }

    @Provides
    @Named("apiKey")
    fun provideApiKey(@ApplicationContext appContext: Context) :String {
        return appContext.getString(R.string.apiKey)
    }

    @Provides
    @Named("apiSecret")
    fun provideApiSecret(@ApplicationContext appContext: Context) :String {
        return appContext.getString(R.string.apiSecret)
    }

    @Provides
    @Singleton
    fun getHouseHoldGoodRetrofit(okHttpClient: OkHttpClient,  @Named("householdGoodsUrl") householdGoodsUrl: String) : HouseholdGoodsRetrofit {
        return  HouseholdGoodsRetrofit(okHttpClient, householdGoodsUrl)
    }

    @Provides
//    @Singleton
//    fun getOkHttpClient(@Named("apiKey") apiKey: String, @Named("apiSecret") apiSecret: String ) : OkHttpClient {
//        return OkHttpClientModule().getOkHttpClient(LoggingInterceptor(), apiKey, apiSecret)
//    }

    fun getOkHttpClient(@ApplicationContext appContext: Context) : OkHttpClient {
        return OkHttpClientModule().getOkHttpClient(LoggingInterceptor(), appContext.getString(R.string.apiKey), appContext.getString(R.string.apiSecret) )
    }

    @Provides
    @Singleton
    fun getLoggingInterceptor() : Interceptor {
        return LoggingInterceptor()
    }

    @Provides
    @Singleton
    fun provideHouseholdGoosApi(householdGoodsRetrofit : HouseholdGoodsRetrofit) : HouseholdGoodsServerApi{
        return (householdGoodsRetrofit.retrofit.create(HouseholdGoodsServerApi::class.java))

    }

}