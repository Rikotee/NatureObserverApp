package com.example.natureobserverapp.fragments

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
import com.example.natureobserverapp.models.NatureObservationsModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

class ListFragment : Fragment(), RecyclerViewAdapter.ClickListener {

    private lateinit var listSpinner: Spinner
    private lateinit var timeFrameFilterSpinner: Spinner
    private val categoriesList: MutableList<String> = PredefinedLists.categories.toMutableList()
    private val observationList = mutableListOf<NatureObservation>()
    private var spinnerIndex: Int = 0
    private var timeSpinnerIndex: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)?.visibility =
            View.VISIBLE
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.list_title_text)

        timeFrameFilterSpinner = view.findViewById(R.id.timeFrameFilterSpinner)
        listSpinner = view.findViewById(R.id.listSpinner)

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
            PredefinedLists.timeFrames
        )
        aaT.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeFrameFilterSpinner.adapter = aaT

        setSpinnerValue()

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

    // Observations are filtered in the list based on the category and time frame filter spinners
    private fun getList() {
        val observationsRecyclerView = view?.findViewById<RecyclerView>(R.id.rv_obs_list)

        if (observationsRecyclerView != null) {
            observationsRecyclerView.layoutManager = LinearLayoutManager(this.context)
        }

        val nom: NatureObservationsModel by viewModels()
        nom.getNatureObservations().observe(this) { it ->
            val categoryS = listSpinner.selectedItem.toString()
            val filtered = it.filter { categoryS == it.category }

            if (observationsRecyclerView != null) {
                if (categoryS == "All categories") {
                    filterObservationsByTimeFrame(timeSpinnerIndex, it)
                } else {
                    filterObservationsByTimeFrame(timeSpinnerIndex, filtered)
                }

                observationsRecyclerView.adapter =
                    RecyclerViewAdapter(observationList.reversed(), this)
                observationList.clear()
            }
        }
    }

    override fun onItemClick(observation: Long?) {
        val bundle = bundleOf("pos" to observation)

        requireActivity().supportFragmentManager.commit {
            setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out
            )
            setReorderingAllowed(true)
            replace<ItemFragment>(R.id.fragmentContainer, args = bundle)
            addToBackStack(null)
        }
    }

    // User added categories are fetched from Shared Preferences and added to the categories list
    private fun addToList(): MutableList<String> {
        val newCategoriesSet = HashSet<String>()

        val oldCategories = SharedPreferencesFunctions.getSharedPreferenceStringSet(
            requireActivity(),
            "newCategories", newCategoriesSet
        )

        for (i in categoriesList.indices) {
            if (categoriesList[0] != "All categories") {
                categoriesList.add(0, "All categories")
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

    // Spinner index is fetched from Shared Preferences and the spinner selection is set
    private fun setSpinnerValue() {
        val newSpinnerValue = SharedPreferencesFunctions.getSharedPreferenceIndexValue(
            requireActivity(),
            "listCategorySpinnerIndex", 0
        )

        if (newSpinnerValue != null) {
            listSpinner.setSelection(newSpinnerValue)
        }
    }

    // The selected spinner value index is saved in Shared Preferences
    private fun updateSpinner() {
        val categoryS = listSpinner.selectedItem.toString()

        for (i in categoriesList.indices) {
            if (categoryS == categoriesList[i]) {
                spinnerIndex = i
            }
        }

        SharedPreferencesFunctions.putSharedPreferenceIndexValue(
            requireActivity(),
            "listCategorySpinnerIndex", spinnerIndex
        )
    }

    // Spinner index is fetched from Shared Preferences and the spinner selection is set
    private fun setTimeSpinnerValue() {
        val newSpinnerValue = SharedPreferencesFunctions.getSharedPreferenceIndexValue(
            requireActivity(),
            "listTimeFrameFilterSpinnerIndex", 0
        )

        if (newSpinnerValue != null) {
            timeSpinnerIndex = newSpinnerValue
            timeFrameFilterSpinner.setSelection(newSpinnerValue)
        }
    }

    // The selected spinner value index is saved in Shared Preferences
    private fun updateTimeSpinner(spinnerIndex: Int) {
        SharedPreferencesFunctions.putSharedPreferenceIndexValue(
            requireActivity(),
            "listTimeFrameFilterSpinnerIndex", spinnerIndex
        )
    }

    // The observation list is filtered based on the selected time frame
    private fun filterObservationsByTimeFrame(
        spinnerIndex: Int,
        list: List<NatureObservation>
    ): MutableList<NatureObservation> {
        val selectedTimeFrame = timeFrameFilterSpinner.getItemAtPosition(spinnerIndex).toString()
        val filteredList = TimeFrameFilter.filterObservationsByTimeFrame(list, selectedTimeFrame)
        observationList.addAll(filteredList)
        return observationList
    }
}