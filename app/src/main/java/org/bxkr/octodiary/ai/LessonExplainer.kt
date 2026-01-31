package org.bxkr.octodiary.ai


import androidx.compose.material.icons.Icons
import android.content.Context
import org.bxkr.octodiary.ai.providers.AiProvider

interface LessonExplainer {
    suspend fun explainTopic(context: Context, topic: String, subject: String, aiProvider: AiProvider): Result<String>
}

class MockLessonExplainer : LessonExplainer {
    override suspend fun explainTopic(context: Context, topic: String, subject: String, aiProvider: AiProvider): Result<String> {
        return Result.success("Mock explanation for topic '$topic' in subject '$subject' using ${aiProvider.name}.")
    }
}



