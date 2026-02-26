package com.meditech.hemav.feature.patient.medication

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meditech.hemav.data.model.Prescription
import com.meditech.hemav.data.repository.AuthRepository
import com.meditech.hemav.data.repository.PrescriptionRepository
import com.meditech.hemav.service.MedicationScheduler
import kotlinx.coroutines.launch
import java.util.Calendar

class PatientMedicationsViewModel(
    private val prescriptionRepository: PrescriptionRepository = PrescriptionRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    var prescriptions by mutableStateOf<List<Prescription>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set

    fun loadMedications(context: Context) {
        val currentUser = authRepository.currentUser
        if (currentUser == null) {
            Log.e("PatientMedsVM", "User not logged in")
            return
        }

        viewModelScope.launch {
            isLoading = true
            val result = prescriptionRepository.getPrescriptionsForPatient(currentUser.uid)
            
            if (result.isSuccess) {
                val fetched = result.getOrNull() ?: emptyList()
                prescriptions = fetched
                
                // Schedule alarms for today/tomorrow based onfetched meds
                scheduleAlarms(context, fetched)
            } else {
                Log.e("PatientMedsVM", "Failed to load meds: ${result.exceptionOrNull()?.message}")
            }
            isLoading = false
        }
    }

    private fun scheduleAlarms(context: Context, prescriptions: List<Prescription>) {
        // Simple logic for POC: Schedule "Twice daily" meds for 9 AM and 9 PM
        // "Once daily" for 9 AM
        
        prescriptions.flatMap { it.medicines }.forEach { med ->
            val calendar = Calendar.getInstance()
            
            // Morning Dose (9 AM)
            calendar.set(Calendar.HOUR_OF_DAY, 9)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            
            if (calendar.timeInMillis > System.currentTimeMillis()) {
               MedicationScheduler.scheduleReminder(context, med.name, med.dosage, calendar.timeInMillis)
            } else {
                // If 9 AM passed, schedule for tomorrow
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                MedicationScheduler.scheduleReminder(context, med.name, med.dosage, calendar.timeInMillis)
            }

            // Evening Dose (9 PM) if twice/thrice
            if (med.frequency.contains("Twice", ignoreCase = true) || med.frequency.contains("Thrice", ignoreCase = true)) {
                val eveningCal = Calendar.getInstance()
                eveningCal.set(Calendar.HOUR_OF_DAY, 21) // 9 PM
                eveningCal.set(Calendar.MINUTE, 0)
                eveningCal.set(Calendar.SECOND, 0)

                if (eveningCal.timeInMillis > System.currentTimeMillis()) {
                    MedicationScheduler.scheduleReminder(context, med.name, med.dosage, eveningCal.timeInMillis)
                } else {
                     eveningCal.add(Calendar.DAY_OF_YEAR, 1)
                     MedicationScheduler.scheduleReminder(context, med.name, med.dosage, eveningCal.timeInMillis)
                }
            }
        }
    }
}
