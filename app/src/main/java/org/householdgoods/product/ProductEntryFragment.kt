package org.householdgoods.product

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import org.householdgoods.data.master.Category
import org.householdgoods.databinding.ProductEntryBinding
import java.util.*

@AndroidEntryPoint
class ProductEntryFragment : Fragment() {

    private var productEntryBinding: ProductEntryBinding? = null
    private var categoryArrayList: ArrayList<Category>? = null
    private val viewModel : ProductEntryViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        productEntryBinding = ProductEntryBinding.inflate(inflater, container, false)
        return productEntryBinding!!.startOptionsCoordinatorLayout.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val categoryAutoCompleteView = productEntryBinding!!.productCategoryAutoCompleteTextView
        categoryArrayList = viewModel.listOfCategories()
        val adapter = CategoryAdapter(requireContext(), R.layout.simple_dropdown_item_1line, categoryArrayList!!)
        categoryAutoCompleteView.threshold = 1
        categoryAutoCompleteView.setAdapter(adapter)

        // handle click event and set desc on textview
        categoryAutoCompleteView.onItemClickListener = OnItemClickListener { adapterView, view, i, l ->
            val category = adapterView.getItemAtPosition(i) as Category
            categoryAutoCompleteView.setText(category.keyAndCombinedCategory)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        productEntryBinding = null
    }

    companion object {
        fun newInstance(): ProductEntryFragment {
            return ProductEntryFragment()
        }
    }
}