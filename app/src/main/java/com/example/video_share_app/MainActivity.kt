package com.example.video_share_app

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
import java.text.SimpleDateFormat
import java.util.*

/**
 * Starting Point
 */
class MainActivity : AppCompatActivity(), OnItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.e("Today : ", SimpleDateFormat("yyyy-MM-dd",
            Locale.getDefault()).format(Date()).toString())

        loadFragment(UploadFragment())

        val navigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        navigationView.setOnItemSelectedListener(this@MainActivity)
    }

     fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.relativelayout, fragment).commit()
     }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.title) {
            getString(R.string.file_menu_title) -> {
                loadFragment(FilesFragment())
                return true
            }

            getString(R.string.upload_icon_title) -> {
                loadFragment(UploadFragment())
                return true
            }

            getString(R.string.view) -> {
                loadFragment(ViewFile())
                return true
            }
        }

        return false
    }
}