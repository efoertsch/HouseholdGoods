package org.householdgoods.app

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.householdgoods.R
import org.householdgoods.data.master.Categories
import org.householdgoods.data.master.Category
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(private val appContext: Context) {

    private var categories: Categories? = null

   fun  listOfCategories(): ArrayList<Category>? {
            if (categories == null) {
                val reader = JSONResourceReader(appContext.resources, R.raw.categories)
                categories = reader.constructUsingGson(Categories::class.java)
            }
            return categories?.categories
        }
}