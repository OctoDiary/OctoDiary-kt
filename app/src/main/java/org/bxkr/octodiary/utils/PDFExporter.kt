package org.bxkr.octodiary.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Экспорт решений AI в PDF
 */
object PDFExporter {
    
    suspend fun exportSolutionToPDF(
        context: Context,
        subject: String,
        task: String,
        solution: String,
        reasoning: String? = null,
        model: String,
        timestamp: Date = Date(),
        outputDir: File? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            // Инициализация PDFBox
            PDFBoxResourceLoader.init(context)
            
            val document = PDDocument()
            
            // Добавляем первую страницу
            val page = PDPage(PDRectangle.A4)
            document.addPage(page)
            
            val contentStream = PDPageContentStream(document, page)
            
            var yPosition = 750f
            val margin = 50f
            val pageWidth = PDRectangle.A4.width - 2 * margin
            
            // Заголовок
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18f)
            contentStream.newLineAtOffset(margin, yPosition)
            contentStream.showText("AI Solution - $subject")
            contentStream.endText()
            
            yPosition -= 40f
            
            // Дата и модель
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA, 10f)
            contentStream.newLineAtOffset(margin, yPosition)
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            contentStream.showText("Date: ${dateFormat.format(timestamp)} | Model: $model")
            contentStream.endText()
            
            yPosition -= 30f
            
            // Задание
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12f)
            contentStream.newLineAtOffset(margin, yPosition)
            contentStream.showText("Task:")
            contentStream.endText()
            
            yPosition -= 20f
            
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA, 10f)
            contentStream.newLineAtOffset(margin, yPosition)
            
            // Разбиваем текст на строки
            val taskLines = wrapText(task, pageWidth, PDType1Font.HELVETICA, 10f)
            taskLines.forEach { line ->
                if (yPosition < 50) {
                    // Новая страница если не хватает места
                    contentStream.endText()
                    contentStream.close()
                    
                    val newPage = PDPage(PDRectangle.A4)
                    document.addPage(newPage)
                    val newStream = PDPageContentStream(document, newPage)
                    
                    yPosition = 750f
                    newStream.beginText()
                    newStream.setFont(PDType1Font.HELVETICA, 10f)
                    newStream.newLineAtOffset(margin, yPosition)
                }
                
                contentStream.showText(line)
                contentStream.newLineAtOffset(0f, -15f)
                yPosition -= 15f
            }
            contentStream.endText()
            
            yPosition -= 20f
            
            // Решение
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12f)
            contentStream.newLineAtOffset(margin, yPosition)
            contentStream.showText("Solution:")
            contentStream.endText()
            
            yPosition -= 20f
            
            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA, 10f)
            contentStream.newLineAtOffset(margin, yPosition)
            
            val solutionLines = wrapText(solution, pageWidth, PDType1Font.HELVETICA, 10f)
            solutionLines.forEach { line ->
                if (yPosition < 50) {
                    contentStream.endText()
                    contentStream.close()
                    
                    val newPage = PDPage(PDRectangle.A4)
                    document.addPage(newPage)
                    contentStream = PDPageContentStream(document, newPage)
                    
                    yPosition = 750f
                    contentStream.beginText()
                    contentStream.setFont(PDType1Font.HELVETICA, 10f)
                    contentStream.newLineAtOffset(margin, yPosition)
                }
                
                contentStream.showText(line)
                contentStream.newLineAtOffset(0f, -15f)
                yPosition -= 15f
            }
            contentStream.endText()
            
            // Reasoning (если есть)
            if (!reasoning.isNullOrBlank()) {
                yPosition -= 20f
                
                contentStream.beginText()
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12f)
                contentStream.newLineAtOffset(margin, yPosition)
                contentStream.showText("Reasoning:")
                contentStream.endText()
                
                yPosition -= 20f
                
                contentStream.beginText()
                contentStream.setFont(PDType1Font.HELVETICA, 10f)
                contentStream.newLineAtOffset(margin, yPosition)
                
                val reasoningLines = wrapText(reasoning, pageWidth, PDType1Font.HELVETICA, 10f)
                reasoningLines.forEach { line ->
                    if (yPosition < 50) {
                        contentStream.endText()
                        contentStream.close()
                        
                        val newPage = PDPage(PDRectangle.A4)
                        document.addPage(newPage)
                        contentStream = PDPageContentStream(document, newPage)
                        
                        yPosition = 750f
                        contentStream.beginText()
                        contentStream.setFont(PDType1Font.HELVETICA, 10f)
                        contentStream.newLineAtOffset(margin, yPosition)
                    }
                    
                    contentStream.showText(line)
                    contentStream.newLineAtOffset(0f, -15f)
                    yPosition -= 15f
                }
                contentStream.endText()
            }
            
            contentStream.close()
            
            // Сохраняем
            val dir = outputDir ?: File(context.getExternalFilesDir(null), "PDFExports")
            dir.mkdirs()
            
            val fileName = "solution_${timestamp.time}.pdf"
            val file = File(dir, fileName)
            
            document.save(file)
            document.close()
            
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun wrapText(text: String, maxWidth: Float, font: PDType1Font, fontSize: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        
        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = font.getStringWidth(testLine) / 1000 * fontSize
            
            if (width > maxWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine)
                currentLine = word
            } else {
                currentLine = testLine
            }
        }
        
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        
        return lines
    }
}
