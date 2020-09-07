package org.householdgoods.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import dagger.hilt.android.AndroidEntryPoint
import org.householdgoods.databinding.PhotoImageView


class PhotoCollectionAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    val photoFileList = ArrayList<String>()

    override fun getItemCount(): Int = photoFileList.size

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        val fragment = PhotoFragment()
        fragment.arguments = Bundle().apply {
            // Our object is just an integer :-P
            putInt(POSITION, position + 1)
            putString(PHOTO_FILE_NAME, photoFileList.get(position))
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
    private var photoImageView: PhotoImageView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        photoImageView = PhotoImageView.inflate(inflater, container, false)
        return photoImageView!!.photoConstraintLayout.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        photoImageView?.lifecycleOwner = viewLifecycleOwner
        photoImageView?.viewModel = viewModel
        arguments?.takeIf { it.containsKey(PHOTO_FILE_NAME) }?.apply {
            viewModel.getImage(getString(PHOTO_FILE_NAME)!!)

        }

    }



}