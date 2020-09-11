package org.householdgoods.product

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import org.householdgoods.R
import org.householdgoods.data.HHGCategory
import org.householdgoods.databinding.ProductEntryView


@AndroidEntryPoint
class ProductEntryFragment : Fragment() {

    private val REQUEST_IMAGE_CAPTURE = 1111
    private val RESULT_OK = 0

    // Request code for selecting a PDF document.
    private val REQUEST_CSV_FILE = 1212

    private var productEntryView: ProductEntryView? = null
    private var categoryList: ArrayList<HHGCategory> = ArrayList()
    private val viewModel: ProductEntryViewModel by activityViewModels()
    private lateinit var categoryAdapter: HHGCategoryAdapter
    private lateinit var photoCollectionAdapter: PhotoCollectionAdapter
    private lateinit var viewPager: ViewPager2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        productEntryView = ProductEntryView.inflate(inflater, container, false)

        setUpObservers()
        viewModel.deleteAllPhotos()

        return productEntryView!!.startOptionsCoordinatorLayout.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        productEntryView?.lifecycleOwner = viewLifecycleOwner
        productEntryView?.viewModel = viewModel

        val categoryAutoCompleteView = productEntryView!!.productCategoryAutoCompleteTextView

        categoryAdapter = HHGCategoryAdapter(activity as Context, android.R.layout.simple_dropdown_item_1line, categoryList)
        categoryAutoCompleteView.threshold = 1
        categoryAutoCompleteView.setAdapter(categoryAdapter)

        // handle click event and set desc on textview
        categoryAutoCompleteView.onItemClickListener = OnItemClickListener { adapterView, view, i, l ->
            val category = adapterView.getItemAtPosition(i) as HHGCategory
            categoryAutoCompleteView.setText(category.subCategory)
            productEntryView?.productName?.setText(category.subCategory)
            viewModel.setCategory(category)
        }

        productEntryView?.productCameraButton?.setOnClickListener { v -> goToCameraApp() }

        photoCollectionAdapter = PhotoCollectionAdapter(this)
        viewPager = productEntryView?.productPhotoPager!!
        viewPager.adapter = photoCollectionAdapter

        val tabLayout = productEntryView?.photoTabLayout
        if (tabLayout != null) {
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = "Photo ${(position + 1)}"
            }.attach()
        }

        assignFocusChangeListenersToViews()

        showHHGCategorySelectionAlertDialog()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.product_menu, menu)
        super.onCreateOptionsMenu(menu, inflater);
    }


    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.getItemId()) {
            R.id.product_menu_product_reset -> resetProduct()
            R.id.product_menu_item_woocommerce -> gotoWooCommerce()
            R.id.product_menu_category_reset -> showHHGCategorySelectionAlertDialog()
        }
        return true
    }

    private fun gotoWooCommerce() {
        val url = "http://www.example.com"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }

    private fun resetProduct() {
        viewModel.resetProduct()
    }

    /**
     * Each time user moves from field check the data
     */
    private fun assignFocusChangeListenersToViews() {
        val onFocusChangeListener = View.OnFocusChangeListener { view: View, hasFocus: Boolean ->
            if (hasFocus) {
                when (view) {
                    productEntryView?.productName -> productEntryView?.productName?.selectAll()
                    productEntryView?.productLength -> productEntryView?.productLength?.selectAll()
                    productEntryView?.productWidth -> productEntryView?.productWidth?.selectAll()
                    productEntryView?.productHeight -> productEntryView?.productHeight?.selectAll()
                    productEntryView?.productQuantity -> productEntryView?.productQuantity?.selectAll()
                    // put cursor at end of any text
                    productEntryView?.productDescription -> productEntryView?.productDescription?.append("")
                }
            } else {
                // focus lost validate input
                when (view) {
                    productEntryView?.productName -> viewModel.validateProductName()
                    productEntryView?.productLength -> viewModel.validateProductLength()
                    productEntryView?.productWidth -> viewModel.validateProductWidth()
                    productEntryView?.productHeight -> viewModel.validateProductHeight()
                    productEntryView?.productQuantity -> viewModel.validateProductQuantity()
                    productEntryView?.productDescription -> viewModel.validateProductDescription()
                }
            }
        }
        productEntryView?.productName?.setOnFocusChangeListener(onFocusChangeListener)
        productEntryView?.productLength?.setOnFocusChangeListener(onFocusChangeListener)
        productEntryView?.productWidth?.setOnFocusChangeListener(onFocusChangeListener)
        productEntryView?.productHeight?.setOnFocusChangeListener(onFocusChangeListener)
        productEntryView?.productQuantity?.setOnFocusChangeListener(onFocusChangeListener)
        productEntryView?.productDescription?.setOnFocusChangeListener(onFocusChangeListener)


    }

    private fun setUpObservers() {
        viewModel.hhgCategories.observe(viewLifecycleOwner, Observer {
            it?.let {
                assignHHGCategories(it)
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

        viewModel.dataEntryOK.observe(viewLifecycleOwner, {
            it?.let {
                productEntryView?.productSubmit?.setEnabled(it)
                productEntryView?.productSubmit?.setClickable(it)
            }
        })

        viewModel.addedSku.observe(viewLifecycleOwner, {
            it?.let {
                if (it != null && it.isNotBlank()) {
                    showAddedProductDialog(it)
                }
            }
        })

    }

    private fun showAddedProductDialog(sku: String) {
        val alertDialog = AlertDialog.Builder(context)
                .setTitle("Item Added")
                .setMessage("Item added with SKU : $sku. Write this SKU on masking tape with a black Sharpie and affix tape to item"  )
                .setPositiveButton(R.string.close_and_enter_next_product, DialogInterface.OnClickListener { dialog, which ->
                    resetProduct()
                    productEntryView!!.productCategoryAutoCompleteTextView.setText("")
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .create()
        alertDialog.setCanceledOnTouchOutside(false)
        val textView : TextView? = alertDialog.findViewById(android.R.id.message)
        textView?.setTextSize(48f)
        alertDialog.show()

    }


    private fun updatePhotoList(photoList: ArrayList<String>) {
        photoCollectionAdapter.setPhotoFileList(photoList)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        productEntryView = null
    }

    private fun assignHHGCategories(categories: ArrayList<HHGCategory>) {
        categoryAdapter.apply {
            setHHGCategories(categories)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        // No idea why select photo or file comes back with resultCode -1
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if ((resultCode == RESULT_OK || resultCode == -1) && resultData != null) {
                    savePhoto(resultData.extras)
            } else {
                Toast.makeText(context, getString(R.string.no_photo_taken), Toast.LENGTH_LONG).show()
            }
        }

        if (requestCode == REQUEST_CSV_FILE) {
            if (resultCode == RESULT_OK || resultCode == -1) {
                resultData?.data?.also { uri ->
                    run {
                        Toast.makeText(context, "One moment while categories are loaded", Toast.LENGTH_LONG).show()
                        viewModel.loadCategories(uri)
                    }
                }
            } else {
                didNotSelectHHGCategoryFileAlertDialog()
            }
        }
    }

    private fun savePhoto(extras: Bundle?) {
        val photo = extras!!["data"] as Bitmap?
        // may need to compress image from tablet camera
        viewModel.savePhoto(photo!!)
    }

    private fun showHHGCategorySelectionAlertDialog() {
        val alertDialog = AlertDialog.Builder(context)
                .setTitle("First Things First")
                .setMessage("When you click OK you will be shown a file selection screen.\n\nYou must select the HouseholdGoods category csv file for loading into this app.")
                .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, which ->
                    selectHHGCategoryFile()
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }


    private fun didNotSelectHHGCategoryFileAlertDialog() {
        val alertDialog = AlertDialog.Builder(context)
                .setTitle("Houston. We have a problem!")
                .setMessage("A HouseholdGoods category csv file must be selected to use this app." +
                        "\nIf you don't see one, see Mike or the powers that be to resolve the problem.")
                .setPositiveButton(R.string.exit, DialogInterface.OnClickListener { dialog, which ->
                    requireActivity().finish()
                })
                .setNegativeButton(R.string.try_again, DialogInterface.OnClickListener { dialog, which ->
                    requireActivity().finish()
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }


    fun selectHHGCategoryFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            // when using emulator and csv file loaded via Android studio emulator doesn't
            // recognize file with mimeType text/csv
            type = "*/*"
        }

        startActivityForResult(intent, REQUEST_CSV_FILE)
    }

    private fun displayErrorMessage(throwable: Throwable) {
        viewModel.isWorking.value = false
        val alertDialog = AlertDialog.Builder(context)
                .setTitle("OOPS!")
                .setMessage("Please show Mike or Leon the following error message before hitting the OK button \n\n"
                        + throwable.message
                        + "\n" + throwable.stackTraceToString())
                .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, which ->
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()

    }


}