package com.meditech.hemav.feature.patient.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.meditech.hemav.data.model.Appointment
import com.meditech.hemav.data.model.AppointmentStatus
import com.meditech.hemav.data.repository.AppointmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class PatientDashboardViewModel : ViewModel() {
    private val appointmentRepository = AppointmentRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _upcomingAppointment = MutableStateFlow<Appointment?>(null)
    val upcomingAppointment: StateFlow<Appointment?> = _upcomingAppointment.asStateFlow()

    private val _hasActiveAppointment = MutableStateFlow(false)
    val hasActiveAppointment: StateFlow<Boolean> = _hasActiveAppointment.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadDashboardData() {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            appointmentRepository.getAppointmentsForPatient(userId)
                .catch { e ->
                    _isLoading.value = false
                    // Handle error
                }
                .collect { appointments ->
                    _hasActiveAppointment.value = appointments.any { 
                        it.status == AppointmentStatus.CONFIRMED || it.status == AppointmentStatus.PENDING 
                    }
                    
                    _upcomingAppointment.value = appointments
                        .filter { it.status == AppointmentStatus.CONFIRMED || it.status == AppointmentStatus.PENDING }
                        .sortedBy { it.timestamp }
                        .firstOrNull { it.timestamp > System.currentTimeMillis() - (2 * 60 * 60 * 1000) }
                        
                    _isLoading.value = false
                }
        }
    }
}
