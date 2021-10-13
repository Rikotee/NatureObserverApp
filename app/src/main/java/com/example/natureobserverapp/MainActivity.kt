package com.example.natureobserverapp

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.natureobserverapp.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), HomeFragment.HomeFragmentListener {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var picPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        fragmentContainer = findViewById(R.id.fragmentContainer)

        val homeFragment = HomeFragment()
        val mapFragment = MapFragment()
        val listFragment = ListFragment()
        val statisticsFragment = StatisticsFragment()

        setCurrentFragment(homeFragment)

        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> setCurrentFragment(homeFragment)
                R.id.map -> setCurrentFragment(mapFragment)
                R.id.list -> setCurrentFragment(listFragment)
                R.id.statistics -> setCurrentFragment(statisticsFragment)
            }
            true
        }
    }

    override fun onNewObservationButtonClick(picturePath: String, photoURI: Uri) {
        picPath = picturePath
        takePicture.launch(photoURI)
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)

            when (fragment) {
                is HomeFragment -> replace(R.id.fragmentContainer, fragment, "homeFragment")
                is MapFragment -> replace(R.id.fragmentContainer, fragment, "mapFragment")
                is NewObservationFragment -> replace(R.id.fragmentContainer, fragment, "newObservationFragment")
                else -> replace(R.id.fragmentContainer, fragment)
            }
        }
    }

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
            if (result) {
                val bundle = bundleOf("picPath" to picPath)
                supportFragmentManager.commit {
                    setCustomAnimations(
                        R.anim.slide_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out
                    )
                    setReorderingAllowed(true)
                    replace<NewObservationFragment>(R.id.fragmentContainer, args = bundle)
                    addToBackStack(null)
                }
            }
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        if (supportFragmentManager.backStackEntryCount == 0) {
            bottomNavigationView.visibility = View.VISIBLE
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (permissions[0] == android.Manifest.permission.ACCESS_FINE_LOCATION &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            when (requestCode) {
                0 -> {
                    val homeFragment =
                        supportFragmentManager.findFragmentByTag("homeFragment") as HomeFragment
                    homeFragment.checkLocationPermissionAndRequestUpdates()
                }
                1 -> {
                    val mapFragment =
                        supportFragmentManager.findFragmentByTag("mapFragment") as MapFragment
                    mapFragment.checkLocationPermissionAndRequestUpdates()
                }
                2 -> {
                    val newObservationFragment =
                        supportFragmentManager.findFragmentByTag("newObservationFragment") as NewObservationFragment
                    newObservationFragment.checkLocationPermissionAndRequestUpdates()
                }
                else -> return
            }
        }
    }
}