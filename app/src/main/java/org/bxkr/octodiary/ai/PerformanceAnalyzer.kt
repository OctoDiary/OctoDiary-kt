package org.bxkr.octodiary.ai


import androidx.compose.material.icons.Icons
import android.content.Context
import org.bxkr.octodiary.ai.providers.AiProvider
import org.bxkr.octodiary.models.marklistsubject.MarkListSubjectItem

interface PerformanceAnalyzer {
    suspend fun analyzePerformance(context: Context, marksData: List<MarkListSubjectItem>, aiProvider: AiProvider): Result<String>
}

class MockPerformanceAnalyzer : PerformanceAnalyzer {
    override suspend fun analyzePerformance(context: Context, marksData: List<MarkListSubjectItem>, aiProvider: AiProvider): Result<String> {
        val subjectsCount = marksData.size
        return Result.success("Mock analysis for $subjectsCount subjects performance using ${aiProvider.name}.")
    }
}


