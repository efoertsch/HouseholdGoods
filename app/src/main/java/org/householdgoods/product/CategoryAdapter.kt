package org.householdgoods.product

import android.R
import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import org.householdgoods.data.master.Category
import java.util.*

class CategoryAdapter(val myContext: Context, val resourceId: Int, val items: ArrayList<Category>) : ArrayAdapter<Category?>(myContext, resourceId, items) {

    private var tempItems: ArrayList<Category> =  ArrayList<Category>()
    private var suggestions: ArrayList<Category> =  ArrayList<Category>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        try {
            if (convertView == null) {
                val inflater = (context as Activity).layoutInflater
                view = inflater.inflate(resourceId, parent, false)
            }
            val category = getItem(position)
            val description = view!!.findViewById<TextView>(R.id.text1)
            description.text = category!!.keyAndCombinedCategory
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return view!!
    }

    override fun getItem(position: Int): Category? {
        return items[position]
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getFilter(): Filter {
        return categoryFilter
    }

    private val categoryFilter: Filter = object : Filter() {
        override fun convertResultToString(resultValue: Any): CharSequence {
            val category = resultValue as Category
            return category.description!!
        }

        override fun performFiltering(charSequence: CharSequence?): FilterResults {
            return if (charSequence != null) {
                suggestions.clear()
                for (category in tempItems) {
                    if (category.key!!.toLowerCase().toLowerCase().startsWith(charSequence.toString().toLowerCase()) ||
                            category.description!!.toLowerCase().startsWith(charSequence.toString().toLowerCase()) ||
                            category.combinedCategorey!!.toLowerCase().contains(charSequence.toString().toLowerCase())) {
                        suggestions.add(category)
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
            if (filterResults != null ) {
                val tempValues = filterResults?.values as ArrayList<Category>
                if (filterResults.count > 0) {
                    clear()
                    for (category in tempValues) {
                        add(category)
                    }
                    notifyDataSetChanged()
                } else {
                    clear()
                    notifyDataSetChanged()
                }
            } else{
                clear()
                notifyDataSetChanged()
            }
        }
    }


}