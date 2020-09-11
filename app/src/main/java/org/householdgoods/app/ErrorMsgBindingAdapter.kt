package org.householdgoods.app

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout


class ErrorMsgBindingAdapter {
    // Currently used to set error msg in TextInputLayout
    companion object {
        @BindingAdapter("app:errorText")
        @JvmStatic
        fun setErrorMessage(view: TextInputLayout, errorMessage: String?) {
            view.error = errorMessage
        }
    }
}


