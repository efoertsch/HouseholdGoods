package org.householdgoods.retrofit

import android.util.Base64
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor(val apiKey: String, val apiSecret: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = chain.run {
        proceed(
                request()
                        .newBuilder()
                        .header("Authorization", getBase64UidPwd(apiKey , apiSecret))
                        .build()
        )
    }

    //TODO make extension function
    private fun getBase64UidPwd(key: String, secret: String): String? {
        return Credentials.basic(key, secret)
    }
}