package org.householdgoods.product

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
    private lateinit var categoryAutoCompleteView: AppCompatAutoCompleteTextView

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

        categoryAutoCompleteView = productEntryView!!.productCategoryAutoCompleteTextView

        categoryAdapter = HHGCategoryAdapter(activity as Context, android.R.layout.simple_dropdown_item_1line, categoryList)
        categoryAutoCompleteView.threshold = 1
        categoryAutoCompleteView.setAdapter(categoryAdapter)

        // handle click event and set desc on textview
        categoryAutoCompleteView.onItemClickListener = OnItemClickListener { adapterView, view, i, l ->
            val category = adapterView.getItemAtPosition(i) as HHGCategory
            categoryAutoCompleteView.setText(category.key.plus(" - ").plus(category.item))
            productEntryView?.productName?.setText(category.item)
            viewModel.setCategory(category)
            productEntryView?.productName?.requestFocus()
        }

        productEntryView?.productCameraButton?.setOnClickListener { v -> goToCameraApp() }

        addProductStatusValuesToSpinner()

        photoCollectionAdapter = PhotoCollectionAdapter(this)
        viewPager = productEntryView?.productPhotoPager!!
        viewPager.adapter = photoCollectionAdapter

        val tabLayout = productEntryView?.photoTabLayout
        if (tabLayout != null) {
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = "Photo ${(position + 1)}"
            }.attach()
        }

        productEntryView?.productAddUpdateButton?.setOnClickListener { v ->
            productEntryView?.productAddUpdateButton?.isClickable = false
            disablePhotoDeleteButtons()
            viewModel.addOrUpdateItem()
        }

        assignFocusChangeListenersToViews()
        if (!viewModel.hasCategories()) {
            showHHGCategorySelectionAlertDialog()
        }
    }

    fun addProductStatusValuesToSpinner() {
        val productStatusList = viewModel.getProductStatusList()
        val spinner = productEntryView?.productStatusSpinner
        val adapter = getHintSpinner(productStatusList)
        spinner?.adapter = adapter
        spinner?.setSelection(viewModel.getProductStatusPosition())
        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                spinner?.requestFocus()
                spinner?.setSelection(0)
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                spinner?.requestFocus()
                viewModel.setProductStatusPosition(position)
            }
        }
    }


    fun getHintSpinner(spinnerList: Array<String>): ArrayAdapter<String?> {
        val adapter: ArrayAdapter<String?> = object : ArrayAdapter<String?>(requireContext(), R.layout.spinner_cap_words, spinnerList) {
            // First position is text hint
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(position: Int, convertView: View?,
                                         parent: ViewGroup): View? {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as TextView
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY)
                } else {
                    tv.setTextColor(Color.BLACK)
                }
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapter
    }

    // Depends on Android naming convention for viewpage fragment tags of f0, f1, ...
    private fun disablePhotoDeleteButtons() {
        for (i in 0 until viewPager.adapter!!.itemCount) {
            val photoFragment: Fragment? = childFragmentManager.findFragmentByTag("f" + i)
            if (photoFragment != null) {
                (photoFragment as PhotoFragment).disableDeletePhotoButton()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.product_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
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
        val url = getString(R.string.woocommerce_url)
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }

    private fun resetProduct() {
        productEntryView?.productSkuConfirmation?.skuConfirmationDisplay?.visibility = View.GONE
        productEntryView?.productCategoryAutoCompleteTextView?.setText("")
        productEntryView?.productAddUpdateButton?.isEnabled = false
        productEntryView?.productAddUpdateButton?.visibility = View.VISIBLE
        viewModel.resetProduct()

    }

    /**
     * Each time user moves from field check the data
     */
    private fun assignFocusChangeListenersToViews() {
        val onFocusChangeListener = View.OnFocusChangeListener { view: View, hasFocus: Boolean ->
            if (hasFocus) {
                when (view) {
                    productEntryView?.productCategoryAutoCompleteTextView -> productEntryView?.productCategoryAutoCompleteTextView?.selectAll()
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
                    productEntryView?.productCategoryAutoCompleteTextView -> viewModel.validateCategory()
                    productEntryView?.productName -> viewModel.validateProductName()
                    productEntryView?.productLength -> viewModel.validateProductLength()
                    productEntryView?.productWidth -> viewModel.validateProductWidth()
                    productEntryView?.productHeight -> viewModel.validateProductHeight()
                    productEntryView?.productQuantity -> viewModel.validateProductQuantity()
                    productEntryView?.productDescription -> {
                        viewModel.validateProductDescription()
                        closeKeyboard()
                    }

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

    private fun closeKeyboard() {
        val imm: InputMethodManager = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(productEntryView?.root!!.windowToken, 0)
    }

    private fun openKeyboard() {
        val imm = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    private fun setUpObservers() {
        viewModel.hhgCategories.observe(viewLifecycleOwner, {
            it?.let {
                assignHHGCategories(it)
                productEntryView?.productCategoryAutoCompleteTextView?.requestFocus()
                openKeyboard()
            }
        })

        viewModel.photoList.observe(viewLifecycleOwner, {
            it?.let {
                updatePhotoList(it)
            }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, {
            it?.let {
                viewModel.isWorking.value = false
                disablePhotoDeleteButtons()
                setWindowTouchability(false)
                displayErrorMessage(it)
            }
        })

        viewModel.dataEntryOK.observe(viewLifecycleOwner, {
            it?.let {
                productEntryView?.productAddUpdateButton?.isEnabled = it
                productEntryView?.productAddUpdateButton?.isClickable = it
                productEntryView?.productAddUpdateButton?.isFocusable = it
            }
        })

        viewModel.addedSku.observe(viewLifecycleOwner, {
            it?.let {
                if (it.isNotBlank()) {
                    showSkuConfirmationView()
                }
            }
        })

        viewModel.isWorking.observe(viewLifecycleOwner, {
            it?.let {
                setWindowTouchability(it)
            }
        })

        viewModel.productId.observe(viewLifecycleOwner, {
            it?.let {
                if (it == 0) {
                    productEntryView?.productAddUpdateButton?.text = getString(R.string.add_item)
                    productEntryView?.productCategoryLayout?.isEnabled = true
                } else {
                    //productEntryView?.productAddUpdateButton?.text = getString(R.string.update_item)
                    productEntryView?.productAddUpdateButton?.isEnabled = false
                    productEntryView?.productCategoryLayout?.isEnabled = false
                }
            }
        })

        viewModel.statusMessage.observe(viewLifecycleOwner, {
            it?.let {
                displayStatusMessage(it)
            }
        })


        viewModel.clipboardLinkToProduct.observe(viewLifecycleOwner, {
            it?.let {
                if (it.isNotBlank()) {
                    val clipboard: ClipboardManager? = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    val clip = ClipData.newPlainText("WooCommerce Product Link", it)
                    if (clipboard != null && clip != null) {
                        clipboard.setPrimaryClip(clip)
                    }
                }
            }
        })

    }

    private fun setWindowTouchability(working: Boolean) {
        if (working) {
            productEntryView?.productDataProgressBar?.visibility = View.VISIBLE
            // For some reason setting progress bar to visible (and therefor holding frame visible)
            // does not stop user from being able to edit text fields or press buttons so using this
            activity?.getWindow()?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            activity?.getWindow()?.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

        } else {
            productEntryView?.productDataProgressBar?.visibility = View.GONE
            activity?.getWindow()?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            activity?.getWindow()?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

        }
    }

    private fun showSkuConfirmationView() {
        productEntryView?.productSkuConfirmation?.skuConfirmationDisplay?.visibility = View.VISIBLE
        productEntryView?.productAddUpdateButton?.visibility = View.GONE
        productEntryView?.productAddUpdateButton?.isEnabled = false
        productEntryView?.productSkuConfirmation?.skuConfirmationCloseButton?.setOnClickListener {
            productEntryView?.productSkuConfirmation?.skuConfirmationDisplay?.visibility = View.GONE
            // productEntryView?.productAddUpdateButton?.visibility = View.VISIBLE
        }

    }


    private fun updatePhotoList(photoList: ArrayList<String>) {
        // get current count
        val numberPhotos = photoCollectionAdapter.itemCount
        photoCollectionAdapter.setPhotoFileList(photoList)
        // To force recreation of photo fragments
        viewPager.adapter = photoCollectionAdapter
        val newNumberPhotos = photoCollectionAdapter.itemCount
        if (numberPhotos < newNumberPhotos) {
            viewPager.setCurrentItem(newNumberPhotos - 1, true)
        }

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
            if ((resultCode == RESULT_OK || resultCode == -1)
                    && resultData != null
                    && resultData.extras != null) {
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
                .setPositiveButton(android.R.string.ok) { dialog, which ->
                    selectHHGCategoryFile()
                }
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
                .setPositiveButton(R.string.exit) { dialog, which ->
                    requireActivity().finish()
                }
                .setNegativeButton(R.string.try_again) { dialog, which ->
                    showHHGCategorySelectionAlertDialog()
                }
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

    private fun displayStatusMessage(message: String) {
        if (message.isNotBlank()) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayErrorMessage(throwable: Throwable) {
        val alertDialog = AlertDialog.Builder(context)
                .setTitle("OOPS!")
                .setMessage("Please take screen print (hold power and volume-down buttons simultaneously for a couple seconds before hitting the OK button \n\n"
                        + throwable.message
                        + "\n" + throwable.stackTraceToString())
                .setPositiveButton(android.R.string.ok) { dialog, which ->
                    //   resetProduct()
                    //setWindowTouchability(false)
                }
                .setIcon(android.R.drawable.ic_dialog_alert)
                .create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()

    }


}