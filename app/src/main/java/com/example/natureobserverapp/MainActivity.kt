package com.example.natureobserverapp

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var flFragment: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        flFragment = findViewById(R.id.flFragment)

        val homeFragment = HomeFragment()
        val mapFragment = MapFragment()
        val listFragment = ListFragment()

        setCurrentFragment(homeFragment)

        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> setCurrentFragment(homeFragment)
                R.id.map -> setCurrentFragment(mapFragment)
                R.id.list -> setCurrentFragment(listFragment)
            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment) {
        if (fragment is HomeFragment) {
            if (supportFragmentManager.backStackEntryCount == 0) {
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment)
                    commit()
                }
            } else {
                supportFragmentManager.popBackStack(
                    "navFragment",
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
            }
        } else {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment)
                addToBackStack("navFragment")
                commit()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        when (flFragment.getChildAt(0).tag) {
            "HOME_FRAGMENT" -> bottomNavigationView.selectedItemId = R.id.home
            "MAP_FRAGMENT" -> bottomNavigationView.selectedItemId = R.id.map
            "LIST_FRAGMENT" -> bottomNavigationView.selectedItemId = R.id.list
        }
    }
}