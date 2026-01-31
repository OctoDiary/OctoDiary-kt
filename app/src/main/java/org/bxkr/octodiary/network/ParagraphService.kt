package org.bxkr.octodiary.network


import androidx.compose.material.icons.Icons
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import org.bxkr.octodiary.models.ParagraphRequest
import org.bxkr.octodiary.models.ParagraphResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object ParagraphService {

    private const val TAG = "ParagraphService"

    /**
     * Запрашивает полный параграф по заданной теме
     * @param request Параметры запроса
     * @return ParagraphResponse с результатом
     */
    suspend fun requestParagraph(request: ParagraphRequest): ParagraphResponse {
        return suspendCancellableCoroutine { continuation ->
            try {
                val call = NetworkService.externalApi().requestParagraph(
                    topic = request.topic,
                    language = request.language,
                    maxLength = request.maxLength
                )

                call.enqueue(object : Callback<ParagraphResponse> {
                    override fun onResponse(
                        call: Call<ParagraphResponse>,
                        response: Response<ParagraphResponse>
                    ) {
                        if (response.isSuccessful) {
                            val paragraphResponse = response.body()
                            if (paragraphResponse != null) {
                                Log.d(TAG, "Paragraph request successful: ${paragraphResponse.requestId}")
                                continuation.resume(paragraphResponse)
                            } else {
                                val errorResponse = ParagraphResponse(
                                    requestId = "",
                                    title = "",
                                    content = "",
                                    source = "",
                                    language = request.language,
                                    wordCount = 0,
                                    estimatedReadTime = 0,
                                    success = false,
                                    errorMessage = "Empty response from server"
                                )
                                Log.e(TAG, "Empty response from server")
                                continuation.resume(errorResponse)
                            }
                        } else {
                            val errorResponse = ParagraphResponse(
                                requestId = "",
                                title = "",
                                content = "",
                                source = "",
                                language = request.language,
                                wordCount = 0,
                                estimatedReadTime = 0,
                                success = false,
                                errorMessage = "HTTP ${response.code()}: ${response.message()}"
                            )
                            Log.e(TAG, "HTTP error: ${response.code()} - ${response.message()}")
                            continuation.resume(errorResponse)
                        }
                    }

                    override fun onFailure(call: Call<ParagraphResponse>, t: Throwable) {
                        val errorResponse = ParagraphResponse(
                            requestId = "",
                            title = "",
                            content = "",
                            source = "",
                            language = request.language,
                            wordCount = 0,
                            estimatedReadTime = 0,
                            success = false,
                            errorMessage = t.localizedMessage ?: "Network error"
                        )
                        Log.e(TAG, "Network request failed", t)
                        continuation.resume(errorResponse)
                    }
                })

                continuation.invokeOnCancellation {
                    call.cancel()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error making paragraph request", e)
                val errorResponse = ParagraphResponse(
                    requestId = "",
                    title = "",
                    content = "",
                    source = "",
                    language = request.language,
                    wordCount = 0,
                    estimatedReadTime = 0,
                    success = false,
                    errorMessage = e.localizedMessage ?: "Unknown error"
                )
                continuation.resume(errorResponse)
            }
        }
    }
}


