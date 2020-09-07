package org.householdgoods.product

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import org.householdgoods.R
import org.householdgoods.databinding.ProductEntryView
import org.householdgoods.woocommerce.Category


@AndroidEntryPoint
class ProductEntryFragment : Fragment() {

    private val REQUEST_IMAGE_CAPTURE = 1111
    private val RESULT_OK = 0

    private var productEntryView: ProductEntryView? = null
    private var categoryList: ArrayList<Category> = ArrayList()
    private val viewModel: ProductEntryViewModel by activityViewModels()
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var photoCollectionAdapter: PhotoCollectionAdapter
    private lateinit var viewPager: ViewPager2


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        productEntryView = ProductEntryView.inflate(inflater, container, false)

        productEntryView?.productPhotoImage?.setOnClickListener { goToCameraApp() }
        return productEntryView!!.startOptionsCoordinatorLayout.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        productEntryView?.lifecycleOwner = viewLifecycleOwner
        productEntryView?.viewModel = viewModel

        val categoryAutoCompleteView = productEntryView!!.productCategoryAutoCompleteTextView

        categoryAdapter = CategoryAdapter(activity as Context, android.R.layout.simple_dropdown_item_1line, categoryList)
        categoryAutoCompleteView.threshold = 1
        categoryAutoCompleteView.setAdapter(categoryAdapter)

        // handle click event and set desc on textview
        categoryAutoCompleteView.onItemClickListener = OnItemClickListener { adapterView, view, i, l ->
            val category = adapterView.getItemAtPosition(i) as Category
            categoryAutoCompleteView.setText(category.name)
            productEntryView?.productName?.setText(category.name)
            viewModel.setCategory(category)
        }

        productEntryView?.productCameraButton?.setOnClickListener { v -> goToCameraApp() }

        photoCollectionAdapter = PhotoCollectionAdapter(this)
        viewPager = productEntryView?.productPhotoPager!!
        viewPager.adapter = photoCollectionAdapter

        val tabLayout = productEntryView?.photoTabLayout
        if (tabLayout != null) {
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = "OBJECT ${(position + 1)}"
            }.attach()
        }

        setUpObservers()
        viewModel.getlistOfCategories()
    }

    private fun setUpObservers() {
        viewModel.lookupCategoryList.observe(viewLifecycleOwner, Observer {
            it?.let {
                assignCategories(it)
            }

        })

        viewModel.photoList.observe(viewLifecycleOwner, Observer
        {
            it?.let {
                updatePhotoList(it)
            }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, {
            it?.let {
                displayErrorMessage(it)
            }
        })


    }


    private fun updatePhotoList(photoList: ArrayList<String>) {
        photoCollectionAdapter.setPhotoFileList(photoList)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        productEntryView = null
    }

    private fun assignCategories(categories: ArrayList<Category>) {
        categoryAdapter.apply {
            setCategories(categories)
            notifyDataSetChanged()
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.updateMonthAndDay()
    }


    fun goToCameraApp() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK || resultCode == -1) {
            if (data != null) {
                savePhoto(data.extras)
            } else {
                Toast.makeText(context, getString(R.string.no_photo_taken), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun savePhoto(extras: Bundle?) {
        val photo = extras!!["data"] as Bitmap?
        // may need to compress image from tablet camera
        productEntryView?.productPhotoImage?.setImageBitmap(photo)
        viewModel.savePhoto(photo!!)
    }

    private fun displayErrorMessage(throwable: Throwable) {

        val alertDialog = AlertDialog.Builder(context)
                .setTitle("OOPS!")
                .setMessage("Please show Mike or Leon the following error message before hitting the OK button \n" + throwable.message)
                .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, which ->
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()

    }


}