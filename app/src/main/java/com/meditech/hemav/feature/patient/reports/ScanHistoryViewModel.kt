package com.meditech.hemav.feature.patient.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.meditech.hemav.data.model.AnemiaResult
import com.meditech.hemav.data.repository.ScanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(val scans: List<AnemiaResult>) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

class ScanHistoryViewModel : ViewModel() {
    private val scanRepository = ScanRepository()
    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        _uiState.value = HistoryUiState.Loading
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.value = HistoryUiState.Error("User not logged in")
            return
        }

        viewModelScope.launch {
            val scans = scanRepository.getUserScans(userId)
            if (scans.isEmpty()) {
                _uiState.value = HistoryUiState.Success(emptyList()) 
            } else {
                _uiState.value = HistoryUiState.Success(scans)
            }
        }
    }
}
