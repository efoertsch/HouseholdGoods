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
import org.householdgoods.woocommerce.Product

class PhotoViewModel @ViewModelInject constructor(@param:Assisted private val savedStateHandle: SavedStateHandle, private val repository: Repository) : ViewModel() {

    //TODO should I create get function for photo for use in view xml?
    var photo: MutableLiveData<Bitmap> = MutableLiveData()
    var isLoading: MutableLiveData<Boolean> = MutableLiveData()

    init {
        isLoading.value = true

    }

    fun getImage(filename: String) {
        isLoading.value = true
        viewModelScope.launch {
            val result = try {
                Result.success(repository.getPhotoFile(filename))

            } catch (exception: Exception) {
                Result.failure<Exception>(Exception("An error occurred retrieving photo"))
            }
            isLoading.value = false

            if (result.isSuccess) {
               photo.value =  result.getOrNull() as Bitmap?
            }
        }
    }



}