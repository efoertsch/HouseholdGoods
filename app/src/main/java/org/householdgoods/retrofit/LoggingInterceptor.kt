package org.householdgoods.retrofit

import android.annotation.SuppressLint
import android.util.Base64
import okhttp3.*
import okio.Buffer
import org.householdgoods.BuildConfig
import timber.log.Timber
import java.io.IOException

//http://stackoverflow.com/questions/32965790/retrofit-2-0-how-to-print-the-full-json-response
class LoggingInterceptor(private val apiKey: String, private val apiSecret: String) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (BuildConfig.DEBUG) {
            Timber.d("inside intercept callback")
        }
        val request = chain.request()
                .newBuilder()
                .header("Content-Type", "application/json")
                .header("Authorization",  getBase64UidPwd(apiKey, apiSecret))
                .build()
        val t1 = System.nanoTime()
        var requestLog = String.format("Sending request %s on %s %s",
                request.url(), chain.connection(), request.headers())
        if (request.method().compareTo("post", ignoreCase = true) == 0) {
            requestLog = """

                $requestLog
                ${bodyToString(request)}
                """.trimIndent()
        }
        if (BuildConfig.DEBUG) {
            Timber.d("request\n$requestLog")
        }
        val response = chain.proceed(request)
        val t2 = System.nanoTime()
        @SuppressLint("DefaultLocale") val responseLog = String.format("Received response for %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1e6, response.headers())
        val contentType = response.header("Content-Type")
        return if (contentType != null && !contentType.startsWith("image")) {
            val bodyString = response.body()!!.string()
            if (BuildConfig.DEBUG) {
                //    Timber.d("response only" + "\n" + bodyString);
                Timber.d("response\n$responseLog\n$bodyString")
            }
            response.newBuilder()
                    .body(ResponseBody.create(response.body()!!.contentType(), bodyString))
                    .build()
        } else {
            chain.proceed(request)
        }
    }

    // Following encode ONLY WORKS in Android (part of Android library), not in JUnit!
    private fun getBase64UidPwd(key: String, secret: String): String {
        val encoded = Credentials.basic(key, secret)
        Timber.d("Encoded : $encoded")
        return encoded
    }

    companion object {
        fun bodyToString(request: Request): String {
            return try {
                val copy = request.newBuilder().build()
                val buffer = Buffer()
                copy.body()!!.writeTo(buffer)
                buffer.readUtf8()
            } catch (e: IOException) {
                "did not work"
            }
        }
    }
}