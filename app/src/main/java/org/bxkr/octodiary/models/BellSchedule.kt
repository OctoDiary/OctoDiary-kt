package org.bxkr.octodiary.models


import androidx.compose.material.icons.Icons
import java.time.LocalTime

data class BellSchedule(
    val lessonNumber: Int,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val breakDuration: Int = 10 // минуты
) {
    val duration: Int
        get() = java.time.Duration.between(startTime, endTime).toMinutes().toInt()
}

object DefaultBellSchedules {
    // Стандартное расписание (45 мин урок)
    val STANDARD = listOf(
        BellSchedule(1, LocalTime.of(8, 30), LocalTime.of(9, 15), 10),
        BellSchedule(2, LocalTime.of(9, 25), LocalTime.of(10, 10), 20), // большая перемена
        BellSchedule(3, LocalTime.of(10, 30), LocalTime.of(11, 15), 10),
        BellSchedule(4, LocalTime.of(11, 25), LocalTime.of(12, 10), 20), // большая перемена
        BellSchedule(5, LocalTime.of(12, 30), LocalTime.of(13, 15), 10),
        BellSchedule(6, LocalTime.of(13, 25), LocalTime.of(14, 10), 10),
        BellSchedule(7, LocalTime.of(14, 20), LocalTime.of(15, 5), 10),
        BellSchedule(8, LocalTime.of(15, 15), LocalTime.of(16, 0))
    )
    
    // Короткие уроки (40 мин урок, 20 мин перемена)
    val SHORT = listOf(
        BellSchedule(1, LocalTime.of(8, 30), LocalTime.of(9, 10), 20),
        BellSchedule(2, LocalTime.of(9, 30), LocalTime.of(10, 10), 20),
        BellSchedule(3, LocalTime.of(10, 30), LocalTime.of(11, 10), 20),
        BellSchedule(4, LocalTime.of(11, 30), LocalTime.of(12, 10), 20),
        BellSchedule(5, LocalTime.of(12, 30), LocalTime.of(13, 10), 20),
        BellSchedule(6, LocalTime.of(13, 30), LocalTime.of(14, 10), 20),
        BellSchedule(7, LocalTime.of(14, 30), LocalTime.of(15, 10), 20),
        BellSchedule(8, LocalTime.of(15, 30), LocalTime.of(16, 10))
    )
    
    // Сменное расписание (вторая смена)
    val SECOND_SHIFT = listOf(
        BellSchedule(1, LocalTime.of(13, 30), LocalTime.of(14, 15), 10),
        BellSchedule(2, LocalTime.of(14, 25), LocalTime.of(15, 10), 20),
        BellSchedule(3, LocalTime.of(15, 30), LocalTime.of(16, 15), 10),
        BellSchedule(4, LocalTime.of(16, 25), LocalTime.of(17, 10), 10),
        BellSchedule(5, LocalTime.of(17, 20), LocalTime.of(18, 5), 10),
        BellSchedule(6, LocalTime.of(18, 15), LocalTime.of(19, 0))
    )
}

data class BellScheduleConfig(
    val name: String,
    val schedule: List<BellSchedule>,
    val daysOfWeek: List<Int> = (1..5).toList(), // 1=Пн, ..., 5=Пт
    val notifyBeforeLesson: Boolean = true,
    val notifyBeforeLessonMinutes: Int = 5,
    val notifyAtLessonEnd: Boolean = true,
    val notifyAtLessonEndMinutes: Int = 1,
    val showCountdown: Boolean = true
)



