package org.householdgoods

import com.google.gson.Gson
import okhttp3.*
import org.apache.commons.codec.binary.Base64OutputStream
import org.apache.commons.io.FileUtils
import org.householdgoods.hilt.OkHttpClientModule
import org.householdgoods.retrofit.HouseholdGoodsRetrofit
import org.householdgoods.retrofit.HouseholdGoodsServerApi
import org.householdgoods.retrofit.LoggingInterceptor
import org.householdgoods.woocommerce.Category
import org.householdgoods.woocommerce.WcPhoto
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import retrofit2.Retrofit
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.time.Instant
import java.util.*


class HouseholdGoodsTest {

    // Staging 5
//    var householdGoodsUrl = "https://staging5.online.householdgoods.org"
//    var apiKey = "ck_96be3dfef67a29ef6b8a5e92d77c3cd8082ab154"
//    var apiSecret = "cs_1dd007b26dab4686e96e075c1866e066997c1e86"

    // Staging7 keys
//    var householdGoodsUrl = "https://staging7.online.householdgoods.org"
//     var apiKey = "ck_10409f5fc5d2a4a27c9dec959084b78cfd0d363a"
//     var apiSecret = "cs_79c203153c176c4005bc18c85b7011ea09fa4835"

    // Staging 9
    var householdGoodsUrl = "https://staging9.online.householdgoods.org"
    var apiKey = "ck_d086b75ea55f5d1563d36d08670d5cb022ad1fb6"
    var apiSecret = "cs_66e86a45a75a5385e4bd36464d742ee799a02d50"


    var retrofit: Retrofit? = null
    var client: HouseholdGoodsServerApi? = null

    @Before
    @Throws(Exception::class)
    fun createRetrofit() {
        retrofit = HouseholdGoodsRetrofit(OkHttpClientModule().getOkHttpClient(LoggingInterceptor(apiKey, apiSecret)), householdGoodsUrl).retrofit
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
            response = client!!.getAllCategoriesTest(perPage, offset).execute()
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
        val httpClient = OkHttpClient().newBuilder().addInterceptor(LoggingInterceptor(apiKey, apiSecret))
        OkHttpClientModule.overrideSslSocketFactory(httpClient)
        val client = httpClient.build()
        val request = Request.Builder()
                .url(householdGoodsUrl + "/wp-json/wc/v3/products/categories")
                .method("GET", null)
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

    @Test
    @Throws(IOException::class)
    fun getCategoriesInResponseBodyUsingHeader() {
        val sb: StringBuilder = StringBuilder()
        var categoryString: String
        var perPage = 100
        var offset = 0
        var response: Response<ResponseBody?>?
        while (true) {
            response = client!!.getAllCategoriesResponseBody(perPage, offset)!!.execute()
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



    @Test
    @Throws(Exception::class)
    fun testToFIS() {
        val MEDIA_TYPE_JPG = MediaType.parse("image/jpg")

        val file = File("/Users/ericfoertsch/Downloads/2020_09_13_16_48_26_19.jpg")
       // val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val body = RequestBody.create(MEDIA_TYPE_JPG, file)

        val httpClient = OkHttpClient().newBuilder().addInterceptor(LoggingInterceptor("x3rqb8eGROaP", "f1JlirUR83HFyeXOgHLfMDpR"))
        OkHttpClientModule.overrideSslSocketFactory(httpClient)
        val client = httpClient.build()
        val request = Request.Builder()
                .url("http://fisincorporated.com/wp-json/wp/v2/media")
                .addHeader("Content-Disposition", "attachment;filename=2020_09_13_16_48_26_19.jpg.jpg")
                .addHeader("Content-Type", "image/jpeg")
                .post(body)
                .build()
        val response = client.newCall(request).execute()
        println(response.body()!!.string())
    }


    @Test
    @Throws(Exception::class)
    fun testUploadPhotoToWC() {
        val file = File("/Users/ericfoertsch/Downloads/IMG_20200924_082548183.jpg")
        val decodedImageFileName = "/Users/ericfoertsch/Downloads/decodedBase64.jpg"
        val base64String = convertImageFileToBase64(file)
        convertBase64StringToFile(base64String, decodedImageFileName )
        val wcPhoto = WcPhoto()
        wcPhoto.media_attachment = base64String
        wcPhoto.media_path = "2020/09"
        wcPhoto.date = Instant.now().toString()
        var fileName = "CO-0925-01.jpg"
        wcPhoto.title = fileName
        wcPhoto.description =fileName
        wcPhoto.slug = fileName
        wcPhoto.media_type = "image"
        wcPhoto.mime_type = "image/jpeg"
        val headerContent = "attachment;filename=".plus(fileName)
        wcPhoto.author = "HHG"

        val response = client?.addWcPhotoTest(wcPhoto, headerContent)?.execute()
        println(response?.body()?.string())
    }


    fun convertImageFileToBase64(imageFile: File): String {
        return FileInputStream(imageFile).use { inputStream ->
            ByteArrayOutputStream().use { outputStream ->
                Base64OutputStream(outputStream, true, -1, null).use { base64FilterStream ->
                    inputStream.copyTo(base64FilterStream)
                    base64FilterStream.close() // This line is required, see comments
                    outputStream.toString()
                }
            }
        }
    }

    fun convertBase64StringToFile(base64String: String, outputFileName: String) {
        val decodedBytes: ByteArray = Base64.getDecoder().decode(base64String)
        FileUtils.writeByteArrayToFile(File(outputFileName), decodedBytes)

    }

}
