package org.bxkr.octodiary.ai


import androidx.compose.material.icons.Icons
import android.content.Context
import android.graphics.Bitmap
import org.bxkr.octodiary.ai.providers.AiProvider

interface HomeworkHelper {
    suspend fun solveHomework(context: Context, homeworkText: String, aiProvider: AiProvider, image: Bitmap? = null): Result<String>
}

class MockHomeworkHelper : HomeworkHelper {
    override suspend fun solveHomework(context: Context, homeworkText: String, aiProvider: AiProvider, image: Bitmap?): Result<String> {
        val imageStatus = if (image != null) " with image" else ""
        return Result.success("Mock solution for homework: '$homeworkText'$imageStatus using ${aiProvider.name}.")
    }
}



