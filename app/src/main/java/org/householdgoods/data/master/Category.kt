package org.householdgoods.data.master

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Category {
    @SerializedName("Category")
    @Expose
    var category: String? = null

    @SerializedName("Subcategory")
    @Expose
    var subcategory: String? = null

    @JvmField
    @SerializedName("Key")
    @Expose
    var key: String? = null

    @SerializedName("Item")
    @Expose
    var item: String? = null

    @JvmField
    @SerializedName("CombinedCategorey")
    @Expose
    var combinedCategorey: String? = null

    @SerializedName("Type")
    @Expose
    var type: String? = null

    @JvmField
    @SerializedName("Description")
    @Expose
    var description: String? = null
    val keyAndCombinedCategory: String
        get() = "$key-$combinedCategorey"
}