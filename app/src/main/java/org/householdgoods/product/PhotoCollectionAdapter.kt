package org.householdgoods.product

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import dagger.hilt.android.AndroidEntryPoint
import org.householdgoods.R
import org.householdgoods.databinding.PhotoImageView
import java.io.File


class PhotoCollectionAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val photoFileList = ArrayList<String>()

    override fun getItemCount(): Int {
        return photoFileList.size
    }

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        val fragment = PhotoFragment()
        fragment.arguments = Bundle().apply {
            // Our object is just an integer :-P
            putInt(POSITION, position)
            putString(PHOTO_FILE_NAME, photoFileList[position])
        }

        return fragment
    }

    fun setPhotoFileList(photoList: ArrayList<String>) {
        photoFileList.clear()
        photoFileList.addAll(photoList)
        notifyDataSetChanged()
    }

}

private const val POSITION = "object"
private const val PHOTO_FILE_NAME = "photo_file_name"

// Instances of this class are fragments representing a single
// object in our collection.
@AndroidEntryPoint
class PhotoFragment : Fragment() {

    private val viewModel: PhotoViewModel by viewModels()
    private val productEntryViewModel: ProductEntryViewModel by activityViewModels()
    private var photoImageView: PhotoImageView? = null

    private lateinit var circularProgressDrawable: CircularProgressDrawable
    private val requestOptions = RequestOptions()

    @SuppressLint("CheckResult")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        photoImageView = PhotoImageView.inflate(inflater, container, false)

        circularProgressDrawable = CircularProgressDrawable(requireContext())
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        requestOptions.placeholder(circularProgressDrawable)
        requestOptions.error(R.drawable.ic_baseline_error_outline_96)
        requestOptions.skipMemoryCache(true)
        requestOptions.fitCenter()


        return photoImageView!!.photoConstraintLayout.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        photoImageView?.lifecycleOwner = viewLifecycleOwner
        photoImageView?.viewModel = viewModel

        viewModel.photoFile.observe(viewLifecycleOwner, {
            it?.let {
                loadPhoto(it)
            }
        })

        arguments?.takeIf { it.containsKey(PHOTO_FILE_NAME) }?.apply {
            viewModel.getPhotoFile(getString(PHOTO_FILE_NAME)!!)
        }

        photoImageView?.photoDeleteButton?.setOnClickListener {
            deletePhoto()
        }
    }

    private fun deletePhoto() {
        arguments?.takeIf { it.containsKey(PHOTO_FILE_NAME) }?.apply {
            productEntryViewModel.deletePhoto(getString(PHOTO_FILE_NAME)!!)
        }
    }

    fun disableDeletePhotoButton(){
       // photoImageView?.photoDeleteButton?.visibility = View.GONE
        photoImageView?.photoDeleteButton?.isEnabled = false
        photoImageView?.photoDeleteButton?.isClickable = false

    }

    private fun loadPhoto(file: File) {
        val context = photoImageView?.photoItemView?.getContext()
        if (context != null) {
            Glide.with(context)
                    .asBitmap()
                    .load(file)
                    .apply(requestOptions)
                    .into(photoImageView?.photoItemView!!)
        }
    }
}
