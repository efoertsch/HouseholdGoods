package org.householdgoods.product

import android.graphics.Bitmap
import android.net.Uri
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.householdgoods.app.Repository
import org.householdgoods.data.HHGCategory
import org.householdgoods.woocommerce.Category
import org.householdgoods.woocommerce.Dimensions
import org.householdgoods.woocommerce.Image
import org.householdgoods.woocommerce.Product
import timber.log.Timber
import timber.log.Timber.e
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

// Overkill on some things, cutting corners on others.
class ProductEntryViewModel //super(application);
@ViewModelInject constructor(@param:Assisted private val savedStateHandle: SavedStateHandle, private val repository: Repository) : ViewModel() {

    val questionMarks = "??"
    private val photoMediaPathFormat = SimpleDateFormat("yyyy/MM")

    //!!!! Remember to put in something like  viewBinding?.lifecycleOwner = viewLifecycleOwner
    //!!!! so Android will pick up changes to isLoading
    val photoList: MutableLiveData<ArrayList<String>> = MutableLiveData()
    var savedWcCategories = ArrayList<Category>()
    val wcCategories = ArrayList<Category>()
    val hhgCategories: MutableLiveData<ArrayList<HHGCategory>> = MutableLiveData()
    val savedHhgCategories = ArrayList<HHGCategory>()
    val lookupCategoryErrorMsg = MutableLiveData<String>()

    val isWorking = MutableLiveData<Boolean>().apply { value = false }

    val skuCategoryCode = MutableLiveData<String>().apply { value = questionMarks }
    val skuDateCode = MutableLiveData<String>().apply { value = "9999" }
    val skuSequenceNumber = MutableLiveData<String>().apply { value = "??" }

    val productCategoryErrorMsg = MutableLiveData<String>()
    val productName: MutableLiveData<String> = MutableLiveData<String>().apply { value = "" }
    val productNameErrorMsg = MutableLiveData<String>()
    val productLength = MutableLiveData<String>().apply { value = "0" }
    val productLengthErrorMsg = MutableLiveData<String>()
    val productWidth = MutableLiveData<String>().apply { value = "0" }
    val productWidthErrorMsg = MutableLiveData<String>()
    val productHeight = MutableLiveData<String>().apply { value = "0" }
    val productHeightErrorMsg = MutableLiveData<String>()
    val productQuantity = MutableLiveData<String>().apply { value = "0" }
    val productQuantityErrorMsg = MutableLiveData<String>()
    val productDescription = MutableLiveData<String>().apply { value = "" }
    val productDescriptionErrorMsg = MutableLiveData<String>()
    val dataEntryOK = MutableLiveData<Boolean>().apply { value = false }
    val addedSku = MutableLiveData<String>().apply { value = "" }

    // For system, api errors
    val errorMessage: MutableLiveData<Throwable> = MutableLiveData()

    // used to strip leading zeros from dimensions
    val removeLeadingZerosPattern = "^0+(?!$)".toRegex()

    var product: Product = Product()
    val categoryHashMap = HashMap<Int, Category>()


    val mmddFormat: SimpleDateFormat = SimpleDateFormat("MMdd")

    fun loadCategories(uri: Uri) {
        isWorking.value = true
        loadWcCategories()
        loadHHGCategoryFile(uri)
    }

    private fun loadWcCategories() {
        wcCategories.clear()
        viewModelScope.launch {
            val result = try {
                Result.success(repository.getCategoryList())
            } catch (e: Exception) {
                e(e.stackTraceToString())
                Result.failure<Exception>(Exception("Could not get list of categories", e))
            }
            if (result.isSuccess) {
                if (result.getOrNull() != null) {
                    wcCategories.clear()
                    wcCategories.addAll(result.getOrNull() as ArrayList<Category>)
                    categoryHashMap.clear()
                    for (category in wcCategories) {
                        categoryHashMap.put(category.id, category)
                    }
                }
            } else {
                val throwable = result.exceptionOrNull()
                e(throwable?.stackTraceToString())
                Timber.d("Could not get list of WooCommerce list of cateogories")
                errorMessage.value = result.exceptionOrNull()
            }
            checkForBothCategoriesLoaded()
        }

    }


    private fun loadHHGCategoryFile(uri: Uri) {
        isWorking.value = true
        hhgCategories.value = ArrayList()
        viewModelScope.launch {
            val result = try {
                Result.success(repository.getHHGCategories(uri))
            } catch (exception: Exception) {
                Result.failure<Exception>(Exception("An error occurred reading the HHG CSV spreadsheet. Is it in the proper format?"))
            }
            if (result.isSuccess) {
                hhgCategories.value = result.getOrNull() as ArrayList<HHGCategory>
            }
            if (result.isFailure) {
                errorMessage.value = result.exceptionOrNull()
            }
            checkForBothCategoriesLoaded()
        }

    }

    private fun checkForBothCategoriesLoaded() {
        isWorking.value = !(wcCategories.size > 0 && hhgCategories.value != null && hhgCategories.value!!.size > 0)
    }

    fun savePhoto(bitmap: Bitmap) {
        isWorking.value = true
        viewModelScope.launch {
            val result = try {
                repository.savePhotoToFile(bitmap)
                Result.success(true)
            } catch (exception: Exception) {
                Result.failure<Exception>(Exception("An error occurred saving the photo"))
            }
            if (result.isSuccess) {
                getListOfPhotos()
            }
            if (result.isFailure) {
                errorMessage.value = result.exceptionOrNull()
            }
            isWorking.value = false
        }

    }

    fun getListOfPhotos() {
        //isWorking.value = true
        viewModelScope.launch {
            val result = try {
                Result.success(repository.getPhotoFileList())
            } catch (exception: Exception) {
                Result.failure<Exception>(Exception("An error occurred getting the list of photos"))
            }
            if (result.isSuccess) {
                photoList.value = result.getOrNull() as ArrayList<String>
            } else {
                Timber.d(result.exceptionOrNull()?.message)
                errorMessage.value = result.exceptionOrNull()
            }
            isWorking.value = false

        }
    }

    fun updateMonthAndDay() {
        skuDateCode.value = mmddFormat.format(Date())
    }


    fun addItem() {
        // 1. cycle thru SKU's to find the last one e.g. TM-0908-03  (01 and 02 already used)
        // 2. Post product to WooCommerce with appropraite sku
        // 3. If posted ok, upload photo
        // 4. Go back and update product with photo urls (can't post product if photos not yet updated
        // 5. Delete all photos?
        //
        //
        isWorking.value = true
        viewModelScope.launch {
            val result = try {
                val yyyymmBaseUrl = photoMediaPathFormat.format(Date())

                // 1. find next available sku
                val partialSku = assemblePartialSku()
                val skuSequence = repository.getFirstAvailableSkuSequenceNumber(partialSku)
                Timber.d("Available sku :  $partialSku-$skuSequence")
                assignProductSKu(skuSequence)

                //2. save product, note that returned product has id assigned
                product = repository.createNewProduct(product)

                //3. Save photos
                // create paths for photos
                val wcPhotoFileNames = createPhotoImageUrls()
                repository.uploadPhotosToWc(wcPhotoFileNames, yyyymmBaseUrl)

                // 4. Update product with photo urls
                val wcPhotoImages = createImagesForProduct(wcPhotoFileNames, yyyymmBaseUrl)
                var productWithUrls = Product()
                productWithUrls.id = product.id
                // saved product now store photos
                productWithUrls.images = wcPhotoImages
                productWithUrls = repository.updateProduct(productWithUrls)
                // update originally returned product just for kicks
                product.images = productWithUrls.images
                Result.success(product)
            } catch (exception: Exception) {
                Result.failure<Exception>(Exception("An error occurred adding the product", exception))
            }
            if (result.isSuccess) {
                product = result.getOrNull() as Product
                if (product.sku.length >= 10) {
                    skuSequenceNumber.value = product.sku?.substringAfterLast('-', "??")
                    addedSku.value = product.sku
                }
            }
            if (result.isFailure) {
                Timber.d(result.exceptionOrNull()?.message)
                errorMessage.value = result.exceptionOrNull()

            }
            isWorking.value = false

        }
    }

    // WC Image entities to be added to product
    // Just put the url into Image.src
    private fun createImagesForProduct(skuPhotoNames: ArrayList<String>, yyyymmBaseUrl: String): ArrayList<Image> {
        val images = ArrayList<Image>()
        val baseMediaUrl = repository.getWcBaseMedialUrl()
        var image: Image
        for (path in skuPhotoNames) {
            image = Image()
            image.src = baseMediaUrl.plus("/")
                    .plus(yyyymmBaseUrl)
                    .plus("/")
                    .plus(path)
            image.title = image.src
            images.add(image)
        }
        return images

    }

    // Product must have full sku created by this ppint !!
    private fun createPhotoImageUrls(): ArrayList<String> {
        val photoFileList = repository.getListOfPhotoFiles()
        val urlList = ArrayList<String>()
        val numberOfPhotos = photoFileList.size
        for (i in 0..numberOfPhotos - 1) {
            urlList.add(product.sku
                    .plus("-")
                    .plus("%02d".format(i + 1))
                    .plus(".jpg")
            )

        }
        return urlList
    }

    private fun assemblePartialSku(): String {
        return skuCategoryCode.value.plus("-").plus(skuDateCode.value)
    }

    private fun assignProductSKu(skuSequence: String) {
        product.sku = skuCategoryCode.value.plus("-")
                .plus(skuDateCode.value).plus("-")
                .plus(skuSequence)
    }

    /**
     * Validate data to point ok to enable submit button
     * Category should be selected
     * Product name should be non blank
     * dimensions should be defined
     * quantity should be defined
     * description optional?
     * photos taken?
     */
    fun validateProductEntry() {
        dataEntryOK.value = (!skuCategoryCode.value.equals(questionMarks)
                && (product.name != null && product.name.isNotEmpty())
                //  || product.sku.isEmpty()  create later
                && (product.dimensions != null)
                && product.dimensions!!.length!!.isNotBlank()
                && product.dimensions?.width!!.isNotBlank()
                && product.dimensions?.height!!.isNotBlank()
                && product.stock_quantity != 0)
    }

    fun setCategory(selectedHHGCategory: HHGCategory) {
        // use to populate sku
        assignSkuCategoryCode(selectedHHGCategory)
        // populate product record with categories
        createCategoryIdsForProduct(selectedHHGCategory)
    }

    /**
     * Create a set of Category classes that only contain the category id, and the parent(s)
     * category ids that are needed when the product record is created
     * The HHGCategory.category value must match the Category.name value
     */
    private fun createCategoryIdsForProduct(selectedHHGCategory: HHGCategory) {
        val productCategories = ArrayList<Category>()
        var categoryForId = Category()
        var wcCategory: Category? = null
        // We only care about saving the category id.
        for (category in wcCategories) {
            if (category.name.toLowerCase().equals(selectedHHGCategory.subCategory.toLowerCase())) {
                wcCategory = category
                // we only need to send category id in the product
                categoryForId.id = category.id
                break
            }
        }
        if (categoryForId.id == null) {
            // Big trouble in Little China!
            errorMessage.value = Exception("No WooCommerce Category record matches the selected $selectedHHGCategory.category")
            return
        }
        // Create categories for Product record
        productCategories.add(categoryForId)
        Timber.d("WC Category found for selected HGG category: {${wcCategory.toString()}")
        // Walk category parent to gather all parent categories and save to product record
        while (true) {
            if (wcCategory?.parent != null && wcCategory.parent != 0) {
                wcCategory = categoryHashMap.get(wcCategory.parent)
                if (wcCategory != null) {
                    categoryForId = Category()
                    categoryForId.id = wcCategory.id
                    productCategories.add(categoryForId)
                    Timber.d("Parent WC Category found: {${wcCategory.toString()}")
                }
            } else {
                break
            }
        }
        product.categories = productCategories
    }

    fun assignSkuCategoryCode(hhgCategory: HHGCategory) {
        skuCategoryCode.value = hhgCategory.key
    }

    fun deleteAllPhotos() {
        viewModelScope.launch {
            val result = try {
                Result.success(repository.deleteAllPhotos())
            } catch (exception: Exception) {
                Result.failure<Exception>(Exception("An error occurred attempting to delete all photo", exception))
            }
            if (result.isSuccess) {
                getListOfPhotos()
            } else {
                Timber.d(result.exceptionOrNull()?.message)
                errorMessage.value = result.exceptionOrNull()
            }
        }
    }

    fun deletePhoto(filename: String) {
        viewModelScope.launch {
            val result = try {
                Result.success(repository.deletePhotoFile(filename))
            } catch (exception: Exception) {
                Result.failure<Exception>(Exception("An error occurred attempting to delete a photo", exception))
            }
            if (result.isSuccess) {
                getListOfPhotos()
            } else {
                Timber.d(result.exceptionOrNull()?.message)
                errorMessage.value = result.exceptionOrNull()
            }
        }
    }

    fun getFirstPartOfSku(): String {
        if (skuCategoryCode.value.isNullOrBlank()) {
            throw Exception("Can't create SKU yet as category not selected")
        }
        if (skuDateCode.value.isNullOrEmpty()) {
            throw Exception("Cant' create SKU yet as don't have MMDD part of SKU yet")
        }
        return skuCategoryCode.value?.substring(0, 2) + "-" + skuDateCode.value?.substring(0, 5)
    }

    fun validateCategory() {
        if (skuCategoryCode.value.isNullOrBlank()) {
            productCategoryErrorMsg.value = "Category not selected. Select category from  dropdown."
        } else {
            productCategoryErrorMsg.value = null
        }
    }

    fun validateProductName() {
        val prodName = productName.value
        product.name = prodName
        if (prodName == null || prodName.isBlank() || prodName.length < 3) {
            productNameErrorMsg.value = "Product name missing or too short."
        } else {
            productNameErrorMsg.value = null
        }
        validateProductEntry()
    }

    fun validateProductLength() {
        var length = productLength.value?.replace(removeLeadingZerosPattern, "")
        if (length == null || length.isEmpty() || length.trim().length > 3
                || length.trim().startsWith('-')) {
            productLengthErrorMsg.value = "Length invalid."
            product.dimensions.length = "0"
        } else {
            addDimensionsToProductIfNeeded()
            product.dimensions.length = length
            productLength.value = length
            productLengthErrorMsg.value = null
        }
        validateProductEntry()
    }

    private fun addDimensionsToProductIfNeeded() {
        if (product.dimensions == null) {
            product.dimensions = Dimensions()
        }
    }

    fun validateProductWidth() {
        val width = productWidth.value?.replace(removeLeadingZerosPattern, "")
        if (width == null || width.isEmpty() || width.trim().length > 3
                || width.trim().startsWith('-')) {
            productWidthErrorMsg.value = "Width invalid."
            product.dimensions.width = "0"
        } else {
            addDimensionsToProductIfNeeded()
            product.dimensions.width = width
            productWidth.value = width
            productWidthErrorMsg.value = null
        }
        validateProductEntry()
    }

    fun validateProductHeight() {
        val height = productHeight.value?.replace(removeLeadingZerosPattern, "")
        if (height == null || height.isEmpty() || height.trim().length > 3
                || height.trim().startsWith('-')) {
            productHeightErrorMsg.value = "Height invalid."
            product.dimensions.height = "0"
        } else {
            addDimensionsToProductIfNeeded()
            product.dimensions.height = height
            productHeight.value = height
            productHeightErrorMsg.value = null
        }
        validateProductEntry()
    }

    fun validateProductQuantity() {
        val quantity = productQuantity.value
        if (quantity == null || quantity.isEmpty() || quantity.trim().length > 3
                || quantity.trim().startsWith('-')) {
            productQuantityErrorMsg.value = "Quantity invalid."
            product.stock_quantity = 0
        } else {
            val stockQuantity = Integer.parseInt(quantity)
            product.stock_quantity = stockQuantity
            if (stockQuantity == 0) {
                productQuantityErrorMsg.value = "Quantity can not be 0."
            } else {
                productQuantityErrorMsg.value = null
            }
        }
        validateProductEntry()
    }

    fun validateProductDescription() {
        val prodDescription = productDescription.value
        if (prodDescription == null || prodDescription.isBlank()) {
            productDescriptionErrorMsg.value = "Product description missing. Do you need to add one?"
        } else {
            productDescriptionErrorMsg.value = null
        }
        // Description allowed to be blank
        product.description = prodDescription

    }

    fun resetProduct() {
        deleteAllPhotos()
        photoList.value = null
        lookupCategoryErrorMsg.value = ""
        isWorking.value = false
        skuCategoryCode.value = questionMarks
        updateMonthAndDay()
        skuSequenceNumber.value = "??"
        productName.value = ""
        productNameErrorMsg.value = null
        productLength.value = "0"
        productLengthErrorMsg.value = null
        productWidth.value = "0"
        productWidthErrorMsg.value = null
        productHeight.value = "0"
        productHeightErrorMsg.value = null
        productQuantity.value = "0"
        productQuantityErrorMsg.value = null
        productDescription.value = ""
        productDescriptionErrorMsg.value = null
        dataEntryOK.value = false
        addedSku.value = ""

        errorMessage.value = null

        product = Product()
    }


}