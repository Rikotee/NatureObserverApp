package com.example.natureobserverapp

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), HomeFragment.HomeFragmentListener {
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

    override fun onNewObservationButtonClick(picturePath: String) {
        val bundle = bundleOf("picPath" to picturePath)
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<NewObservationFragment>(R.id.flFragment, args = bundle)
            addToBackStack(null)
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