package com.example.natureobserverapp.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.natureobserverapp.NatureObservationDB
import com.example.natureobserverapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeleteDialogFragment : DialogFragment() {
    private var activityCallBack: DeleteDialogFragmentListener? = null
    private val db by lazy { NatureObservationDB.get(requireActivity().applicationContext) }

    interface DeleteDialogFragmentListener {
        fun onDeleteObservationButtonClick()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityCallBack = context as DeleteDialogFragmentListener
    }

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

                            withContext(Dispatchers.Main) {
                                requireActivity().supportFragmentManager.popBackStack()
                                activityCallBack?.onDeleteObservationButtonClick()
                            }
                        }
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