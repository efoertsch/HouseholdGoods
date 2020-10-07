package org.householdgoods.product

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.householdgoods.app.Repository
import java.io.File

class PhotoViewModel @ViewModelInject constructor(@param:Assisted private val savedStateHandle: SavedStateHandle, private val repository: Repository) : ViewModel() {

    var photoFile: MutableLiveData<File> = MutableLiveData()
    var isLoading: MutableLiveData<Boolean> = MutableLiveData()
    var errorMessage: MutableLiveData<Throwable> = MutableLiveData()
    var photoFileName : String? = null
    var photoPosition: Int = -1

    init {
        isLoading.value = true

    }

    fun loadPhotoFile()  {
        isLoading.value = true
        viewModelScope.launch {
            val result = try {
                Result.success(repository.getPhotoFile(photoFileName!!))

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

    fun setPhotoFileNameAndPostion( photoFileName: String?, position : Int) {
        this.photoFileName =  photoFileName
        this.photoPosition = position
        loadPhotoFile()
    }


}