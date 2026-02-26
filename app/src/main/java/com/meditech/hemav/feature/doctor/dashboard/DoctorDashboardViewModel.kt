package com.meditech.hemav.feature.doctor.dashboard

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

class DoctorDashboardViewModel : ViewModel() {
    private val appointmentRepository = AppointmentRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _nextPatient = MutableStateFlow<Appointment?>(null)
    val nextPatient: StateFlow<Appointment?> = _nextPatient.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _todayAppointmentsCount = MutableStateFlow(0)
    val todayAppointmentsCount: StateFlow<Int> = _todayAppointmentsCount.asStateFlow()

    private val _pendingAppointmentsCount = MutableStateFlow(0)
    val pendingAppointmentsCount: StateFlow<Int> = _pendingAppointmentsCount.asStateFlow()

    private val _recoveryRate = MutableStateFlow(85) // Baseline
    val recoveryRate: StateFlow<Int> = _recoveryRate.asStateFlow()

    fun loadDashboardData() {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            appointmentRepository.getAppointmentsForDoctor(userId)
                .catch { e ->
                    _isLoading.value = false
                    // Handle error
                }
                .collect { appointments ->
                    _nextPatient.value = appointments
                        .filter { it.status == AppointmentStatus.CONFIRMED }
                        .sortedBy { it.timestamp }
                        .firstOrNull { it.timestamp > System.currentTimeMillis() - (1 * 60 * 60 * 1000) }
                        
                    val todayStart = java.util.Calendar.getInstance().apply {
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    
                    val todayEnd = todayStart + (24 * 60 * 60 * 1000)
                        
                    _todayAppointmentsCount.value = appointments.count {
                        (it.status == AppointmentStatus.CONFIRMED || it.status == AppointmentStatus.COMPLETED) &&
                        it.timestamp in todayStart..todayEnd
                    }
                    
                    _pendingAppointmentsCount.value = appointments.count { 
                        it.status == AppointmentStatus.PENDING 
                    }
                    
                    val total = appointments.size
                    val completed = appointments.count { it.status == AppointmentStatus.COMPLETED }
                    _recoveryRate.value = if (total > 0 && completed > 0) {
                        // Rough mock metric using completed / total ratio + baseline
                        val ratio = (completed.toFloat() / total.toFloat()) * 100
                        (50f + (ratio / 2)).toInt()
                    } else {
                        85 // Default baseline if no appointments
                    }

                    _isLoading.value = false
                }
        }
    }
}
