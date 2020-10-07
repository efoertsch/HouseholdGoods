package org.householdgoods.product

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.householdgoods.R
import org.householdgoods.app.Repository
import org.householdgoods.databinding.PhotoImageView
import timber.log.Timber
import java.io.File
import javax.inject.Inject


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
        // this does not force recreation of fragments. So reassigning adapter to viewpager elsewhere
        notifyDataSetChanged()
    }

}

private const val POSITION = "object"
private const val PHOTO_FILE_NAME = "photo_file_name"

// Instances of this class are fragments representing a single
// object in our collection.
@AndroidEntryPoint
class PhotoFragment : Fragment() {

    @Inject
    lateinit var repository : Repository
    private val productEntryViewModel: ProductEntryViewModel by activityViewModels()
    private var photoImageView: PhotoImageView? = null
    private var errorMessage: MutableLiveData<Throwable> = MutableLiveData()
    var photoFileName : String? = null
    var photoPosition : Int = -1



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

        photoFileName = requireArguments().getString(PHOTO_FILE_NAME)
        photoPosition = requireArguments().getInt(POSITION)
        Timber.d("Added photo : %d  %s", photoPosition,  photoFileName)

        return photoImageView!!.photoConstraintLayout.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        photoImageView?.photoDeleteButton?.setOnClickListener {
            Timber.d("Deleting photo  %s", photoFileName)
            productEntryViewModel.deletePhoto(photoFileName!!)
        }
        loadPhoto()
    }


    fun disableDeletePhotoButton(){
       // photoImageView?.photoDeleteButton?.visibility = View.GONE
        photoImageView?.photoDeleteButton?.isEnabled = false
        photoImageView?.photoDeleteButton?.isClickable = false

    }

    private fun loadPhoto()  {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = try {
                Result.success(repository.getPhotoFile(photoFileName!!))

            } catch (exception: Exception) {
                Result.failure<Exception>(Exception("An error occurred retrieving photo", exception))
            }

            if (result.isSuccess) {
                val file = result.getOrNull() as File?
                if (file != null) {
                    loadPhotoFile(file)
                }
            } else {
                errorMessage.value = result.exceptionOrNull()
            }
        }
    }

    private fun loadPhotoFile(file: File) {
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
