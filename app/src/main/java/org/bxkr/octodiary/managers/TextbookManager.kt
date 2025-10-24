package org.bxkr.octodiary.managers

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bxkr.octodiary.models.Textbook
import org.bxkr.octodiary.ai.PdfProcessingProgress
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Менеджер для управления учебниками (PDF)
 */
object TextbookManager {
    
    private const val PREFS_NAME = "textbooks_prefs"
    private const val KEY_TEXTBOOKS = "textbooks_list"
    private const val TEXTBOOKS_DIR = "textbooks"
    
    private val gson = Gson()
    
    /**
     * Получить все учебники
     */
    fun getAllTextbooks(context: Context): List<Textbook> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_TEXTBOOKS, null) ?: return emptyList()
        
        return try {
            val type = object : TypeToken<List<Textbook>>() {}.type
            gson.fromJson<List<Textbook>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e("TextbookManager", "Ошибка загрузки учебников", e)
            emptyList()
        }
    }
    
    /**
     * Получить учебники по предмету
     */
    fun getTextbooksBySubject(context: Context, subjectName: String): List<Textbook> {
        return getAllTextbooks(context).filter { 
            it.subjectName.equals(subjectName, ignoreCase = true) 
        }
    }
    
    /**
     * Добавить учебник из URI
     */
    suspend fun addTextbook(
        context: Context,
        uri: Uri,
        subjectName: String,
        title: String,
        author: String? = null
    ): Result<Textbook> {
        return try {
            // Создаём директорию для учебников
            val textbooksDir = File(context.filesDir, TEXTBOOKS_DIR)
            if (!textbooksDir.exists()) {
                textbooksDir.mkdirs()
            }
            
            // Копируем файл
            val fileName = "${UUID.randomUUID()}.pdf"
            val destFile = File(textbooksDir, fileName)
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            val fileSize = destFile.length()
            
            // Создаём объект учебника
            val textbook = Textbook(
                id = UUID.randomUUID().toString(),
                subjectName = subjectName,
                title = title,
                author = author,
                fileName = fileName,
                filePath = destFile.absolutePath,
                fileSize = fileSize
            )
            
            // Сохраняем в список
            val textbooks = getAllTextbooks(context).toMutableList()
            textbooks.add(textbook)
            saveTextbooks(context, textbooks)
            
            Log.d("TextbookManager", "Учебник добавлен: $title")
            Result.success(textbook)
            
        } catch (e: Exception) {
            Log.e("TextbookManager", "Ошибка добавления учебника", e)
            Result.failure(e)
        }
    }
    
    /**
     * Удалить учебник
     */
    fun deleteTextbook(context: Context, textbookId: String): Boolean {
        val textbooks = getAllTextbooks(context).toMutableList()
        val textbook = textbooks.find { it.id == textbookId } ?: return false
        
        // Удаляем файл
        val file = File(textbook.filePath)
        if (file.exists()) {
            file.delete()
        }
        
        // Удаляем из списка
        textbooks.removeAll { it.id == textbookId }
        saveTextbooks(context, textbooks)
        
        Log.d("TextbookManager", "Учебник удалён: ${textbook.title}")
        return true
    }
    
    /**
     * Получить файл учебника
     */
    fun getTextbookFile(textbook: Textbook): File {
        return File(textbook.filePath)
    }
    
    /**
     * Сохранить список учебников
     */
    private fun saveTextbooks(context: Context, textbooks: List<Textbook>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(textbooks)
        prefs.edit().putString(KEY_TEXTBOOKS, json).apply()
    }
    
    /**
     * Получить текстовое представление учебников для AI
     */
    fun getTextbooksContextForAI(context: Context, subjectName: String? = null): String {
        val textbooks = if (subjectName != null) {
            getTextbooksBySubject(context, subjectName)
        } else {
            getAllTextbooks(context)
        }

        if (textbooks.isEmpty()) {
            return "Учебники не загружены."
        }

        return buildString {
            appendLine("**Доступные учебники:**")
            textbooks.forEach { textbook ->
                appendLine("- ${textbook.title} (${textbook.subjectName})")
                if (textbook.author != null) {
                    appendLine("  Автор: ${textbook.author}")
                }
                // Добавляем информацию об оглавлении
                // kotlinx.coroutines.runBlocking { // Placeholder
                //     if (TocManager.hasTocForTextbook(context, textbook.id)) {
                //         appendLine("  Оглавление: доступно")
                //     }
                // }
            }
        }
    }

    /**
     * Генерировать оглавление для учебника
     */
    suspend fun generateTocForTextbook(
        context: Context,
        textbook: Textbook,
        onProgress: ((PdfProcessingProgress) -> Unit)? = null
    ): Result<Unit> {
        return TocService.generateTocForTextbook(
            context = context,
            textbookId = textbook.id,
            pdfUri = Uri.fromFile(File(textbook.filePath)),
            onProgress = onProgress
        )
    }

    /**
     * Проверить, существует ли оглавление для учебника
     */
    suspend fun hasTocForTextbook(
        context: Context,
        textbookId: String
    ): Boolean {
        return TocService.hasTocForTextbook(context, textbookId)
    }

    /**
     * Получить оглавление учебника для AI
     */
    suspend fun getTocContextForAI(context: Context, textbook: Textbook): String {
        val baseContext = "Оглавление недоступно." // Placeholder

        return buildString {
            append(baseContext)

            try {
                // val tocEntries = TocManager.getTocForTextbook(context, textbook.id) // Placeholder
                // if (tocEntries.isNotEmpty()) {
                //     appendLine("\n**Элементы оглавления:**")
                //     tocEntries.take(10).forEach { entry -> // Ограничиваем для экономии токенов
                //         appendLine("- ${entry.title} (стр. ${entry.pageNumber ?: "?"})")
                //         if (entry.keywords.isNotEmpty()) {
                //             appendLine("  Ключевые слова: ${entry.keywords.joinToString(", ")}")
                //         }
                //     }
                //     if (tocEntries.size > 10) {
                //         appendLine("- ... и ещё ${tocEntries.size - 10} элементов")
                //     }
                // }
            } catch (e: Exception) {
                appendLine("\nОшибка получения оглавления: ${e.message}")
            }
        }
    }
}
