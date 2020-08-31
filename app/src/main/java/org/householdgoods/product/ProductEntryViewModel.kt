package org.householdgoods.product

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import org.householdgoods.app.Repository
import org.householdgoods.data.master.Category
import java.util.*

class ProductEntryViewModel //super(application);
@ViewModelInject constructor(@param:Assisted private val savedStateHandle: SavedStateHandle, private val repository: Repository) : ViewModel() {

    fun listOfCategories (): ArrayList<Category>? {
        return repository.listOfCategories()
    }
}