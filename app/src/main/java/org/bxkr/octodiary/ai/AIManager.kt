package org.bxkr.octodiary.ai


import androidx.compose.material.icons.Icons
import android.content.Context
import android.graphics.Bitmap
import org.bxkr.octodiary.models.marklistsubject.MarkListSubjectItem

object AIManager {

    private lateinit var aiService: AiService
    private lateinit var aiSettingsViewModel: AiSettingsViewModel

    fun initialize(context: Context) {
        // Initialize ViewModel and Services here.
        // In a real Hilt scenario, these would be injected.
        aiSettingsViewModel = AiSettingsViewModel(context) // Manual instantiation for now
        val mockLessonExplainer = MockLessonExplainer()
        val mockHomeworkHelper = MockHomeworkHelper()
        val mockPerformanceAnalyzer = MockPerformanceAnalyzer()

        aiService = AiService(
            context,
            aiSettingsViewModel,
            mockLessonExplainer,
            mockHomeworkHelper,
            mockPerformanceAnalyzer
        )
    }

    fun getAiSettingsViewModel(): AiSettingsViewModel {
        check(this::aiSettingsViewModel.isInitialized) { "AIManager not initialized. Call initialize() first." }
        return aiSettingsViewModel
    }

    suspend fun explainLessonTopic(topic: String, subject: String): Result<String> {
        check(this::aiService.isInitialized) { "AIManager not initialized. Call initialize() first." }
        return aiService.explainLessonTopic(topic, subject)
    }

    suspend fun getHomeworkSolution(homeworkText: String, image: Bitmap? = null): Result<String> {
        check(this::aiService.isInitialized) { "AIManager not initialized. Call initialize() first." }
        return aiService.getHomeworkSolution(homeworkText, image)
    }

    suspend fun analyzeStudentPerformance(marksData: List<MarkListSubjectItem>): Result<String> {
        check(this::aiService.isInitialized) { "AIManager not initialized. Call initialize() first." }
        return aiService.analyzeStudentPerformance(marksData)
    }

    fun getActiveAiProviderName(): String {
        check(this::aiSettingsViewModel.isInitialized) { "AIManager not initialized. Call initialize() first." }
        return aiSettingsViewModel.getActiveProvider().name
    }
}



