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

sealed class ScanDetailUiState {
    object Loading : ScanDetailUiState()
    data class Success(val result: AnemiaResult) : ScanDetailUiState()
    data class Error(val message: String) : ScanDetailUiState()
}

class ScanDetailViewModel : ViewModel() {
    private val scanRepository = ScanRepository()
    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow<ScanDetailUiState>(ScanDetailUiState.Loading)
    val uiState: StateFlow<ScanDetailUiState> = _uiState.asStateFlow()

    fun loadScan(scanId: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.value = ScanDetailUiState.Error("User not logged in")
            return
        }

        viewModelScope.launch {
            _uiState.value = ScanDetailUiState.Loading
            val scan = scanRepository.getScanById(userId, scanId)
            if (scan != null) {
                _uiState.value = ScanDetailUiState.Success(scan)
            } else {
                _uiState.value = ScanDetailUiState.Error("Scan not found")
            }
        }
    }
}
