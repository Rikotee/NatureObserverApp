package com.example.natureobserverapp.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.example.natureobserverapp.PredefinedLists
import com.example.natureobserverapp.NatureObservation
import com.example.natureobserverapp.R
import com.example.natureobserverapp.models.NatureObservationsModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.*

class StatisticsFragment : Fragment() {
    private lateinit var pieChart: PieChart
    private lateinit var timeFrameFilterSpinner: Spinner
    private val categoriesList: MutableList<String> = PredefinedLists.categories.toMutableList()
    private val sharedPrefFile = "sharedpreference"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.statistics_title_text)

        pieChart = view.findViewById(R.id.pieChart)

        timeFrameFilterSpinner = view.findViewById(R.id.pieChartTimeFrameFilterSpinner)

        val aa = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            PredefinedLists.timeFrames
        )
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeFrameFilterSpinner.adapter = aa

        setSpinnerValue()
        addUserAddedCategoriesToCategoriesList()

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
                    filterObservationsByTimeFrame(position)
                    updateSpinner(position)
                }
            }
    }

    private fun filterObservationsByTimeFrame(spinnerIndex: Int) {
        val nom: NatureObservationsModel by viewModels()
        nom.getNatureObservations().observe(this) {
            val observations = it
            val selectedTimeFrame = timeFrameFilterSpinner.getItemAtPosition(spinnerIndex)

            val currentDateCalendar = Calendar.getInstance()
            val currentYear = currentDateCalendar.get(Calendar.YEAR)
            val currentMonth = currentDateCalendar.get(Calendar.MONTH)
            val currentWeek = currentDateCalendar.get(Calendar.WEEK_OF_YEAR)
            val currentDay = currentDateCalendar.get(Calendar.DAY_OF_YEAR)

            val formatter = SimpleDateFormat("d.M.yyyy hh.mm", Locale.getDefault())
            val observationDateCalendar = Calendar.getInstance()
            val filteredObservations = mutableListOf<NatureObservation>()

            for (observation in observations) {
                val observationDate = formatter.parse(observation.dateAndTime)

                if (observationDate != null) {
                    observationDateCalendar.time = observationDate
                    val observationYear = observationDateCalendar.get(Calendar.YEAR)
                    val observationMonth = observationDateCalendar.get(Calendar.MONTH)
                    val observationWeek = observationDateCalendar.get(Calendar.WEEK_OF_YEAR)
                    val observationDay = observationDateCalendar.get(Calendar.DAY_OF_YEAR)

                    when (selectedTimeFrame) {
                        "This year" -> {
                            if (observationYear == currentYear) {
                                filteredObservations.add(observation)
                            }
                        }
                        "This month" -> {
                            if (observationYear == currentYear && observationMonth == currentMonth) {
                                filteredObservations.add(observation)
                            }
                        }
                        "This week" -> {
                            if (observationYear == currentYear && observationWeek == currentWeek) {
                                filteredObservations.add(observation)
                            }
                        }
                        "Today" -> {
                            if (observationYear == currentYear && observationDay == currentDay) {
                                filteredObservations.add(observation)
                            }
                        }
                        else -> {
                            filteredObservations.add(observation)
                        }
                    }
                }
            }

            createPieChart(filteredObservations)
        }
    }

    private fun createPieChart(observations: List<NatureObservation>) {
        pieChart.description.isEnabled = false
        pieChart.animateY(1000, Easing.EaseInOutQuad)
        pieChart.legend.isEnabled = false
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(16f)
        pieChart.isRotationEnabled = false
        pieChart.isHighlightPerTapEnabled = false
        pieChart.setExtraOffsets(10f, 0f, 10f, 0f)
        pieChart.setHoleColor(Color.parseColor("#F1F1F1"))

        val categories = categoriesList.toMutableList()
        val observationCategoryCountsList = MutableList(categories.size) { 0 }
        val indicesOfZeroValue = mutableListOf<Int>()

        for (i in categories.indices) {
            for (observation in observations) {
                if (observation.category == categories[i]) {
                    val sumOfObs = observationCategoryCountsList[i] + 1
                    observationCategoryCountsList[i] = sumOfObs
                }
            }

            if (observationCategoryCountsList[i] == 0) {
                indicesOfZeroValue.add(i)
            }
        }

        var indexSubtractionValue = 0

        for (i in indicesOfZeroValue.indices) {
            if (i > 0) {
                indexSubtractionValue++
            }

            observationCategoryCountsList.removeAt(indicesOfZeroValue[i] - indexSubtractionValue)
            categories.removeAt(indicesOfZeroValue[i] - indexSubtractionValue)
        }

        val numberOfObservationsByCategory = mutableListOf<PieEntry>()

        for (i in observationCategoryCountsList.indices) {
            numberOfObservationsByCategory.add(
                PieEntry(
                    observationCategoryCountsList[i].toFloat(), categories[i]
                )
            )
        }

        val dataSet = PieDataSet(numberOfObservationsByCategory, "Observations")
        val colors = mutableListOf<Int>()

        for (color in ColorTemplate.MATERIAL_COLORS) {
            colors.add(color)
        }

        for (color in ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color)
        }

        dataSet.colors = colors
        val data = PieData(dataSet)
        data.setValueFormatter(DefaultValueFormatter(0))
        data.setValueTextSize(14f)
        pieChart.data = data
        pieChart.invalidate()
    }

    private fun addUserAddedCategoriesToCategoriesList() {
        val sharedPreference =
            this.activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

        val newCategoriesSet = HashSet<String>()

        val oldCategories = sharedPreference?.getStringSet(
            "newCategories",
            newCategoriesSet
        )

        if (oldCategories != null) {
            for (item in oldCategories) {
                if (item !in categoriesList) {
                    categoriesList.add(item)
                }
            }
        }
    }

    private fun setSpinnerValue() {
        val sharedPreference =
            this.activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val newSpinnerValue = sharedPreference?.getInt("statisticsTimeFrameFilterSpinnerIndex", 0)
        if (newSpinnerValue != null) {
            timeFrameFilterSpinner.setSelection(newSpinnerValue)
        }
    }

    private fun updateSpinner(spinnerIndex: Int) {
        val sharedPreference =
            this.activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor = sharedPreference?.edit()
        editor?.putInt("statisticsTimeFrameFilterSpinnerIndex", spinnerIndex)
        editor?.apply()
    }
}