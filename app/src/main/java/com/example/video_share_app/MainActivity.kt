package com.example.video_share_app

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.video_share_app.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener

class MainActivity : AppCompatActivity(), OnItemSelectedListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        }

        return false
    }
}