package com.genz.secure_share

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.genz.secure_share.utils.NetworkChangeListener
import com.genz.secure_share.utils.NetworkChangeReceiver
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Starting Point
 */
class MainActivity : AppCompatActivity(), OnItemSelectedListener, NetworkChangeListener {

    /**
     * Fragments will Observe this
     */
    val networkState = MutableLiveData<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.e("Today : ", SimpleDateFormat("yyyy-MM-dd",
            Locale.getDefault()).format(Date()).toString())

        registerReceiver(NetworkChangeReceiver(),
            IntentFilter(getString(R.string.CONNECTIVITY_ACTION)))

        NetworkChangeReceiver.networkChangeListener = this

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

    /**
     * when internet state changed,
     * state indicates the status of internet,
     * true if connected
     * false otherwise
     */
    override fun onNetWorkChange(state: Boolean) {
        Log.e("State : ", state.toString())
        lifecycleScope.launch {
            if (state) {
                networkState.postValue(true)
            } else {
                networkState.postValue(false)
            }
        }
    }
}