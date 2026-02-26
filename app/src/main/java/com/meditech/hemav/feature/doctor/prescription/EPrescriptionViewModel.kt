package com.meditech.hemav.feature.doctor.prescription

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meditech.hemav.data.model.Medicine
import com.meditech.hemav.data.model.Prescription
import com.meditech.hemav.data.repository.AuthRepository
import com.meditech.hemav.data.repository.PrescriptionRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EPrescriptionViewModel(
    private val prescriptionRepository: PrescriptionRepository = PrescriptionRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    var isSaving by mutableStateOf(false)
        private set
    
    var saveSuccess by mutableStateOf(false)
        private set
        
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun savePrescription(
        context: android.content.Context,
        patientId: String,
        patientName: String,
        diagnosis: String,
        notes: String,
        medicines: List<MedicineEntry>
    ) {
        val doctorId = authRepository.currentUser?.uid ?: return
        
        viewModelScope.launch {
            isSaving = true
            errorMessage = null
            
            try {
                // 1. Prepare Data
                val dataMedicines = medicines.map { 
                    Medicine(it.name, it.dosage, it.frequency, it.duration, it.instructions) 
                }

                val prescription = Prescription(
                    id = "", // Will be set by repo
                    patientId = patientId,
                    doctorId = doctorId,
                    doctorName = "Dr. ${authRepository.currentUser?.displayName ?: "Ayurvedic Specialist"}",
                    diagnosis = diagnosis,
                    notes = notes,
                    medicines = dataMedicines
                )

                // 2. Generate PDF locally
                val pdfFile = com.meditech.hemav.util.PdfReportGenerator.generatePrescriptionPdf(context, prescription)
                if (pdfFile == null) {
                    errorMessage = "Failed to generate PDF"
                    isSaving = false
                    return@launch
                }

                // 3. Save to Firestore FIRST to get the ID
                val createResult = prescriptionRepository.createPrescription(prescription)
                if (createResult.isFailure) {
                    errorMessage = createResult.exceptionOrNull()?.message ?: "Failed to save record"
                    isSaving = false
                    return@launch
                }
                val prescriptionId = createResult.getOrThrow()

                // 4. Upload PDF to Storage
                val uploadResult = prescriptionRepository.uploadPrescriptionPdf(doctorId, patientId, prescriptionId, pdfFile)
                val finalPdfUrl = if (uploadResult.isSuccess) uploadResult.getOrThrow() else ""

                // 5. Update Record with PDF URL
                if (finalPdfUrl.isNotEmpty()) {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("prescriptions")
                        .document(prescriptionId)
                        .update("pdfUrl", finalPdfUrl)
                        .await()
                }

                // 6. Automated Chat Message
                val chatRepo = com.meditech.hemav.data.repository.ChatRepository()
                val chatId = chatRepo.getChatId(doctorId, patientId)
                val msgText = "Dr. ${authRepository.currentUser?.displayName ?: "Name"} has issued a new E-Prescription for you.\n\n" +
                             "Diagnosis: $diagnosis\n" +
                             "You can view the prescription in your 'My Reports' section or download it here."
                
                chatRepo.sendMessage(
                    chatId = chatId,
                    message = com.meditech.hemav.data.model.ChatMessage(
                        text = msgText,
                        senderId = doctorId,
                        receiverId = patientId,
                        timestamp = System.currentTimeMillis(),
                        mediaType = "prescription",
                        mediaUrl = finalPdfUrl // Link to the PDF
                    )
                )
                
                saveSuccess = true
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unknown error"
            } finally {
                isSaving = false
            }
        }
    }
}
