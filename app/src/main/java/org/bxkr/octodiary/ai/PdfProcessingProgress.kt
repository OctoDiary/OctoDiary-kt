package org.bxkr.octodiary.ai

/**
 * Класс для отслеживания прогресса обработки PDF
 */
data class PdfProcessingProgress(
    val progress: Float, // Прогресс от 0.0 до 1.0
    val currentPage: Int, // Текущая страница
    val totalPages: Int, // Общее количество страниц
    val message: String? = null, // Сообщение о текущем состоянии
    val canResume: Boolean = true, // Можно ли возобновить
    val isPaused: Boolean = false, // Приостановлено ли
    val currentPageTitle: String? = null, // Название текущей страницы
    val status: ProcessingStatus = ProcessingStatus.PROCESSING // Статус обработки
)

enum class ProcessingStatus {
    PROCESSING,
    PAUSED,
    ERROR,
    COMPLETED
}