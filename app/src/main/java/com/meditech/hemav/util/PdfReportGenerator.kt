package com.meditech.hemav.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import com.meditech.hemav.data.model.AnemiaResult
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Generates a multi-page, professional PDF report for HemaV Anemia Screening.
 * Includes patient photos, per-image findings, AI explanation, and Ayurvedic insights.
 */
object PdfReportGenerator {

    private const val PAGE_WIDTH = 595  // A4 width in points
    private const val PAGE_HEIGHT = 842 // A4 height in points
    private const val MARGIN = 40f
    private const val CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN.toInt()
    private const val BOTTOM_SAFE = 60f // Footer zone

    // Colors
    private val PRIMARY = Color.rgb(156, 17, 47)
    private val DARK_GRAY = Color.DKGRAY
    private val LIGHT_GRAY = Color.LTGRAY
    private val GREEN = Color.rgb(46, 125, 50)

    fun generateReport(
        context: Context,
        result: AnemiaResult,
        patientName: String,
        bitmaps: List<Bitmap>
    ): File? {
        val pdfDocument = PdfDocument()
        var pageNumber = 1
        var page = pdfDocument.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        )
        var canvas = page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var y = MARGIN

        // Helper: check if we need a new page, finish current, start new
        fun ensureSpace(needed: Float): Boolean {
            if (y + needed > PAGE_HEIGHT - BOTTOM_SAFE) {
                // Draw page number footer
                drawPageFooter(canvas, paint, pageNumber)
                pdfDocument.finishPage(page)
                pageNumber++
                page = pdfDocument.startPage(
                    PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                )
                canvas = page.canvas
                y = MARGIN
                return true
            }
            return false
        }

        // Helper: draw wrapped text, returns height used
        fun drawWrappedText(
            text: String,
            x: Float,
            startY: Float,
            width: Int,
            textSize: Float = 11f,
            color: Int = Color.BLACK,
            bold: Boolean = false
        ): Float {
            val tp = TextPaint(Paint.ANTI_ALIAS_FLAG)
            tp.textSize = textSize
            tp.color = color
            tp.typeface = if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                          else Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val layout = StaticLayout.Builder.obtain(text, 0, text.length, tp, width).build()
            canvas.save()
            canvas.translate(x, startY)
            layout.draw(canvas)
            canvas.restore()
            return layout.height.toFloat()
        }

        // ===================== PAGE 1: HEADER & RESULTS =====================

        // --- HEADER ---
        paint.color = PRIMARY
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("HemaV", MARGIN, y + 10, paint)

        paint.textSize = 12f
        paint.color = DARK_GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("AI-Powered Anemia Screening Report", MARGIN + 90, y + 10, paint)

        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(result.timestamp))
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(dateStr, PAGE_WIDTH - MARGIN, y + 10, paint)
        paint.textAlign = Paint.Align.LEFT

        y += 30f

        // Decorative line
        paint.color = PRIMARY
        paint.strokeWidth = 2f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paint)
        y += 25f

        // --- PATIENT INFO ---
        paint.color = DARK_GRAY
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        val displayName = if (result.patientName.isNotBlank()) result.patientName else patientName
        canvas.drawText("Patient: $displayName", MARGIN, y, paint)
        canvas.drawText("Scan ID: ${result.id.take(12)}", MARGIN + 280, y, paint)
        y += 15f

        // Patient demographics row
        val demoInfo = mutableListOf<String>()
        if (result.patientAge > 0) demoInfo.add("Age: ${result.patientAge}")
        if (result.patientGender.isNotBlank()) demoInfo.add("Gender: ${result.patientGender}")
        demoInfo.add("Provider: ${result.providerUsed}")

        canvas.drawText(demoInfo.joinToString("  •  "), MARGIN, y, paint)
        y += 20f

        // --- FULL PATIENT DETAILS TABLE ---
        if (result.patientDetailsJson.isNotBlank()) {
            try {
                val gson = com.google.gson.Gson()
                val details = gson.fromJson(result.patientDetailsJson, com.meditech.hemav.data.model.PatientDetails::class.java)

                // Draw a subtle box for patient details
                val detailLines = mutableListOf<Pair<String, String>>()
                if (details.ethnicity.isNotBlank()) detailLines.add("Ethnicity" to details.ethnicity)
                if (details.region.isNotBlank()) detailLines.add("Region" to details.region)
                if (details.weight > 0f) detailLines.add("Weight" to "${details.weight} kg")
                if (details.dietType.isNotBlank()) detailLines.add("Diet" to details.dietType)
                if (details.knownConditions.isNotBlank()) detailLines.add("Medical Conditions" to details.knownConditions)
                if (details.currentSymptoms.isNotBlank()) detailLines.add("Symptoms" to details.currentSymptoms)
                if (details.menstrualHistory.isNotBlank() && details.gender == "Female") detailLines.add("Menstrual Status" to details.menstrualHistory)
                if (details.previousAnemia) detailLines.add("Previous Anemia" to "Yes")
                if (details.currentMedications.isNotBlank()) detailLines.add("Medications" to details.currentMedications)

                if (detailLines.isNotEmpty()) {
                    val detailBoxHeight = 18f * detailLines.size + 30f
                    ensureSpace(detailBoxHeight)

                    // Label
                    paint.color = PRIMARY
                    paint.textSize = 10f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("PATIENT DETAILS", MARGIN, y, paint)
                    y += 14f

                    // Detail rows
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.textSize = 10f
                    for ((label, value) in detailLines) {
                        paint.color = Color.DKGRAY
                        canvas.drawText("$label:", MARGIN + 10, y, paint)
                        paint.color = Color.BLACK
                        canvas.drawText(value, MARGIN + 140, y, paint)
                        y += 15f
                    }
                    y += 10f
                }
            } catch (_: Exception) {
                // If JSON parsing fails, just skip the details section
            }
        }

        y += 5f

        // --- SCREENING RESULT BOX ---
        val boxHeight = 90f
        val boxRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + boxHeight)

        // Background
        paint.color = Color.rgb(250, 245, 245)
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(boxRect, 8f, 8f, paint)

        // Border
        paint.color = PRIMARY
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawRoundRect(boxRect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        // Hemoglobin
        paint.textSize = 12f
        paint.color = DARK_GRAY
        canvas.drawText("Estimated Hemoglobin", MARGIN + 20, y + 25, paint)
        paint.textSize = 32f
        paint.color = PRIMARY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("%.1f g/dL".format(result.hemoglobinEstimate), MARGIN + 20, y + 65, paint)

        // Stage
        paint.textSize = 12f
        paint.color = DARK_GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Stage", MARGIN + 280, y + 25, paint)
        paint.textSize = 28f
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(result.stage.label, MARGIN + 280, y + 60, paint)

        // Confidence
        paint.textSize = 12f
        paint.color = DARK_GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Confidence: %.0f%%".format(result.confidence * 100), MARGIN + 280, y + 80, paint)

        y += boxHeight + 25f

        // --- CAPTURED IMAGES ---
        if (bitmaps.isNotEmpty()) {
            paint.color = PRIMARY
            paint.textSize = 16f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Visual Evidence", MARGIN, y, paint)
            y += 15f

            val labels = listOf("Face", "Tongue", "Lower Eyelid", "Palm / Wrist", "Nail Beds")
            val imgSize = 90
            val spacing = ((CONTENT_WIDTH - (5 * imgSize)) / 4f).coerceAtLeast(5f)

            bitmaps.take(5).forEachIndexed { index, bitmap ->
                val x = MARGIN + index * (imgSize + spacing)
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, imgSize, imgSize, true)

                // Draw rounded image frame
                val imgRect = RectF(x, y, x + imgSize, y + imgSize)
                paint.color = LIGHT_GRAY
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1f
                canvas.drawRoundRect(imgRect, 6f, 6f, paint)
                paint.style = Paint.Style.FILL

                canvas.drawBitmap(scaledBitmap, x, y, null)

                // Label
                val label = labels.getOrElse(index) { "" }
                paint.color = DARK_GRAY
                paint.textSize = 9f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText(label, x + imgSize / 2f, y + imgSize + 12, paint)
                paint.textAlign = Paint.Align.LEFT
            }
            y += imgSize + 30f
        }

        // --- AI EXPLANATION ---
        ensureSpace(80f)
        paint.color = PRIMARY
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("AI Analysis Summary", MARGIN, y, paint)
        y += 15f

        val explHeight = drawWrappedText(result.explanation, MARGIN, y, CONTENT_WIDTH, 11f)
        y += explHeight + 20f

        // --- PER-IMAGE FINDINGS ---
        if (result.perImageFindings.isNotEmpty()) {
            ensureSpace(40f)
            paint.color = PRIMARY
            paint.textSize = 16f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Per-Image Findings", MARGIN, y, paint)
            y += 18f

            val findingLabels = mapOf(
                "face" to "Face",
                "tongue" to "Tongue",
                "conjunctiva" to "Lower Eyelid",
                "palm" to "Palm / Wrist",
                "nails" to "Fingernail Beds"
            )

            result.perImageFindings.forEach { (key, value) ->
                val label = findingLabels[key.lowercase()] ?: key.replaceFirstChar { it.uppercase() }
                val text = "$label: $value"

                // Check space, might need new page  
                ensureSpace(50f)

                val h = drawWrappedText("• $text", MARGIN + 10, y, CONTENT_WIDTH - 20, 10.5f)
                y += h + 8f
            }
            y += 10f
        }

        // ===================== PAGE 2+: AYURVEDIC INSIGHTS =====================
        if (result.ayurvedicInsights.isNotEmpty()) {
            ensureSpace(80f)

            // Section header
            paint.color = GREEN
            paint.textSize = 18f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Ayurvedic Insights", MARGIN, y, paint)
            y += 8f

            // Decorative green line
            paint.color = GREEN
            paint.strokeWidth = 1.5f
            canvas.drawLine(MARGIN, y, MARGIN + 150, y, paint)
            y += 18f

            val insightLabels = mapOf(
                "dosha_assessment" to "Dosha Assessment",
                "dietary_recommendations" to "Dietary Recommendations",
                "herbal_remedies" to "Herbal Remedies",
                "lifestyle_tips" to "Lifestyle Tips",
                "home_remedies" to "Home Remedies"
            )

            result.ayurvedicInsights.forEach { (key, value) ->
                val label = insightLabels[key.lowercase()] ?: key.replace("_", " ").replaceFirstChar { it.uppercase() }

                ensureSpace(60f)

                // Draw green sub-header
                val headerH = drawWrappedText(label, MARGIN, y, CONTENT_WIDTH, 13f, GREEN, bold = true)
                y += headerH + 4f

                // Draw content
                val contentH = drawWrappedText(value, MARGIN + 10, y, CONTENT_WIDTH - 20, 10.5f, DARK_GRAY)
                y += contentH + 12f

                // Separator line
                paint.color = Color.rgb(200, 230, 200)
                paint.strokeWidth = 0.5f
                canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paint)
                y += 10f
            }
        }

        // --- DISCLAIMER ---
        ensureSpace(60f)
        y += 10f
        paint.color = LIGHT_GRAY
        paint.strokeWidth = 1f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paint)
        y += 15f

        paint.color = Color.RED
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DISCLAIMER", MARGIN, y, paint)
        y += 12f
        
        val disclaimerText = "This is an AI-assisted screening report, NOT a medical diagnosis. " +
            "Please consult a certified medical professional for accurate diagnosis and treatment. " +
            "Ayurvedic recommendations are for informational purposes only and should be followed under guidance of a qualified practitioner."
        val disclaimerH = drawWrappedText(disclaimerText, MARGIN, y, CONTENT_WIDTH, 9f, DARK_GRAY)
        y += disclaimerH + 10f

        // Final page footer
        drawPageFooter(canvas, paint, pageNumber)
        pdfDocument.finishPage(page)

        // --- Save File ---
        val fileName = "HemaV_Report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            Log.d("PdfGenerator", "PDF saved to ${file.absolutePath}")
            pdfDocument.close()
            file
        } catch (e: IOException) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    private fun drawPageFooter(canvas: Canvas, paint: Paint, pageNumber: Int) {
        paint.color = LIGHT_GRAY
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(
            "HemaV by Meditech AI  •  Page $pageNumber  •  Generated on ${
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            }",
            PAGE_WIDTH / 2f,
            PAGE_HEIGHT - 20f,
            paint
        )
        paint.textAlign = Paint.Align.LEFT
    }

    fun generatePrescriptionPdf(
        context: Context,
        prescription: com.meditech.hemav.data.model.Prescription
    ): File? {
        val pdfDocument = PdfDocument()
        val pageNumber = 1
        val page = pdfDocument.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        )
        val canvas = page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var y = MARGIN

        // Helper
        fun drawWrappedText(text: String, x: Float, startY: Float, width: Int, textSize: Float = 11f, color: Int = Color.BLACK, bold: Boolean = false): Float {
            val tp = TextPaint(Paint.ANTI_ALIAS_FLAG)
            tp.textSize = textSize
            tp.color = color
            tp.typeface = if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val layout = StaticLayout.Builder.obtain(text, 0, text.length, tp, width).build()
            canvas.save()
            canvas.translate(x, startY)
            layout.draw(canvas)
            canvas.restore()
            return layout.height.toFloat()
        }

        // --- HEADER ---
        paint.color = PRIMARY
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("HemaV", MARGIN, y + 10, paint)

        paint.textSize = 12f
        paint.color = DARK_GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Digital E-Prescription", MARGIN + 100, y + 10, paint)

        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(prescription.createdAt))
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(dateStr, PAGE_WIDTH - MARGIN, y + 10, paint)
        paint.textAlign = Paint.Align.LEFT

        y += 30f

        // Decorative line
        paint.color = PRIMARY
        paint.strokeWidth = 2f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paint)
        y += 25f

        // --- DOCTOR & PATIENT INFO ---
        paint.color = DARK_GRAY
        paint.textSize = 12f

        canvas.drawText("Doctor: ${prescription.doctorName}", MARGIN, y, paint)
        canvas.drawText("Patient ID: ${prescription.patientId.take(12)}", MARGIN + 280, y, paint)
        y += 15f
        canvas.drawText("Prescription ID: ${prescription.id.take(12)}", MARGIN, y, paint)
        y += 25f

        // --- DIAGNOSIS ---
        paint.color = PRIMARY
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Diagnosis", MARGIN, y, paint)
        y += 18f
        
        val diagHeight = drawWrappedText(prescription.diagnosis, MARGIN, y, CONTENT_WIDTH, 12f, DARK_GRAY)
        y += diagHeight + 25f

        // --- MEDICINES ---
        paint.color = PRIMARY
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Prescribed Medicines", MARGIN, y, paint)
        y += 18f

        paint.color = Color.BLACK
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        prescription.medicines.forEachIndexed { index, med ->
            // Bullet point
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("${index + 1}. ${med.name}", MARGIN, y, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            y += 15f
            
            val medDetails = "Dosage: ${med.dosage}  |  Frequency: ${med.frequency}  |  Duration: ${med.duration}"
            canvas.drawText(medDetails, MARGIN + 15, y, paint)
            y += 15f
            
            canvas.drawText("Instructions: ${med.instructions}", MARGIN + 15, y, paint)
            y += 20f
        }
        y += 10f

        // --- NOTES ---
        if (prescription.notes.isNotBlank()) {
            paint.color = PRIMARY
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Additional Notes / Advice", MARGIN, y, paint)
            y += 18f
            
            val notesHeight = drawWrappedText(prescription.notes, MARGIN, y, CONTENT_WIDTH, 12f, DARK_GRAY)
            y += notesHeight + 25f
        }

        // --- FOOTER SIGNATURE ---
        y = PAGE_HEIGHT - BOTTOM_SAFE - 40f
        paint.color = DARK_GRAY
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Electronically signed by ${prescription.doctorName}", MARGIN, y, paint)
        
        y += 15f
        paint.color = LIGHT_GRAY
        paint.strokeWidth = 1f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, paint)

        // Final page footer
        drawPageFooter(canvas, paint, pageNumber)
        pdfDocument.finishPage(page)

        // --- Save File ---
        val fileName = "Prescription_${prescription.id.take(8)}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            Log.d("PdfGenerator", "Prescription PDF saved to ${file.absolutePath}")
            pdfDocument.close()
            file
        } catch (e: IOException) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }
}
