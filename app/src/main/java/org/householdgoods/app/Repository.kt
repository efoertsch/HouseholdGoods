package org.householdgoods.app

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64.DEFAULT
import android.util.Base64.NO_WRAP
import android.util.Base64OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.householdgoods.R
import org.householdgoods.data.HHGCategory
import org.householdgoods.retrofit.HouseholdGoodsServerApi
import org.householdgoods.woocommerce.Category
import org.householdgoods.woocommerce.Product
import org.householdgoods.woocommerce.WcPhoto
import timber.log.Timber
import java.io.*
import java.text.SimpleDateFormat
import java.time.Instant
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


    init {
        sharedPreferences = appContext.getSharedPreferences("App", MODE_PRIVATE)
    }

    suspend fun getCategoryList(): ArrayList<Category> {
        return withContext(Dispatchers.IO) {
            val perPage = 100
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

    suspend fun createNewProduct(product: Product): Product {
        return withContext(Dispatchers.IO) {
            householdGoodsServerApi.addProduct(product)
        }
    }

    /**
     * Partial sku should be in form e.g. 'TM-0903'
     */
    suspend fun getFirstAvailableSkuSequenceNumber(partialSku: String): String {
        return withContext(Dispatchers.IO) {
            var productsBySku: ArrayList<Product>?
            var sequenceNumber = 1
            var sequenceString: String
            val firstPartOfSku = partialSku.substring(0, 7)
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
            val dateTime = sdf.format(Calendar.getInstance().getTime())
            val filename = dateTime + ".jpg"
            appContext.openFileOutput(filename, Context.MODE_PRIVATE).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
            filename
        }
    }

    suspend fun getPhotoFileList(): ArrayList<String> {
        return withContext(Dispatchers.IO) {
            getListOfPhotoFiles()
        }
    }

    fun getListOfPhotoFiles(): ArrayList<String> {
        val photoFileList = ArrayList<String>()
        photoFileList.addAll(appContext.fileList())
        return photoFileList
    }

    // Partial sku e.g. 'XC-0902'
    suspend fun uploadPhotosToWc(photoFileNames: ArrayList<String>, yyyymmBaseUrl: String?) {
        val baseUrl = getWcBaseMedialUrl()
        return withContext(Dispatchers.IO) {
            val photoFileList = getListOfPhotoFiles()
            if (photoFileList.size != photoFileNames.size) {
                throw Exception("Oh-oh!!! Mismatch between number of photos stored and generated URL list")
            }
            var photoFile: File?
            var base64String: String
            var wcPhoto: WcPhoto
            photoFileList.addAll(appContext.fileList())
            for ((i, photoFileName) in photoFileList.withIndex()) {
                photoFile = getPhotoFile(photoFileName)
                if (photoFile != null) {
                    base64String = convertImageFileToBase64(photoFile)
                    wcPhoto = WcPhoto()
                    wcPhoto.media_attachment = base64String
                    wcPhoto.date = Instant.now().toString()
                    wcPhoto.title = photoFileNames[i]
                    wcPhoto.slug= photoFileNames[i]
                    wcPhoto.author = "HHG"
                    //wcPhoto.media_path = baseUrl + yyyymmBaseUrl + photoFileNames[i]
                    wcPhoto.media_path =  yyyymmBaseUrl
                    Timber.d("Photo media path $wcPhoto.media_path")
                    wcPhoto = householdGoodsServerApi.addPhoto(wcPhoto, "filename=$photoFileNames[i]")
                    Timber.d("Photo $i added: $wcPhoto")
                }
            }
        }
    }

    suspend fun updateProduct(product: Product): Product {
        return householdGoodsServerApi.updateProduct(product.id, product)
    }

    suspend fun getPhotoFile(photoFile: String): File? {
        return withContext(Dispatchers.IO) {
            getFile(photoFile)
        }
    }

    private fun getFile(photoFile: String) = File(appContext.filesDir, photoFile)

    fun convertImageFileToBase64(imageFile: File): String {
        return FileInputStream(imageFile).use { inputStream ->
            ByteArrayOutputStream().use { outputStream ->
                Base64OutputStream(outputStream, DEFAULT + NO_WRAP).use { base64FilterStream ->
                    inputStream.copyTo(base64FilterStream)
                    base64FilterStream.close() // This line is required, see comments
                    outputStream.toString()
                }
            }
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

    fun getWcBaseMedialUrl(): String {
        return appContext.getString(R.string.householdgoods_url)
                .plus(appContext.getString(R.string.base_media_url))

    }
}

