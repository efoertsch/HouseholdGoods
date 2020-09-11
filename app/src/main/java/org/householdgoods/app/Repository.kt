package org.householdgoods.app

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.householdgoods.data.HHGCategory
import org.householdgoods.retrofit.HouseholdGoodsServerApi
import org.householdgoods.woocommerce.Category
import org.householdgoods.woocommerce.Product
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList


// For reference on Kotlin/suspend functions/background thread  https://developer.android.com/kotlin/coroutines
// Move the execution of the coroutine to the I/O dispatcher

@Singleton
class Repository @Inject constructor(private val appContext: Context,
                                     val householdGoodsServerApi: HouseholdGoodsServerApi) {

    private var sdf: SimpleDateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SS")
    private var sharedPreferences: SharedPreferences
    private val LATEST_SKU_MMDD = "LATEST_SKU_MMDD"
    private val LATEST_SKU_SEQ = "LATEST_SKU_SEQ"

    init {
        sharedPreferences = appContext.getSharedPreferences("App", MODE_PRIVATE)
    }

    suspend fun getCategoryList(): ArrayList<Category> {
        return withContext(Dispatchers.IO) {
            var perPage = 100
            var offset = 0
            var categories: ArrayList<Category>?
            val allCategories = java.util.ArrayList<Category>()
            while (true) {
                categories = householdGoodsServerApi.getAllCategories(perPage, offset)
                if (categories!!.isEmpty()) {
                    break
                }
                allCategories.addAll(categories)
                offset += perPage
            }
            allCategories
        }
    }

    suspend fun createNewProduct(product: Product) : Product {
        return withContext(Dispatchers.IO) {
            householdGoodsServerApi.addProduct(product)
        }
    }

    /**
     * Partial sku should be in form e.g. 'TM-0903'
     */
    suspend fun getFirstAvailableSkuSequenceNumber(partialSku: String): String {
        return withContext(Dispatchers.IO) {
            var productsBySku: ArrayList<Product>? = null
            var sequenceNumber = 1
            var sequenceString = "01"
            var firstPartOfSku = partialSku.substring(0, 7)
            var testSku: String
            while (true) {
                sequenceString = String.format("%02d", sequenceNumber)
                testSku = firstPartOfSku + '-' + sequenceString
                productsBySku = householdGoodsServerApi.getProductBySku(testSku)
                if (productsBySku == null || productsBySku.size == 0) {
                    break
                }
                ++sequenceNumber
            }
            sequenceString
        }
    }

    suspend fun savePhotoToFile(bitmap: Bitmap): String {
        return withContext(Dispatchers.IO) {
            var dateTime = sdf.format(Calendar.getInstance().getTime());
            val filename = dateTime + ".jpeg"
            appContext.openFileOutput(filename, Context.MODE_PRIVATE).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
            filename
        }
    }

    suspend fun getPhotoFileList(): ArrayList<String> {
        return withContext(Dispatchers.IO) {
            val photoFileList = ArrayList<String>()
            photoFileList.addAll(appContext.fileList())
            photoFileList
        }
    }

    suspend fun getPhotoFile(photoFile: String): File? {
        return withContext(Dispatchers.IO) {
            File(appContext.filesDir, photoFile)
        }
    }

    suspend fun deletePhotoFile(photoFile: String) {
        return withContext(Dispatchers.IO) {
            appContext.deleteFile(photoFile)
        }
    }

//    fun getLastestSkuForCategory(skuCategoryCode: String, skuMMDD : String){
//        sharedPreferences.
//    }

    suspend fun deleteAllPhotos() {
        return withContext(Dispatchers.IO) {
            val photoFiles: ArrayList<String> = getPhotoFileList()
            for (photoFile in photoFiles) {
                appContext.deleteFile(photoFile)
            }
        }
    }


    suspend fun getHHGCategories(uri: Uri): ArrayList<HHGCategory> {
        return withContext(Dispatchers.IO) {
            val contentResolver = appContext.contentResolver
            val HHGCategories: ArrayList<HHGCategory> = ArrayList()
            var itemDetails: List<String>
            var lookUpItem: HHGCategory
            var line: String?
            var lineNumber = 0
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    line = reader.readLine()
                    while (line != null) {
                        ++lineNumber
                        if (lineNumber >= 2) {
                            itemDetails = CSVUtils.parseLine(line)
                            if (itemDetails[0].isNotBlank()) {
                                lookUpItem = HHGCategory(itemDetails[0], itemDetails[1], itemDetails[2], itemDetails[3])
                                HHGCategories.add(lookUpItem)
                            }
                        }
                        line = reader.readLine()
                    }
                }
            }
            HHGCategories
        }
    }
}

