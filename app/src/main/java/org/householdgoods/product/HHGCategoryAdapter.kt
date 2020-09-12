package org.householdgoods.product

import android.R
import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import org.householdgoods.data.HHGCategory
import org.householdgoods.woocommerce.Category
import kotlin.collections.ArrayList

class HHGCategoryAdapter(val myContext: Context, val resourceId: Int, val items: ArrayList<HHGCategory>) : ArrayAdapter<HHGCategory?>(myContext, resourceId, items as List<HHGCategory?>) {

    private var masterHHGCategories: ArrayList<HHGCategory> = ArrayList()
    private var suggestions: ArrayList<HHGCategory> = ArrayList()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        try {
            if (convertView == null) {
                val inflater = (context as Activity).layoutInflater
                view = inflater.inflate(resourceId, parent, false)
            }
            val lookupItem = getItem(position)
            val description = view!!.findViewById<TextView>(R.id.text1)
            description.text = lookupItem!!.key + " " + lookupItem!!.item + " -  " + lookupItem!!.category
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return view!!
    }

    fun setHHGCategories(hhgCategory: ArrayList<HHGCategory>) {
        masterHHGCategories = hhgCategory

    }

    override fun getItem(position: Int): HHGCategory? {
        return items[position]
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getFilter(): Filter {
        return lookupItemFilter
    }

    private val lookupItemFilter: Filter = object : Filter() {
        override fun convertResultToString(resultValue: Any): CharSequence {
            val lookupItem = resultValue as HHGCategory
            return lookupItem.category
        }

        override fun performFiltering(charSequence: CharSequence?): FilterResults {
            return if (charSequence != null) {
                suggestions.clear()
                for (lookupItem in masterHHGCategories) {
                    if (lookupItem.key!!.toLowerCase().toLowerCase().startsWith(charSequence.toString().toLowerCase()) ||
                           // lookupItem.category!!.toLowerCase().startsWith(charSequence.toString().toLowerCase()) ||
                            lookupItem.subCategory!!.toLowerCase().startsWith(charSequence.toString().toLowerCase()) ||
                            lookupItem.item!!.toLowerCase().contains(charSequence.toString().toLowerCase())) {
                        suggestions.add(lookupItem)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = suggestions
                filterResults.count = suggestions.size
                filterResults
            } else {
                val filterResults = FilterResults()
                filterResults.values = ArrayList<Category>()
                filterResults.count = 0
                filterResults
            }
        }

        override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults?) {
            val tempValues = filterResults?.values as ArrayList<HHGCategory>
            if (filterResults.count > 0) {
                clear()
                for (lookupItem in tempValues) {
                    add(lookupItem)
                }
                notifyDataSetChanged()
            } else {
                clear()
                notifyDataSetChanged()
            }
        }
    }
}


