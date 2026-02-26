package com.meditech.hemav.feature.patient.appointment

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meditech.hemav.data.model.Appointment
import com.meditech.hemav.data.model.AppointmentStatus
import com.meditech.hemav.data.model.AppointmentType
import com.meditech.hemav.data.repository.AppointmentRepository
import com.meditech.hemav.data.repository.AuthRepository
import com.meditech.hemav.service.MedicationScheduler
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BookAppointmentViewModel(
    private val appointmentRepository: AppointmentRepository = AppointmentRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    var isBooking by mutableStateOf(false)
        private set
    
    var bookingSuccess by mutableStateOf(false)
        private set

    fun bookAppointment(
        context: Context,
        doctorId: String,
        doctorName: String,
        type: AppointmentType,
        dateStr: String,
        timeStr: String,
        notes: String,
        patientAge: String,
        patientGender: String,
        patientBloodGroup: String,
        patientWeight: String
    ) {
        val currentUser = authRepository.currentUser ?: return
        val timestamp = parseDateTime(dateStr, timeStr)

        viewModelScope.launch {
            isBooking = true
            
            // Fetch real user profile to get the full name
            val userProfile = authRepository.getUserProfile(currentUser.uid)
            val realPatientName = userProfile?.name ?: currentUser.displayName ?: "Patient"

            val appointment = Appointment(
                patientId = currentUser.uid,
                patientName = realPatientName,
                doctorId = doctorId,
                doctorName = doctorName,
                type = type,
                date = dateStr,
                time = timeStr,
                timestamp = timestamp,
                status = AppointmentStatus.PENDING,
                notes = notes,
                patientAge = patientAge,
                patientGender = patientGender,
                patientBloodGroup = patientBloodGroup,
                patientWeight = patientWeight
            )

            val result = appointmentRepository.createAppointment(appointment)
            if (result.isSuccess) {
                bookingSuccess = true
                // Schedule reminder 1 hour before
                scheduleAppointmentReminder(context, doctorName, timestamp)
            }
            isBooking = false
        }
    }

    private fun scheduleAppointmentReminder(context: Context, doctorName: String, appointmentTime: Long) {
        if (appointmentTime <= 0) return
        
        val reminderTime = appointmentTime - (60 * 60 * 1000) // 1 hour before
        if (reminderTime > System.currentTimeMillis()) {
            MedicationScheduler.scheduleReminder(
                context, 
                "Appointment with $doctorName", 
                "Upcoming in 1 hour", 
                reminderTime
            )
        }
    }

    private fun parseDateTime(dateStr: String, timeStr: String): Long {
        try {
            // "Mon, Feb 17" + " " + "09:00 AM"
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val format = SimpleDateFormat("EEE, MMM dd yyyy hh:mm a", Locale.US)
            val fullString = "$dateStr $currentYear $timeStr"
            val date = format.parse(fullString)
            return date?.time ?: 0L
        } catch (e: Exception) {
            e.printStackTrace()
            return 0L
        }
    }
}
