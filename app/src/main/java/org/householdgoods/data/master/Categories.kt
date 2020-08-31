package org.householdgoods.data.master

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

class Categories {
    @SerializedName("categories")
    @Expose
    var categories: ArrayList<Category>? = null
}