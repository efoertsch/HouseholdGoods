package org.householdgoods.retrofit

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.householdgoods.networkresponse.NetworkResponse
import org.householdgoods.woocommerce.category.Category
import org.householdgoods.woocommerce.error.WcError
import org.householdgoods.woocommerce.product.Product
import org.householdgoods.woocommerce.photo.WcPhoto
import org.householdgoods.woocommerce.product.ProductWithPhotos
import retrofit2.Call
import retrofit2.http.*


interface HouseholdGoodsServerApi {

    // Get an arraylist of all categories
    @Headers("Content-Type: application/json")
    @GET("/wp-json/wc/v3/products/categories")
    suspend fun getAllCategories(@Header("Authorization") base64AuthorizationString : String,
                                 @Query("per_page") perPage: Int, @Query("offset") offset: Int): ArrayList<Category>?

    // Use to find if sku already exists - sku's are supposedly unique but call returns JSON array
    @Headers("Content-Type: application/json")
    @GET("/wp-json/wc/v3/products/")
    suspend fun getProductBySku(@Header("Authorization") base64AuthorizationString : String,
                                @Query("sku") sku: String): ArrayList<Product>?

    // Add a product
    @Headers("Content-Type: application/json")
    @POST("wp-json/wc/v3/products")
    suspend fun addProduct(@Header("Authorization") base64AuthorizationString : String,
                           @Body product: Product): Product

    // Update a product
    @Headers("Content-Type: application/json")
    @PUT("wp-json/wc/v3/products/{productId}")
    suspend fun updateProduct(@Header("Authorization") base64AuthorizationString : String,
                              @Path("productId") productId: Int
                              , @Body product: Product): Product

    //Upload photo
    @POST("/wp-json/wc/v2/media")
    suspend fun addMedia(@Header("Authorization") base64AuthorizationString : String,
                         @Body wcPhoto: WcPhoto,
                         @Header("Content-Disposition") fileName: String): WcPhoto


    @POST("/wp-json/wc/v2/media")
    suspend fun uploadWcPhotoUsingBodyNetworkResponse(@Body wcPhoto: WcPhoto) : NetworkResponse<NetworkResponse.Success<WcPhoto>, WcError>

    @DELETE("/wp-json/wc/v2/media/{mediaId}")
    suspend fun deleteMedia(@Header("Authorization") base64AuthorizationString : String,
                            @Path("mediaId") mediaId: Int,
                            @Query("force") force : Boolean
                            )

    //vvvvvvv  Used for testing  vvvvvvvvvv

    //Upload photo - may get http ahead of json response

    @POST("/wp-json/wc/v2/media")
    fun addWcPhotoTestWithContentDisposition(@Body wcPhoto: WcPhoto, @Header("Content-Disposition") fileName: String): Call<ResponseBody>?

    @POST("/wp-json/wc/v2/media")
    fun addWcPhotoTest(@Body wcPhoto: WcPhoto): Call<ResponseBody>?



    @Multipart
    @POST("/wp-json/wp/v2/media")
    fun updateProfile(@Part image: MultipartBody.Part?): Call<ResponseBody?>?

    @get:GET("/wp-json/wc/v3")
    val houseGoodsApiList: Call<ResponseBody?>?

    // Testing using Call<List<Category>>  Return last of categories as array
    @GET("/wp-json/wc/v3/products/categories")
    fun getAllCategoriesTest(@Query("per_page") perPage: Int, @Query("offset") offset: Int): Call<List<Category>>

    // Testing using  Call ResponseBody
    @GET("/wp-json/wc/v3/products/categories")
    fun getAllCategoriesResponseBody(@Query("per_page") perPage: Int, @Query("offset") offset: Int): Call<ResponseBody?>?

    // Testing using  Call ResponseBody
    @GET("/wp-json/wc/v3/products/categories")
    fun getAllCategoriesResponseBody(@Query("consumer_key") consumer_key: String, @Query("consumer_secret") consumer_secret: String, @Query("per_page") perPage: Int, @Query("offset") offset: Int): Call<ResponseBody?>?
}