package com.meditech.hemav.feature.patient.reports

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meditech.hemav.feature.patient.anemia.AnemiaResultsScreen
import com.meditech.hemav.ui.components.HemaVLoader

@Composable
fun ScanDetailScreen(
    scanId: String,
    onFindDoctor: () -> Unit,
    onBack: () -> Unit,
    viewModel: ScanDetailViewModel = viewModel()
) {
    LaunchedEffect(scanId) {
        viewModel.loadScan(scanId)
    }

    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is ScanDetailUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                HemaVLoader()
            }
        }
        is ScanDetailUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
        }
        is ScanDetailUiState.Success -> {
            AnemiaResultsScreen(
                result = state.result,
                onFindDoctor = onFindDoctor,
                onBack = onBack
            )
        }
    }
}
