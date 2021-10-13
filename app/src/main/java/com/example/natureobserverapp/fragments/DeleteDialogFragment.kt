package com.example.natureobserverapp.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.natureobserverapp.NatureObservationDB
import com.example.natureobserverapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DeleteDialogFragment : DialogFragment() {
    private val db by lazy { NatureObservationDB.get(requireActivity().applicationContext) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = arguments
        val observationId = args?.getLong("observationId")

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(getString(R.string.delete_dialog_title))
            builder.setMessage(R.string.delete_dialog_text)
                .setPositiveButton(
                    R.string.delete_text
                ) { _, _ ->
                    if (observationId != null) {
                        GlobalScope.launch(Dispatchers.IO) {
                            deleteObservation(observationId)
                        }

                        requireActivity().onBackPressed()
                    }
                }
                .setNegativeButton(
                    R.string.cancel_text
                ) { dialog, _ ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun deleteObservation(observationId: Long) {
        db.natureObservationDao().deleteNatureObservation(observationId)
        db.weatherInfoDao().deleteWeatherInfo(observationId)
    }
}