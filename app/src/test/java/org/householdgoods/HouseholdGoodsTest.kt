package org.householdgoods

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.householdgoods.hilt.OkHttpClientModule
import org.householdgoods.retrofit.HouseholdGoodsRetrofit
import org.householdgoods.retrofit.HouseholdGoodsServerApi
import org.householdgoods.retrofit.LoggingInterceptor
import org.householdgoods.woocommerce.Category
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.util.*

class HouseholdGoodsTest {

    // Staging 5
    var householdGoogsUrl = "https://staging5.online.householdgoods.org"
    var apiKey = "ck_96be3dfef67a29ef6b8a5e92d77c3cd8082ab154"
    var apiSecret = "cs_1dd007b26dab4686e96e075c1866e066997c1e86"

    // Staging7 keys
//    var householdGoogsUrl = "https://staging7.online.householdgoods.org"
//     var apiKey = "ck_10409f5fc5d2a4a27c9dec959084b78cfd0d363a"
//     var apiSecret = "cs_79c203153c176c4005bc18c85b7011ea09fa4835"

    var retrofit: Retrofit? = null
    var client: HouseholdGoodsServerApi? = null

    @Before
    @Throws(Exception::class)
    fun createRetrofit() {
        retrofit = HouseholdGoodsRetrofit(OkHttpClientModule().getOkHttpClient(LoggingInterceptor(), apiKey, apiSecret), householdGoogsUrl).retrofit
        client = retrofit?.create(HouseholdGoodsServerApi::class.java)
    }

    @Test
    @Throws(IOException::class)
    fun shouldGetListofApis() {
        val response: Response<ResponseBody?>? = client!!.houseGoodsApiList!!.execute()
        Assert.assertNotNull(response)
        println("Response:" + response?.body()!!.string())
    }

    @Test
    @Throws(IOException::class)
    fun usingParmsShouldGetAllCategories() {
        var perPage = 100
        var offset = 0
        var response: Response<List<Category>>
        var categories: List<Category>?
        val allCategories = ArrayList<Category>()
        while (true) {
            response = client!!.getAllCategoriesTest(apiKey, apiSecret, perPage, offset).execute()
            categories = response.body()
            if (categories != null) {
                if (categories.isEmpty()) {
                    break
                }
                allCategories.addAll(categories)
                offset +=perPage
            }
        }
        System.out.println(Gson().toJson(allCategories))
    }

    @Test
    @Throws(Exception::class)
    fun shouldGetSomething() {
        val httpClient = OkHttpClient().newBuilder()
        OkHttpClientModule.overrideSslSocketFactory(httpClient)
        val client = httpClient.build()
        val request = Request.Builder()
                .url("https://staging7.online.householdgoods.org/wp-json/wc/v3/products/categories")
                .method("GET", null)
                .addHeader("Authorization", "Basic " + getBase64UidPwd(apiKey, apiSecret))
                .build()
        val response = client.newCall(request).execute()
        println(response.body()!!.string())
    }


    @Test
    @Throws(IOException::class)
    fun shouldGetCategoriesInResponseBody() {
        val sb: StringBuilder = StringBuilder()
        var categoryString: String
        var perPage = 100
        var offset = 0
        var response: Response<ResponseBody?>?
        while (true) {
            response = client!!.getAllCategoriesResponseBody(apiKey, apiSecret, perPage, offset)!!.execute()
            if (response != null) {
                if (response.body()!!.string().length <= 2) {
                    break
                }
                categoryString = response.body()!!.string()
                categoryString.dropLast(1)
                sb.append(response.body()!!.string().dropLast(1)).append(",")
            }
            offset += perPage
        }
        println("Response:" + sb.toString())
    }


    private fun getBase64UidPwd(key: String, secret: String): String? {
        val encoded = Base64.getEncoder().encodeToString((key + ":" + secret).toByteArray())
        println("Encoded :$encoded")
        return encoded

    }

}