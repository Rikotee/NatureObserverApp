package com.example.natureobserverapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val firstFragment = FirstFragment()
        val mapFragment = MapFragment()
        val thirdFragment = ThirdFragment()

        setCurrentFragment(firstFragment)

        findViewById<BottomNavigationView>(R.id.bottomNavigationView).setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> setCurrentFragment(firstFragment)
                R.id.map -> setCurrentFragment(mapFragment)
                R.id.settings -> setCurrentFragment(thirdFragment)
            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }
}