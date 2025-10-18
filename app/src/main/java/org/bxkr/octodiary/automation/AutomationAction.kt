package org.bxkr.octodiary.automation

/**
 * Действия для автоматизации WebView (аналог Droidrun)
 */
sealed class AutomationAction {
    /**
     * Клик по координатам (x, y) или по элементу с текстом
     */
    data class Click(
        val x: Int? = null,
        val y: Int? = null,
        val text: String? = null,
        val selector: String? = null
    ) : AutomationAction()

    /**
     * Свайп от (x1, y1) к (x2, y2)
     */
    data class Swipe(
        val fromX: Int,
        val fromY: Int,
        val toX: Int,
        val toY: Int,
        val durationMs: Long = 300
    ) : AutomationAction()

    /**
     * Скролл (вертикальный или горизонтальный)
     */
    data class Scroll(
        val direction: Direction,
        val amount: Int = 300
    ) : AutomationAction() {
        enum class Direction { UP, DOWN, LEFT, RIGHT }
    }

    /**
     * Drag & Drop
     */
    data class Drag(
        val fromX: Int,
        val fromY: Int,
        val toX: Int,
        val toY: Int
    ) : AutomationAction()

    /**
     * Ввод текста в поле
     */
    data class Type(
        val text: String,
        val selector: String? = null
    ) : AutomationAction()

    /**
     * Пауза перед следующим действием
     */
    data class Wait(val milliseconds: Long) : AutomationAction()

    /**
     * Завершение автоматизации
     */
    data class Finish(val message: String = "Задание выполнено") : AutomationAction()
}
