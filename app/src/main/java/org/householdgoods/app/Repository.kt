package org.householdgoods.app

import android.app.DownloadManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Base64.DEFAULT
import android.util.Base64.NO_WRAP
import android.util.Base64OutputStream
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import org.householdgoods.R
import org.householdgoods.data.HHGCategory
import org.householdgoods.retrofit.HouseholdGoodsServerApi
import org.householdgoods.woocommerce.category.Category
import org.householdgoods.woocommerce.photo.WcPhoto
import org.householdgoods.woocommerce.product.Product
import org.json.JSONObject
import timber.log.Timber
import java.io.*
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


// For reference on Kotlin/suspend functions/background thread  https://developer.android.com/kotlin/coroutines
// Move the execution of the coroutine to the I/O dispatcher

@Singleton
class Repository @Inject constructor(private val appContext: Context,
                                     val householdGoodsServerApi: HouseholdGoodsServerApi) {

    private var sdf: SimpleDateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SS")
    private var sharedPreferences: SharedPreferences = appContext.getSharedPreferences("App", MODE_PRIVATE)
    private val LAST_SKU_PREF_KEY = "LAST_SKU_PREF_KEY"
    private var lastSkuHashMap: HashMap<String, String>
    var wcBase64Authorization : String


    val appPreferences : SharedPreferences

    init {
        lastSkuHashMap = loadLastSkuHashMap()
        wcBase64Authorization = createBase64BaseAuthorizationValue(appContext.getString(R.string.apiKey),
                appContext.getString(R.string.apiSecret))
        appPreferences = appContext.getSharedPreferences("HHG", MODE_PRIVATE)

    }

    suspend fun getCategoryList(): ArrayList<Category> {
        return withContext(Dispatchers.IO) {
            val perPage = 100
            var offset = 0
            var categories: ArrayList<Category>?
            val allCategories = java.util.ArrayList<Category>()
            while (true) {
                categories = householdGoodsServerApi.getAllCategories(wcBase64Authorization, perPage, offset)
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
            householdGoodsServerApi.addProduct(wcBase64Authorization, product)
        }
    }

    /**
     * Partial sku should be in form e.g. 'TM-0903'
     */
    suspend fun getFirstAvailableSkuSequenceNumber(partialSku: String, startingSeq: Int): Int {
        return withContext(Dispatchers.IO) {
            var productsBySku: ArrayList<Product>?
            var sequenceNumber = startingSeq
            var sequenceString: String
            var testSku: String
            while (true) {
                sequenceString = String.format("%02d", sequenceNumber)
                testSku = partialSku.plus('-').plus(sequenceString)
                productsBySku = householdGoodsServerApi.getProductBySku(wcBase64Authorization, testSku)
                if (productsBySku == null || productsBySku.size == 0) {
                    break
                }
                ++sequenceNumber
            }
            sequenceNumber
        }
    }

    fun getPhotoDirAndName() : Uri {
        val dateTime = sdf.format(Calendar.getInstance().time)
        val dir = appContext.filesDir
        val dirAndName = "$dir/$dateTime.jpg"
        val photoFile  =   File(dirAndName)
        val apkURI: Uri = FileProvider.getUriForFile(appContext,
                appContext.getPackageName().toString() + ".provider",
                photoFile)
        Timber.d("Photo uri: %s", apkURI.toString())
        return apkURI
    }

    suspend fun savePhotoToFile(bitmap: Bitmap): String {
        return withContext(Dispatchers.IO) {
            val dateTime = sdf.format(Calendar.getInstance().getTime())
            val filename = dateTime + ".jpg"
            Timber.d("bitmap size  height : %d, bitmap width: %d", bitmap.height, bitmap.width)
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
        // make sure  photos in ascending order (makes first photo feature photo)
        var fileList = appContext.fileList()
        Arrays.sort(fileList)
        photoFileList.addAll(fileList)
        return photoFileList
    }



    // Partial sku e.g. 'XC-0902'
    suspend fun uploadPhotosToWc(wcPhotoSkuNames: ArrayList<String>, mediaPath: String?): ArrayList<WcPhoto> {
        return withContext(Dispatchers.IO) {
            val photoFileList = getListOfPhotoFiles()
            if (photoFileList.size != wcPhotoSkuNames.size) {
                throw Exception("Oh-oh!!! Mismatch between number of photos stored and generated URL list")
            }

            val wcPhotoList = ArrayList<WcPhoto>()
            for ((i, photoFileName) in photoFileList.withIndex()) {
               // showJpgFileSize(photoFileName)
                var wcPhoto = createWcPhoto(photoFileName, wcPhotoSkuNames[i], mediaPath)
                if (wcPhoto == null) {
                    throw   Exception("Oh-oh!!! Error occurred create wcPhoto upload object!")
                }
                wcPhoto = householdGoodsServerApi.addMedia(wcBase64Authorization, wcPhoto, wcPhoto.title.raw)
                wcPhotoList.add(wcPhoto)
            }

            wcPhotoList
        }
    }

    private fun showJpgFileSize(jpegFile: String){
        val o: BitmapFactory.Options = BitmapFactory.Options()
        o.inJustDecodeBounds = true
        BitmapFactory.decodeFile(File(appContext.filesDir, jpegFile).absolutePath, o)
        val imageHeight: Int = o.outHeight
        val imageWidth: Int = o.outWidth
        Timber.d("%s   height: %d  width: %d", jpegFile, imageHeight, imageWidth)
    }

    suspend fun deleteOriginalPhotosFromWc(wcPhotos: ArrayList<WcPhoto>)  {
        return withContext(Dispatchers.IO) {
            for (wcPhoto in wcPhotos) {
                 householdGoodsServerApi.deleteMedia(wcBase64Authorization, wcPhoto.id, true)
            }
        }

    }

    suspend fun createWcPhoto(photoFileName: String, wcPhotoSkuName: String, mediaPath: String?): WcPhoto? {
        val photoFile = getPhotoFile(photoFileName)
        val base64String = convertImageFileToBase64(photoFile!!)
        if (base64String == null) {
            return null
        }
        val wcPhoto = WcPhoto()
        wcPhoto.date = Instant.now().toString()
        wcPhoto.media_attachment = base64String
        wcPhoto.media_path = mediaPath
        wcPhoto.media_type = "image"
        wcPhoto.mime_type = "image/jpeg"
        wcPhoto.status = "publish"
        wcPhoto.title.raw = wcPhotoSkuName
        wcPhoto.title.rendered = wcPhotoSkuName
        wcPhoto.type = "attachment"
        wcPhoto.author = "HHG"
        return wcPhoto
    }


    suspend fun copyPhotosToDownloadDirectory(sku: String) {
        val photoFiles = getListOfPhotoFiles()
        for (i in 0..photoFiles.size - 1) {
            val request = DownloadManager.Request(Uri.parse(photoFiles[i]))
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, sku.plus("-").plus("%02d".format(i)))
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) // to notify when download is complete
            val manager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
            manager!!.enqueue(request)
        }

    }


    suspend fun updateProduct(product: Product): Product {
        return householdGoodsServerApi.updateProduct(wcBase64Authorization, product.id, product)
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

    fun getStartingSkuSequence(categoryKey: String, skuMMDD: String): Int {
        var lastSeq: Int

        val lastSkuForCategory = lastSkuHashMap.get(categoryKey)
        if (lastSkuForCategory != null && lastSkuForCategory?.startsWith(skuMMDD)) {
            try {
                val stringSeq = lastSkuForCategory.substring(5)
                lastSeq = Integer.parseInt(stringSeq)
                lastSeq++
                Timber.d("Prior sku found for %s for %s so using %d ", categoryKey, skuMMDD, lastSeq)
                return lastSeq
            } catch (e: Exception) {
                Timber.d("could not get last sequence number \n {${e.stackTraceToString()}")
                // but we will keep truck'in
                return 1
            }
        } else {
            // starting with 1
            Timber.d("No prior sku found for %s for %s so using 1", categoryKey, skuMMDD)
            return 1
        }
    }


    private fun loadLastSkuHashMap(): HashMap<String, String> {
        val jsonString = sharedPreferences.getString(LAST_SKU_PREF_KEY, null)
        if (jsonString == null) {
            return HashMap()
        }
        val outputMap = HashMap<String, String>()
        val jsonObject = JSONObject(jsonString)
        val keysItr = jsonObject.keys()
        try {
            while (keysItr.hasNext()) {
                val key = keysItr.next()
                outputMap[key] = jsonObject[key] as String
            }
        } catch (e: java.lang.Exception) {
            throw Exception("Error loading lastSkuHashMap", e)
        }
        return outputMap
    }

    fun saveLastSkuAdded(categoryCode: String, skuMMDD: String, lastSequence: Int) {
        lastSkuHashMap.put(categoryCode, skuMMDD
                .plus("-")
                .plus("%02d".format(lastSequence)))
        val jsonObject = JSONObject(lastSkuHashMap as Map<*, *>)
        val jsonString: String = jsonObject.toString()
        val editor = sharedPreferences.edit()
        editor.remove(LAST_SKU_PREF_KEY).apply()
        editor.putString(LAST_SKU_PREF_KEY, jsonString)
        editor.commit()

    }


    // Following encode ONLY WORKS in Android (part of Android library), not in JUnit!
    private fun createBase64BaseAuthorizationValue(key: String, secret: String): String {
        val encoded = Credentials.basic(key, secret)
        System.out.println("Encoded : $encoded")
        return encoded
    }

    fun getProductStatus() : String {
        return appPreferences.getString(PRODUCT_STATUS, appContext.getString(R.string.publish))!!
    }

    fun setProductStatus(productStatus: String?)  {
        val editor = sharedPreferences.edit()
        editor.putString(PRODUCT_STATUS, productStatus)
        editor.apply()
    }

    fun getProductStatusList():  Array<String> {
        return   appContext.resources.getStringArray(R.array.product_status_list)
    }

    companion object {
        const val PRODUCT_STATUS = "PRODUCT_STATUS"
    }
}

