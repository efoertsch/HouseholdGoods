package org.householdgoods.app

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.householdgoods.R
import org.householdgoods.data.HHGCategory
import org.householdgoods.retrofit.HouseholdGoodsServerApi
import org.householdgoods.woocommerce.Category
import org.householdgoods.woocommerce.Product
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.Arrays.asList
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList


// For reference on Kotlin/suspend functions/background thread  https://developer.android.com/kotlin/coroutines
// Move the execution of the coroutine to the I/O dispatcher

@Singleton
class Repository @Inject constructor(private val appContext: Context, val apiKey: String, val apiSecret: String,
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
                categories = householdGoodsServerApi.getAllCategories(apiKey, apiSecret, perPage, offset)
                if (categories!!.isEmpty()) {
                    break
                }
                allCategories.addAll(categories)
                offset += perPage
            }
            allCategories
        }
    }

    suspend fun getFirstAvailableSkuSequenceNumber(partialSku: String): String {
        return withContext(Dispatchers.IO) {
            var product: Product? = null
            var sequenceNumber = 1
            var sequenceString = "00"
            var firstPartOfSku = partialSku.substring(0, 7)
            var testSku: String
            while (true) {
                sequenceString = String.format("%02d", sequenceNumber)
                testSku = firstPartOfSku + '-' + sequenceString
                product = householdGoodsServerApi.getProductBySku(apiKey, apiSecret, testSku)
                if (product == null) {
                    break
                }
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

    suspend fun getHHGItemsFromRaw(): ArrayList<HHGCategory> {
        return withContext(Dispatchers.IO) {
            val inputStream = appContext.resources.openRawResource(R.raw.master_lookup);
            getLookupItems(inputStream)
        }
    }

    suspend fun getLookupItems(inputStream: InputStream): ArrayList<HHGCategory> {
        return withContext(Dispatchers.IO) {
            val HHGCategories: ArrayList<HHGCategory> = ArrayList()
            var itemDetails: List<String>
            var lookUpItem: HHGCategory
            val inputreader = InputStreamReader(inputStream);
            val buffreader = BufferedReader(inputreader)
            var line: String?
            var lineNumber = 0
            try {
                while (buffreader.readLine().also { line = it } != null) {
                    ++lineNumber
                    if (lineNumber >= 2) {
                        itemDetails = CSVUtils.parseLine(line)
                        lookUpItem = HHGCategory(itemDetails[0], itemDetails[1], itemDetails[2])
                        HHGCategories.add(lookUpItem)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(appContext, "Error occurred in reading MasterLookup file", Toast.LENGTH_LONG).show()
            }
            HHGCategories
        }

    }
}

