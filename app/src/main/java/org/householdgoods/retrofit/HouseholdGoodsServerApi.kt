package org.householdgoods.retrofit

import okhttp3.ResponseBody
import org.householdgoods.woocommerce.Category
import org.householdgoods.woocommerce.Product
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface HouseholdGoodsServerApi {

    // Get an arraylist of all categories
    @GET("/wp-json/wc/v3/products/categories?filter[orderby]=name&order=desc")
    suspend fun getAllCategories(@Query("consumer_key") consumer_key: String
                         , @Query("consumer_secret") consumer_secret: String
                         ,@Query("per_page") perPage : Int
                         , @Query("offset") offset : Int ): ArrayList<Category>?

    //
    @GET("/wp-json/wc/v3/products/")
    suspend fun getProductBySku(@Query("sku") sku : String ) : Product?

    //testing
    @get:GET("/wp-json/wc/v3")
    val houseGoodsApiList: Call<ResponseBody?>?


    // Testing using Call<List<Category>>  Return liast of categories as array
    @GET("/wp-json/wc/v3/products/categories")
    fun  getAllCategoriesTest(@Query("consumer_key") consumer_key: String
                              , @Query("consumer_secret") consumer_secret: String
                              ,@Query("per_page") perPage : Int
                              , @Query("offset") offset : Int ): Call<List<Category>>


    // Testing using  Call ResponseBody
    @GET("/wp-json/wc/v3/products/categories")
    fun getAllCategoriesResponseBody(@Query("consumer_key") consumer_key: String
                                     , @Query("consumer_secret") consumer_secret: String
                                     ,@Query("per_page") perPage : Int
                                     , @Query("offset") offset : Int ): Call<ResponseBody?>?
}