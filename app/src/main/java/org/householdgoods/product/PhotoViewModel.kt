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
import java.io.File

class PhotoViewModel @ViewModelInject constructor(@param:Assisted private val savedStateHandle: SavedStateHandle, private val repository: Repository) : ViewModel() {

    //TODO should I create get function for photo for use in view xml?
    var photoFile: MutableLiveData<File> = MutableLiveData()
    var isLoading: MutableLiveData<Boolean> = MutableLiveData()
    var errorMessage: MutableLiveData<Throwable> = MutableLiveData()

    init {
        isLoading.value = true

    }

    fun getPhotoFile(filename: String)  {
        isLoading.value = true

        viewModelScope.launch {
            val result = try {
                Result.success(repository.getPhotoFile(filename))

            } catch (exception: Exception) {
                Result.failure<Exception>(Exception("An error occurred retrieving photo",exception))
            }
            isLoading.value = false

            if (result.isSuccess) {
               photoFile.value =  result.getOrNull() as File?
            } else {
                errorMessage.value = result.exceptionOrNull()
            }
        }
    }



}