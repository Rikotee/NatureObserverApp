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
import com.example.natureobserverapp.Categories
import com.example.natureobserverapp.R
import com.example.natureobserverapp.RecyclerViewAdapter
import com.example.natureobserverapp.model.NatureObservationsWithWeatherInfoModel
import java.util.*

class ListFragment : Fragment(), RecyclerViewAdapter.ClickListener {

    private lateinit var listSpinner: Spinner
    private val categoriesList: MutableList<String> = Categories.categories.toMutableList()
    private var spinnerIndex: Int = 0
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

        listSpinner = view.findViewById(R.id.listSpinner)

        val aa = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            addToList()
        )
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        listSpinner.adapter = aa

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
                    observationsRecyclerView.adapter = RecyclerViewAdapter(it.reversed(), this)
                }
            } else {
                if (observationsRecyclerView != null) {
                    observationsRecyclerView.adapter = RecyclerViewAdapter(filtered.reversed(), this)
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
            for (item in oldCategories){
                if (item !in categoriesList) { categoriesList.add(item) }
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
}