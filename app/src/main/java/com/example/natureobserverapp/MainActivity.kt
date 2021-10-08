package com.example.natureobserverapp

import android.net.Uri
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.natureobserverapp.fragment.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), HomeFragment.HomeFragmentListener {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var flFragment: FrameLayout
    private lateinit var picPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        flFragment = findViewById(R.id.flFragment)

        val homeFragment = HomeFragment()
        val mapFragment = MapFragment()
        val listFragment = ListFragment()
        val chartFragment = ChartFragment()

        setCurrentFragment(homeFragment)

        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> setCurrentFragment(homeFragment)
                R.id.map -> setCurrentFragment(mapFragment)
                R.id.list -> setCurrentFragment(listFragment)
                R.id.chart -> setCurrentFragment(chartFragment)
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
            replace(R.id.flFragment, fragment)
        }
    }

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
            if (result) {
                val bundle = bundleOf("picPath" to picPath)
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace<NewObservationFragment>(R.id.flFragment, args = bundle)
                    addToBackStack(null)
                }
            }
        }
}