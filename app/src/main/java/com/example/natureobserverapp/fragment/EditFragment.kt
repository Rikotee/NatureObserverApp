package com.example.natureobserverapp.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.example.natureobserverapp.Categories
import com.example.natureobserverapp.NatureObservationDB
import com.example.natureobserverapp.R
import com.example.natureobserverapp.model.NatureObservationModel
import com.example.natureobserverapp.model.NatureObservationModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class EditFragment : Fragment() {
    var observationId: Long? = null
    private lateinit var titleEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var categoryEditText: EditText
    private lateinit var descriptionEditText: EditText
    private val sharedPrefFile = "sharedpreference"
    private val categoriesList: MutableList<String> = Categories.categories.toMutableList()
    private val db by lazy { NatureObservationDB.get(requireActivity().applicationContext) }
    private var usePredefinedCategory = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observationId = requireArguments().getLong("observationId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.edit_title_text)

        titleEditText = view.findViewById(R.id.editFragmentTitleEditText)
        categorySpinner = view.findViewById(R.id.editFragmentCategorySpinner)
        categoryEditText = view.findViewById(R.id.editFragmentCategoryEditText)
        descriptionEditText = view.findViewById(R.id.editFragmentDescriptionEditText)

        val aa = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            getCategoriesListWithAddedCategories()
        )
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = aa

        setCurrentObservationValues()

        view.findViewById<Button>(R.id.editFragmentSaveButton).setOnClickListener {
            if (titleEditText.text.isEmpty()) {
                Toast.makeText(
                    context,
                    R.string.empty_title_edit_text_toast,
                    Toast.LENGTH_SHORT
                ).show()
            } else if (!usePredefinedCategory && categoryEditText.text.isEmpty()) {
                Toast.makeText(
                    context,
                    R.string.empty_category_edit_text_toast,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                getDataAndSave()
                requireActivity().supportFragmentManager.popBackStack()
            }
        }

        view.findViewById<RadioGroup>(R.id.editFragmentCategoryOptionRadioGroup)
            .setOnCheckedChangeListener { group, checkedId ->
                if (checkedId == R.id.editFragmentSelectCategoryRadioButton) {
                    categoryEditText.visibility = View.GONE
                    categorySpinner.visibility = View.VISIBLE
                    categoryEditText.text.clear()
                    usePredefinedCategory = true
                } else {
                    categorySpinner.visibility = View.GONE
                    categoryEditText.visibility = View.VISIBLE
                    usePredefinedCategory = false
                }
            }
    }

    private fun setCurrentObservationValues() {
        if (observationId != null) {
            val nom: NatureObservationModel by viewModels {
                NatureObservationModelFactory(
                    requireActivity().application,
                    observationId!!
                )
            }

            nom.getNatureObservation().observe(viewLifecycleOwner) {
                titleEditText.setText(it.title)

                val currentCategory = it.category

                for (i in categoriesList.indices) {
                    if (categoriesList[i] == currentCategory) {
                        categorySpinner.setSelection(i)
                        break
                    }
                }

                descriptionEditText.setText(it.description)
            }
        }
    }

    private fun getDataAndSave() {
        val title = titleEditText.text.toString()
        val category: String

        if (usePredefinedCategory) {
            category = categorySpinner.selectedItem.toString()
        } else {
            category = categoryEditText.text.toString()

            val newCategoriesSet = HashSet<String>()

            val sharedPreference =
                this.activity?.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

            val categoriesSet = sharedPreference?.getStringSet("newCategories", newCategoriesSet)

            if (categoriesSet != null) {
                newCategoriesSet.addAll(categoriesSet)
            }

            newCategoriesSet.add(category)

            val editor = sharedPreference?.edit()
            editor?.putStringSet("newCategories", newCategoriesSet)
            editor?.apply()
        }

        val description = descriptionEditText.text.toString()

        if (observationId != null) {
            GlobalScope.launch(Dispatchers.IO) {
                db.natureObservationDao()
                    .updateNatureObservationDetails(observationId!!, title, category, description)
            }
        }
    }

    private fun getCategoriesListWithAddedCategories(): MutableList<String> {
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

        return categoriesList
    }
}