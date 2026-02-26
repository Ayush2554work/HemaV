package com.meditech.hemav.feature.doctor.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.meditech.hemav.data.model.Appointment
import com.meditech.hemav.data.model.AppointmentStatus
import com.meditech.hemav.data.repository.AppointmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AppointmentUiState {
    object Loading : AppointmentUiState()
    data class Success(val appointments: List<Appointment>) : AppointmentUiState()
    data class Error(val message: String) : AppointmentUiState()
}

class AppointmentViewModel : ViewModel() {
    private val appointmentRepository = AppointmentRepository()
    private val auth = FirebaseAuth.getInstance()
    
    // UI State for the list of appointments
    private val _uiState = MutableStateFlow<AppointmentUiState>(AppointmentUiState.Loading)
    val uiState: StateFlow<AppointmentUiState> = _uiState.asStateFlow()

    // State for update operations (to show loading indicators on buttons)
    private val _updatingAppointmentId = MutableStateFlow<String?>(null)
    val updatingAppointmentId: StateFlow<String?> = _updatingAppointmentId.asStateFlow()

    fun loadAppointments(isDoctor: Boolean) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.value = AppointmentUiState.Error("User not logged in")
            return
        }

        viewModelScope.launch {
            _uiState.value = AppointmentUiState.Loading
            
            val flow = if (isDoctor) {
                appointmentRepository.getAppointmentsForDoctor(userId)
            } else {
                appointmentRepository.getAppointmentsForPatient(userId)
            }
            
            try {
                flow.collect { appointments ->
                    _uiState.value = AppointmentUiState.Success(appointments)
                }
            } catch (e: Exception) {
                _uiState.value = AppointmentUiState.Error(e.message ?: "Failed to listen to appointments")
            }
        }
    }

    fun updateStatus(appointmentId: String, newStatus: AppointmentStatus) {
        viewModelScope.launch {
            _updatingAppointmentId.value = appointmentId
            val result = appointmentRepository.updateAppointmentStatus(appointmentId, newStatus)
            // No need to manually update local UI state, SnapshotListener will handle it globally
            _updatingAppointmentId.value = null
        }
    }
}
