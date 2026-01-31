package org.bxkr.octodiary.ai


import androidx.compose.material.icons.Icons
import android.content.Context
import android.graphics.Bitmap
import org.bxkr.octodiary.ai.providers.AiProvider
import org.bxkr.octodiary.models.marklistsubject.MarkListSubjectItem

class AiService(
    private val context: Context,
    private val aiSettingsViewModel: AiSettingsViewModel, // Assuming this is instantiated elsewhere
    private val lessonExplainer: LessonExplainer,
    private val homeworkHelper: HomeworkHelper,
    private val performanceAnalyzer: PerformanceAnalyzer
) {

    private val activeProvider: AiProvider
        get() = aiSettingsViewModel.getActiveProvider()

    suspend fun explainLessonTopic(topic: String, subject: String): Result<String> {
        return if (activeProvider.name == "Disabled") {
            Result.failure(IllegalStateException("AI is disabled in settings."))
        } else {
            lessonExplainer.explainTopic(context, topic, subject, activeProvider)
        }
    }

    suspend fun getHomeworkSolution(homeworkText: String, image: Bitmap? = null): Result<String> {
        return if (activeProvider.name == "Disabled") {
            Result.failure(IllegalStateException("AI is disabled in settings."))
        } else {
            homeworkHelper.solveHomework(context, homeworkText, activeProvider, image)
        }
    }

    suspend fun analyzeStudentPerformance(marksData: List<MarkListSubjectItem>): Result<String> {
        return if (activeProvider.name == "Disabled") {
            Result.failure(IllegalStateException("AI is disabled in settings."))
        } else {
            performanceAnalyzer.analyzePerformance(context, marksData, activeProvider)
        }
    }
}


