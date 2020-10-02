package org.householdgoods

import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import okhttp3.*
import org.apache.commons.codec.binary.Base64OutputStream
import org.apache.commons.io.FileUtils
import org.householdgoods.hilt.OkHttpClientModule
import org.householdgoods.networkresponse.NetworkResponse
import org.householdgoods.retrofit.HouseholdGoodsRetrofit
import org.householdgoods.retrofit.HouseholdGoodsServerApi
import org.householdgoods.retrofit.LoggingInterceptor
import org.householdgoods.woocommerce.category.Category
import org.householdgoods.woocommerce.photo.WcPhoto
import org.householdgoods.woocommerce.photo.WcPhotoUpload
import org.junit.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import retrofit2.Response
import retrofit2.Retrofit
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.charset.Charset
import java.time.Instant
import java.util.*
import kotlin.collections.HashMap


class HouseholdGoodsTest {

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
                offset += perPage
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
        //val file = File("/Users/ericfoertsch/Downloads/IMG_20200924_082548183.jpg")
        val file = File("/Users/ericfoertsch/Downloads/2020_09_13_16_48_26_19.jpg")
        val decodedImageFileName = "/Users/ericfoertsch/Downloads/decodedBase64.jpg"
        val base64StringFile = "/Users/ericfoertsch/Downloads/base64_jpg_encoded.txt"
        val base64String = convertImageFileToBase64(file)
        // decode it back to original to check that encode/decode still produces valid jpg
        convertBase64StringToFile(base64String, decodedImageFileName)
        // write the base64 string to a file for later use in curl/postman testing
        writeBase64StringToFile(base64String, base64StringFile)
        val wcPhoto = WcPhoto()

        wcPhoto.date = Instant.now().toString()
        wcPhoto.media_attachment = base64String
        wcPhoto.media_path = "2020/10"
        wcPhoto.media_type = "image"
        wcPhoto.mime_type = "image/jpeg"
        wcPhoto.status = "publish"

        val fileName = "TM-1002-01-02-1000000.jpg"
        wcPhoto.title.rendered = fileName
        wcPhoto.title.raw = fileName
        wcPhoto.type = "attachment"
        wcPhoto.author = "HHG"
        //wcPhoto.description = fileName
        //wcPhoto.slug = fileName
       // val headerContent = "attachment;filename=".plus(fileName)
        val headersMap = HashMap<String, String>()
        //headersMap.put("attachment", "filename=".plus(fileName))
        //headersMap.put("Content-Type", "application/x-www-form-urlencoded")

       // uploadPhotoGetNetworkResponse(headersMap, wcPhoto)
        uploadPhotoReturnResponseBody(wcPhoto)
    }

    private fun uploadPhotoReturnResponseBody(wcPhoto : WcPhoto ) {
        runBlocking {
            val job: Job = launch(context = Dispatchers.IO) {
                val addedPhoto = client?.addPhoto(wcPhoto , wcPhoto.title.rendered)
                System.out.println(addedPhoto?.toString())
            }
            job.join()
        }


    }

    private fun uploadPhotoGetNetworkResponse(headersMap: HashMap<String, String>, wcPhoto: WcPhoto) {
        runBlocking {
            val job: Job = launch(context = Dispatchers.IO) {
                //val networkResponse = client?.uploadWcPhotoWithNetworkResponse(wcPhoto)
                val networkResponse = client?.uploadWcPhotoWithNetworkResponse(
                        headersMap, wcPhoto.date, wcPhoto.media_type, wcPhoto.mime_type, wcPhoto.status, wcPhoto.title.raw, wcPhoto.type, wcPhoto.media_path, wcPhoto.media_attachment
    //                ,wcPhoto.slug
    //                ,wcPhoto.description
                )
                networkResponse.apply {
                    when (this) {
                        is NetworkResponse.Success -> System.out.println("Success: ${this}")
                        is NetworkResponse.ApiError -> System.out.println("ApiError: ${this}")
                        is NetworkResponse.NetworkError -> System.out.println("NetworkError: ${this}")
                        is NetworkResponse.UnknownError -> System.out.println("UnknownError: ${this}")
                    }
                }
            }

            job.join()
        }
    }


    fun convertImageFileToBase64(imageFile: File): String {
        return FileInputStream(imageFile).use { inputStream ->
            ByteArrayOutputStream().use { outputStream ->
                Base64OutputStream(outputStream, true, 0, null).use { base64FilterStream ->
                    inputStream.copyTo(base64FilterStream)
                    base64FilterStream.close() // This line is required, see comments
                    outputStream.toString()
                }
            }
        }
    }


    // This decodes base64 string and writes it back to file
// So if you have in jpg as Base64 string, the file should contain the original jpg
    fun convertBase64StringToFile(base64String: String, outputFileName: String) {
        val decodedBytes: ByteArray = Base64.getDecoder().decode(base64String)
        FileUtils.writeByteArrayToFile(File(outputFileName), decodedBytes)

    }

    // This is meant to write a base64 encoded string to a file for later use in
// using postman or curl to test photo uploads to the WooCommerce media API
    fun writeBase64StringToFile(base64String: String, outputFileName: String) {
        FileUtils.writeStringToFile(File(outputFileName), base64String, Charset.defaultCharset())
    }

}

class CoroutineTestRule(val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()) : TestWatcher() {
    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
}
