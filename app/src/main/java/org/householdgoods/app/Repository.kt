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
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList


// For reference on Kotlin/suspend functions/background thread  https://developer.android.com/kotlin/coroutines
// Move the execution of the coroutine to the I/O dispatcher

@Singleton
class Repository @Inject constructor(private val appContext: Context, val apiKey: String, val apiSecret: String,
                                     val householdGoodsServerApi: HouseholdGoodsServerApi) {

    private var sdf: SimpleDateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SS")
    private var sharedPreferences : SharedPreferences
    private val LATEST_SKU_MMDD = "LATEST_SKU_MMDD"
    private val LATEST_SKU_SEQ = "LATEST_SKU_SEQ"

    init{
        sharedPreferences  = appContext.getSharedPreferences("App", MODE_PRIVATE)
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

    suspend fun savePhotoToFile(bitmap: Bitmap) :String  {
        return withContext(Dispatchers.IO) {
            var dateTime = sdf.format(Calendar.getInstance().getTime());
            val filename = dateTime + ".jpeg"
            appContext.openFileOutput(filename, Context.MODE_PRIVATE).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
            filename
        }
    }

    suspend fun getPhotoFileList(): Array<String>  {
        return withContext(Dispatchers.IO) {
            appContext.fileList()
        }
    }

    suspend fun getPhotoFile(photoFile: String) : Bitmap? {
        return withContext(Dispatchers.IO) {
            var bitmap: Bitmap? = null
            val f = appContext.openFileInput(photoFile)
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            try {
                bitmap = BitmapFactory.decodeStream(f, null, options)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            bitmap
        }
    }

    suspend fun deletePhotoFile(photoFile: String){
        return withContext(Dispatchers.IO) {
            appContext.deleteFile(photoFile)
        }
    }

//    fun getLastestSkuForCategory(skuCategoryCode: String, skuMMDD : String){
//        sharedPreferences.
//    }

    suspend fun removeAllPhotoFiles(){
        return withContext(Dispatchers.IO) {
            val photoFiles: Array<String> = getPhotoFileList()
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

