package com.example.natureobserverapp

import java.text.SimpleDateFormat
import java.util.*

object TimeFrameFilter {
    fun filterObservationsByTimeFrame(
        observations: List<NatureObservation>,
        selectedTimeFrame: String
    ): List<NatureObservation> {
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

        return filteredObservations
    }
}