package org.householdgoods.start

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.householdgoods.BuildConfig
import org.householdgoods.R

import java.util.*

@AndroidEntryPoint
class StartActivity : AppCompatActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        updateTitle()
    }

   private fun updateTitle(){
       var version : String = ""
       val appName = getString(R.string.app_name)
       val buildDate =   Date(BuildConfig.BUILD_TIME.toLong());
       try {
           version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName
       } catch ( e : PackageManager.NameNotFoundException) {
           version = "Undefined"
       }
       val hhgUrl = getString(R.string.householdgoods_url).replace("https://", "")

       getSupportActionBar()?.setTitle(getString (R.string.get_title_and_build_info, appName, version, hhgUrl))

    }




}