package com.example.natureobserverapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sm: SensorManager
    private var running = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val homeFragment = HomeFragment()
        val mapFragment = MapFragment()
        val thirdFragment = ThirdFragment()

        setCurrentFragment(homeFragment)

        findViewById<BottomNavigationView>(R.id.bottomNavigationView).setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> setCurrentFragment(homeFragment)
                R.id.map -> setCurrentFragment(mapFragment)
                R.id.settings -> setCurrentFragment(thirdFragment)
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        running = true
        val stepsSensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT)

        if (stepsSensor == null) {
            Toast.makeText(this, "No Light Sensor !", Toast.LENGTH_SHORT).show()
            Log.d("LIGHT", "onResume: no sensor!")
        } else {
            sm.registerListener(this, stepsSensor, SensorManager.SENSOR_DELAY_UI)
            //Log.d("LIGHT", "onResume")
        }
    }

    override fun onPause() {
        super.onPause()
        running = false
        sm.unregisterListener(this)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (running) {
            //lightValue.text = "" + event.values[0]
            Log.d("LIGHT", event.values[0].toString())
        }

/*        when (event.values[0]) {
            in 0.0..3.0 -> infoView.text = getText(R.string.Darkness).toString()
            in 3.0..200.0 -> infoView.text = getText(R.string.Very_dark_overcast_day).toString()
            in 200.0..320.0 -> infoView.text = getText(R.string.Train_station_platforms).toString()
            in 320.0..500.0 -> infoView.text = getText(R.string.Office_lighting).toString()
            in 500.0..1000.0 -> infoView.text =
                getText(R.string.Sunrise_or_sunset_on_a_clear_day).toString()
            in 1000.0..10000.0 -> infoView.text =
                getText(R.string.Overcast_day_or_typical_TV_studio_lighting).toString()
            in 10000.0..25000.0 -> infoView.text =
                getText(R.string.Full_daylight_but_not_direct_sun).toString()
            in 25000.0..100000.0 -> infoView.text = getText(R.string.Direct_sunlight).toString()
        }*/
    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }
}