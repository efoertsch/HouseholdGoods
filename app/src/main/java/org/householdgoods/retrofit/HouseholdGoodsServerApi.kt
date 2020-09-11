package org.householdgoods.retrofit

import okhttp3.ResponseBody
import org.householdgoods.woocommerce.Category
import org.householdgoods.woocommerce.Product
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface HouseholdGoodsServerApi {

    // Get an arraylist of all categories
    @GET("/wp-json/wc/v3/products/categories")
    suspend fun getAllCategories(@Query("per_page") perPage: Int
                                 , @Query("offset") offset: Int): ArrayList<Category>?

    // Use to find if sku already exists - sku's are supposedly unique but call returns JSON array
    @GET("/wp-json/wc/v3/products/")
    suspend fun getProductBySku(@Query("sku") sku: String): ArrayList<Product>?

    // Add a product
    @POST("wp-json/wc/v3/products")
    suspend fun addProduct(@Body product: Product) : Product



    //vvvvvvv  Used for testing  vvvvvvvvvv
    @get:GET("/wp-json/wc/v3")
    val houseGoodsApiList: Call<ResponseBody?>?

    // Testing using Call<List<Category>>  Return last of categories as array
    @GET("/wp-json/wc/v3/products/categories")
    fun getAllCategoriesTest( @Query("per_page") perPage: Int, @Query("offset") offset: Int): Call<List<Category>>

    // Testing using  Call ResponseBody
    @GET("/wp-json/wc/v3/products/categories")
    fun getAllCategoriesResponseBody( @Query("per_page") perPage: Int, @Query("offset") offset: Int): Call<ResponseBody?>?

    // Testing using  Call ResponseBody
    @GET("/wp-json/wc/v3/products/categories")
    fun getAllCategoriesResponseBody(@Query("consumer_key") consumer_key: String, @Query("consumer_secret") consumer_secret: String, @Query("per_page") perPage: Int, @Query("offset") offset: Int): Call<ResponseBody?>?
}