package org.householdgoods.hilt;

import android.content.Context
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.householdgoods.BuildConfig
import org.householdgoods.R
import org.householdgoods.app.Repository
import org.householdgoods.retrofit.*
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    fun provideRepository(@ApplicationContext appContext: Context, householdGoodsServerApi: HouseholdGoodsServerApi): Repository {
        return Repository(appContext, householdGoodsServerApi)
    }

    @Provides
    @Named("householdGoodsUrl")
    fun provideHouseholdGoodsUrl(@ApplicationContext appContext: Context) :String {
        return appContext.getString(R.string.householdgoods_url)
    }


    @Provides
    @Singleton
    fun getHouseHoldGoodRetrofit(okHttpClient: OkHttpClient, @Named("householdGoodsUrl") householdGoodsUrl: String) : HouseholdGoodsRetrofit {
        return  HouseholdGoodsRetrofit(okHttpClient, householdGoodsUrl)
    }

    @Provides
    @Singleton
    fun getOkHttpClient() : OkHttpClient {
        val interceptor : Interceptor
        if (BuildConfig.DEBUG) {
            interceptor = LoggingInterceptor()
        } else {
            interceptor = HeaderInterceptor()
        }
        return OkHttpClientModule().getOkHttpClient(interceptor)
    }

    @Provides
    @Singleton
    fun provideHouseholdGoodsApi(householdGoodsRetrofit: HouseholdGoodsRetrofit) : HouseholdGoodsServerApi{
        return (householdGoodsRetrofit.retrofit.create(HouseholdGoodsServerApi::class.java))
    }

}