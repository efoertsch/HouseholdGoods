package org.householdgoods.product

import android.graphics.Bitmap
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.householdgoods.app.Repository
import org.householdgoods.woocommerce.Category
import org.householdgoods.woocommerce.Product
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ProductEntryViewModel //super(application);
@ViewModelInject constructor(@param:Assisted private val savedStateHandle: SavedStateHandle, private val repository: Repository) : ViewModel() {


    //!!!! Remember to put in something like  viewBinding?.lifecycleOwner = viewLifecycleOwner
    //!!!! so Android will pick up changes to isLoading
    var photoList: MutableLiveData<ArrayList<String>> = MutableLiveData()
    var lookupCategoryList: MutableLiveData<ArrayList<Category>> = MutableLiveData()
    var errorMessage: MutableLiveData<Throwable> = MutableLiveData()
    var isWorking: MutableLiveData<Boolean> = MutableLiveData()
    var skuDateCode : MutableLiveData<String> = MutableLiveData()
    var skuSequenceNumber : MutableLiveData<String> = MutableLiveData()
    var skuCategoryCode : MutableLiveData<String> = MutableLiveData()


    val product: Product = Product()
    val categoryHashMap = HashMap<Int, Category>()
    val masterCategoryList: ArrayList<Category> = ArrayList()
    val partialSku : String = ""

    var mmddFormat: SimpleDateFormat = SimpleDateFormat("MMdd")


    init {
        isWorking.value = true

    }

    fun getlistOfCategories() {
        isWorking.value = true
        viewModelScope.launch {
            val result = try {
                Result.success(repository.getCategoryList())
            } catch (e: Exception) {
                Result.failure<Exception>(Exception("Could not get list of categories"))
            }
            if (result.isSuccess) {
                if (result.getOrNull() != null) {
                    masterCategoryList.addAll(result.getOrNull() as ArrayList<Category>)
                    processMasterCategoryList(masterCategoryList)
                }
            } else {
                errorMessage.value = result.exceptionOrNull()
                Timber.d("Could not get list of cateogories " )
                result.exceptionOrNull()?.printStackTrace()
            }
            isWorking.value = false
        }

    }

    private fun processMasterCategoryList(masterList: ArrayList<Category>) {
        var matcher: Matcher? = null
        var firstDash = 0
        categoryHashMap.clear()
        val searchList: ArrayList<Category> = ArrayList()
        for (category in masterList) {
            categoryHashMap.put(category.id, category)
            // Want to find slugs that are like tw- or xfb-
            firstDash = category.slug.indexOfFirst { it == '-' }
            if (firstDash == 2 || firstDash == 3) {
                    searchList.add(category)
            }
        }
        lookupCategoryList.value = searchList

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
            if (result.isSuccess){
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
            if (result.isSuccess){
                photoList.value = result.getOrNull() as ArrayList<String>
            }
            if (result.isFailure) {
                Timber.d(result.exceptionOrNull()?.message)
                errorMessage.value = result.exceptionOrNull()
            }
            isWorking.value = false
        }
    }

    fun  updateMonthAndDay(){
        skuDateCode.value =  mmddFormat.format(Date())
    }


    fun submit() {

    }

    fun setCategory(category: Category) {
        val productCategories = ArrayList<Category>()
        productCategories.add(category)
        assignSkuCategoryCode(category)

        var tempCategory: Category? = category
        // TODO use to populate sku
        // Walk category parent to gather all parent categories and save to product record
        while (true) {
            if (tempCategory?.parent != null) {
                tempCategory = categoryHashMap.get(tempCategory?.parent)
                if (tempCategory != null) {
                    productCategories.add(tempCategory)
                }
            } else {
                break
            }
        }
        product.categories = productCategories
    }

    fun assignSkuCategoryCode(category : Category){
        var firstDashIndex = category.slug.indexOfFirst { it == '-' }
        if (firstDashIndex > 0 && firstDashIndex <= 3) {
            skuCategoryCode.value = category.slug.subSequence(0,firstDashIndex ).toString().toUpperCase()
        } else {
            errorMessage.value = Exception("Oh-oh the sku category code wasn't found at the start of the slug for category ${category.name}")
            skuCategoryCode.value  = "  "
        }
    }

    fun getFirstAvailableSkuSequenceNumber(){
        viewModelScope.launch {
            val result = try {
                Result.success(repository.getFirstAvailableSkuSequenceNumber(getFirstPartOfSku()))
            } catch (exception: Exception) {
                Result.failure<Exception>(Exception("An error occurred attempting to find the SKU the next sequence number", exception))
            }
            if (result.isSuccess){
                skuSequenceNumber.value = result.getOrNull() as  String
            }
            if (result.isFailure) {
                Timber.d(result.exceptionOrNull()?.message)
                errorMessage.value = result.exceptionOrNull()
            }
        }
    }

    fun deleteAllPhotos(){
        viewModelScope.launch {
            val result = try{
                Result.success(repository.deleteAllPhotos())
            } catch (exception: Exception){
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

    fun deletePhoto(filename : String) {
        viewModelScope.launch {
            val result = try{
                Result.success(repository.deletePhotoFile(filename))
            } catch (exception: Exception){
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

    fun getFirstPartOfSku() : String {
        if (skuCategoryCode.value.isNullOrBlank()) {
            throw Exception("Can't create SKU yet as category not selected")
        }
        if (skuDateCode.value.isNullOrEmpty()) {
            throw Exception("Cant' create SKU yet as don't have MMDD part of SKU yet")
        }
         return skuCategoryCode.value?.substring(0,2) + "-" + skuDateCode.value?.substring(0,5)
    }
}