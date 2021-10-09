package com.example.natureobserverapp.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.natureobserverapp.*
import com.example.natureobserverapp.model.NatureObservationsWithWeatherInfoModel
import java.text.SimpleDateFormat
import java.util.*

class ListFragment : Fragment(), RecyclerViewAdapter.ClickListener {

    private lateinit var listSpinner: Spinner
    private lateinit var timeFrameFilterSpinner: Spinner
    private val categoriesList: MutableList<String> = Categories.categories.toMutableList()
    private val observationList = mutableListOf<NatureObservationWithWeatherInfo>()
    private var spinnerIndex: Int = 0
    private var timeSpinnerIndex: Int = 0
    private val sharedPrefFile = "sharedpreference"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timeFrameFilterSpinner = view.findViewById(R.id.timeFrameFilterSpinner)
        listSpinner = view.findViewById(R.id.listSpinner)

        val timeFrames = listOf("All time", "This year", "This month", "This week", "Today")

        val aa = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            addToList()
        )
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        listSpinner.adapter = aa

        val aaT = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            timeFrames
        )
        aaT.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeFrameFilterSpinner.adapter = aaT

        setSpinnerValue()

        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.list_title_text)

        listSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                getList()
                updateSpinner()
            }
        }

        setTimeSpinnerValue()

        timeFrameFilterSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    timeSpinnerIndex = position
                    getList()
                    updateTimeSpinner(position)
                }
            }
    }

    private fun getList() {
        val observationsRecyclerView = view?.findViewById<RecyclerView>(R.id.rv_obs_list)
        if (observationsRecyclerView != null) {
            observationsRecyclerView.layoutManager = LinearLayoutManager(this.context)
        }
        val nowwim: NatureObservationsWithWeatherInfoModel by viewModels()
        nowwim.getNatureObservationsWithWeatherInfo().observe(this) { it ->

            val categoryS = listSpinner.selectedItem.toString()
            val filtered = it.filter { categoryS == it.natureObservation?.category ?: 0 }

            if (categoryS == "All") {
                if (observationsRecyclerView != null) {

                    filterObservationsByTimeFrame(timeSpinnerIndex, it)
                    observationsRecyclerView.adapter =
                        RecyclerViewAdapter(observationList.reversed(), this)
                    observationList.clear()
                }
            } else {
                if (observationsRecyclerView != null) {
                    filterObservationsByTimeFrame(timeSpinnerIndex, filtered)
                    observationsRecyclerView.adapter =
                        RecyclerViewAdapter(observationList.reversed(), this)
                    observationList.clear()
                }
            }
        }
    }

    override fun onItemClick(observation: Long?) {
        val bundle = bundleOf("pos" to observation)

        requireActivity().supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<ItemFragment>(R.id.flFragment, args = bundle)
            addToBackStack(null)
        }
    }

    private fun addToList(): MutableList<String> {
        val sharedPreference =
            this.activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

        val newCategoriesSet = HashSet<String>()

        val oldCategories = sharedPreference?.getStringSet(
            "newCategories",
            newCategoriesSet
        )

        for (i in categoriesList.indices) {
            if (categoriesList[0] != "All") {
                categoriesList.add(0, "All")
            }
        }

        if (oldCategories != null) {
            for (item in oldCategories) {
                if (item !in categoriesList) {
                    categoriesList.add(item)
                }
            }
        }
        return categoriesList
    }

    private fun setSpinnerValue() {
        val sharedPreference =
            this.activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val newSpinnerValue = sharedPreference?.getInt("listSpinnerIndex", 0)
        if (newSpinnerValue != null) {
            listSpinner.setSelection(newSpinnerValue)
        }
    }

    private fun updateSpinner() {
        val categoryS = listSpinner.selectedItem.toString()

        for (i in categoriesList.indices) {
            if (categoryS == categoriesList[i]) {
                spinnerIndex = i
            }
        }

        val sharedPreference =
            this.activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor = sharedPreference?.edit()
        editor?.putInt("listSpinnerIndex", spinnerIndex)
        editor?.apply()
    }

    private fun setTimeSpinnerValue() {
        val sharedPreference =
            this.activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val newSpinnerValue = sharedPreference?.getInt("TimeFrameFilterSpinnerIndex", 0)
        if (newSpinnerValue != null) {
            timeSpinnerIndex = newSpinnerValue
            timeFrameFilterSpinner.setSelection(newSpinnerValue)
        }
    }

    private fun updateTimeSpinner(spinnerIndex: Int) {
        val sharedPreference =
            this.activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor = sharedPreference?.edit()
        editor?.putInt("TimeFrameFilterSpinnerIndex", spinnerIndex)
        editor?.apply()
    }

    private fun filterObservationsByTimeFrame(
        spinnerIndex: Int,
        list: List<NatureObservationWithWeatherInfo>
    ): MutableList<NatureObservationWithWeatherInfo> {
        val selectedTimeFrame = timeFrameFilterSpinner.getItemAtPosition(spinnerIndex)

        val currentDateCalendar = Calendar.getInstance()
        val currentYear = currentDateCalendar.get(Calendar.YEAR)
        val currentMonth = currentDateCalendar.get(Calendar.MONTH)
        val currentWeek = currentDateCalendar.get(Calendar.WEEK_OF_YEAR)
        val currentDay = currentDateCalendar.get(Calendar.DAY_OF_YEAR)

        val formatter = SimpleDateFormat("d.M.yyyy hh.mm", Locale.getDefault())
        val observationDateCalendar = Calendar.getInstance()

        for (observation in list) {
            val observationDate = formatter.parse(observation.natureObservation!!.dateAndTime)

            if (observationDate != null) {
                observationDateCalendar.time = observationDate
                val observationYear = observationDateCalendar.get(Calendar.YEAR)
                val observationMonth = observationDateCalendar.get(Calendar.MONTH)
                val observationWeek = observationDateCalendar.get(Calendar.WEEK_OF_YEAR)
                val observationDay = observationDateCalendar.get(Calendar.DAY_OF_YEAR)

                when (selectedTimeFrame) {
                    "This year" -> {
                        if (observationYear == currentYear) {
                            observationList.add(observation)
                        }
                    }
                    "This month" -> {
                        if (observationYear == currentYear && observationMonth == currentMonth) {
                            observationList.add(observation)
                        }
                    }
                    "This week" -> {
                        if (observationYear == currentYear && observationWeek == currentWeek) {
                            observationList.add(observation)
                        }
                    }
                    "Today" -> {
                        if (observationYear == currentYear && observationDay == currentDay) {
                            observationList.add(observation)
                        }
                    }
                    else -> {
                        observationList.add(observation)
                    }
                }
            }
        }
        return observationList
    }
}