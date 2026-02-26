package com.meditech.hemav.feature.patient.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.meditech.hemav.data.model.AnemiaResult
import com.meditech.hemav.data.repository.ScanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HealthInsightsViewModel(
    private val scanRepository: ScanRepository = ScanRepository()
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _scanHistory = MutableStateFlow<List<AnemiaResult>>(emptyList())
    val scanHistory: StateFlow<List<AnemiaResult>> = _scanHistory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadInsights() {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            val results = scanRepository.getUserScans(userId)
            // Sort by timestamp ascending for the chart
            _scanHistory.value = results.sortedBy { it.timestamp }
            _isLoading.value = false
        }
    }
}
