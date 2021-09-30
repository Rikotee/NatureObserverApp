package com.example.natureobserverapp

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.example.natureobserverapp.fragment.HomeFragment
import com.example.natureobserverapp.fragment.ListFragment
import com.example.natureobserverapp.fragment.MapFragment
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
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace(R.id.flFragment, fragment)
                }
            } else {
                supportFragmentManager.popBackStack(
                    supportFragmentManager.getBackStackEntryAt(0).id,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
            }
        } else {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.flFragment, fragment)
                addToBackStack("tabNavigationFragment")
            }
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0 &&
            supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1).name == "tabNavigationFragment"
        ) {
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            bottomNavigationView.selectedItemId = R.id.home
        } else {
            super.onBackPressed()
        }
    }
}