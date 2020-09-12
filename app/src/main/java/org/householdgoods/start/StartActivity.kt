package org.householdgoods.start

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import dagger.hilt.android.AndroidEntryPoint
import org.householdgoods.R
import org.householdgoods.product.ProductEntryFragment

@AndroidEntryPoint
class StartActivity : AppCompatActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


}