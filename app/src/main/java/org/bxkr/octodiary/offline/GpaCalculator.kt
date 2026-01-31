package org.bxkr.octodiary.offline


import androidx.compose.material.icons.Icons
/**
 * Специализированный калькулятор GPA с поддержкой различных шкал оценок.
 * Поддерживает международные шкалы и конвертацию между ними.
 */
object GpaCalculator {

    enum class GpaScale(val displayName: String, val maxValue: Double) {
        STANDARD_4_0("Стандартная 4.0", 4.0),
        STANDARD_5_0("Стандартная 5.0", 5.0),
        WEIGHTED_4_0("Взвешенная 4.0", 4.0),
        WEIGHTED_5_0("Взвешенная 5.0", 5.0),
        BRITISH("Британская шкала", 4.0),
        GERMAN("Немецкая шкала", 4.0),
        FRENCH("Французская шкала", 20.0),
        PERCENTAGE("Процентная шкала", 100.0)
    }

    data class GpaResult(
        val gpa: Double,
        val scale: GpaScale,
        val letterGrade: String? = null,
        val description: String? = null,
        val credits: Int = 1
    )

    data class GradeConversion(
        val numericGrade: Int,
        val gpaValue: Double,
        val letterGrade: String,
        val description: String
    )

    /**
     * Вычисляет GPA по шкале 4.0 (стандартная американская).
     */
    fun calculateStandardGPA(grades: List<Int>, credits: List<Int> = List(grades.size) { 1 }): GpaResult {
        require(grades.size == credits.size) { "Количество оценок должно совпадать с количеством кредитов" }

        val conversions = grades.map { convertRussianToStandard4(it) }
        val weightedSum = conversions.zip(credits).sumOf { (conversion, credit) -> conversion.gpaValue * credit }
        val totalCredits = credits.sum()

        val gpa = if (totalCredits > 0) weightedSum / totalCredits else 0.0

        return GpaResult(
            gpa = gpa,
            scale = GpaScale.STANDARD_4_0,
            letterGrade = getLetterGrade(gpa),
            description = getGpaDescription(gpa),
            credits = totalCredits
        )
    }

    /**
     * Вычисляет GPA по шкале 5.0.
     */
    fun calculate5PointGPA(grades: List<Int>, credits: List<Int> = List(grades.size) { 1 }): GpaResult {
        require(grades.size == credits.size) { "Количество оценок должно совпадать с количеством кредитов" }

        val conversions = grades.map { convertRussianTo5Point(it) }
        val weightedSum = conversions.zip(credits).sumOf { (conversion, credit) -> conversion.gpaValue * credit }
        val totalCredits = credits.sum()

        val gpa = if (totalCredits > 0) weightedSum / totalCredits else 0.0

        return GpaResult(
            gpa = gpa,
            scale = GpaScale.STANDARD_5_0,
            letterGrade = getLetterGrade5Point(gpa),
            description = getGpaDescription5Point(gpa),
            credits = totalCredits
        )
    }

    /**
     * Вычисляет взвешенный GPA (учитывает сложность предметов).
     */
    fun calculateWeightedGPA(
        grades: List<Int>,
        credits: List<Int> = List(grades.size) { 1 },
        weights: List<Double> = List(grades.size) { 1.0 }
    ): GpaResult {
        require(grades.size == credits.size && grades.size == weights.size) {
            "Количество оценок, кредитов и весов должно совпадать"
        }

        val conversions = grades.map { convertRussianToStandard4(it) }
        val weightedSum = conversions.zip(credits).zip(weights).sumOf { (conversionCredit, weight) ->
            val (conversion, credit) = conversionCredit
            conversion.gpaValue * credit * weight
        }
        val totalWeightedCredits = credits.zip(weights).sumOf { (credit, weight) -> credit * weight }

        val gpa = if (totalWeightedCredits > 0) weightedSum / totalWeightedCredits else 0.0

        return GpaResult(
            gpa = gpa,
            scale = GpaScale.WEIGHTED_4_0,
            letterGrade = getLetterGrade(gpa),
            description = getWeightedGpaDescription(gpa),
            credits = credits.sum()
        )
    }

    /**
     * Конвертирует русскую пятибалльную шкалу в GPA 4.0.
     */
    fun convertRussianToStandard4(grade: Int): GradeConversion {
        return when (grade) {
            5 -> GradeConversion(5, 4.0, "A", "Отлично")
            4 -> GradeConversion(4, 3.0, "B", "Хорошо")
            3 -> GradeConversion(3, 2.0, "C", "Удовлетворительно")
            2 -> GradeConversion(2, 1.0, "D", "Неудовлетворительно")
            else -> GradeConversion(grade, 0.0, "F", "Неизвестная оценка")
        }
    }

    /**
     * Конвертирует русскую шкалу в GPA 5.0.
     */
    fun convertRussianTo5Point(grade: Int): GradeConversion {
        return when (grade) {
            5 -> GradeConversion(5, 5.0, "A+", "Отлично")
            4 -> GradeConversion(4, 4.0, "B+", "Хорошо")
            3 -> GradeConversion(3, 3.0, "C+", "Удовлетворительно")
            2 -> GradeConversion(2, 2.0, "D+", "Неудовлетворительно")
            else -> GradeConversion(grade, 0.0, "F", "Неизвестная оценка")
        }
    }

    /**
     * Конвертирует процентную шкалу в GPA 4.0.
     */
    fun convertPercentageToGPA(percentage: Double): GradeConversion {
        val gpa = when {
            percentage >= 95 -> 4.0
            percentage >= 90 -> 3.7
            percentage >= 85 -> 3.3
            percentage >= 80 -> 3.0
            percentage >= 75 -> 2.7
            percentage >= 70 -> 2.3
            percentage >= 65 -> 2.0
            percentage >= 60 -> 1.7
            percentage >= 55 -> 1.3
            percentage >= 50 -> 1.0
            else -> 0.0
        }

        val letterGrade = getLetterGrade(gpa)
        val description = getGpaDescription(gpa)

        return GradeConversion(percentage.toInt(), gpa, letterGrade, description)
    }

    /**
     * Конвертирует британскую шкалу в GPA 4.0.
     */
    fun convertBritishToGPA(britishGrade: String): GradeConversion {
        val (gpa, description) = when (britishGrade.uppercase()) {
            "A*" -> 4.0 to "Выдающийся"
            "A" -> 4.0 to "Отлично"
            "B" -> 3.7 to "Очень хорошо"
            "C" -> 3.0 to "Хорошо"
            "D" -> 2.3 to "Удовлетворительно"
            "E" -> 2.0 to "Приемлемо"
            "F" -> 1.7 to "Неудовлетворительно"
            "G" -> 1.0 to "Плохо"
            "U" -> 0.0 to "Не сдан"
            else -> 0.0 to "Неизвестная оценка"
        }

        return GradeConversion(0, gpa, britishGrade, description)
    }

    /**
     * Конвертирует немецкую шкалу (1.0-4.0) в GPA 4.0.
     */
    fun convertGermanToGPA(germanGrade: Double): GradeConversion {
        val gpa = when {
            germanGrade <= 1.5 -> 4.0
            germanGrade <= 2.5 -> 3.0
            germanGrade <= 3.5 -> 2.0
            germanGrade <= 4.0 -> 1.0
            else -> 0.0
        }

        val letterGrade = getLetterGrade(gpa)
        val description = getGermanDescription(germanGrade)

        return GradeConversion(germanGrade.toInt(), gpa, letterGrade, description)
    }

    /**
     * Конвертирует французскую шкалу (0-20) в GPA 4.0.
     */
    fun convertFrenchToGPA(frenchGrade: Double): GradeConversion {
        val percentage = (frenchGrade / 20.0) * 100.0
        return convertPercentageToGPA(percentage).copy(
            numericGrade = frenchGrade.toInt(),
            description = getFrenchDescription(frenchGrade)
        )
    }

    /**
     * Получает буквенную оценку для GPA 4.0.
     */
    private fun getLetterGrade(gpa: Double): String {
        return when {
            gpa >= 3.7 -> "A"
            gpa >= 3.3 -> "A-"
            gpa >= 3.0 -> "B+"
            gpa >= 2.7 -> "B"
            gpa >= 2.3 -> "B-"
            gpa >= 2.0 -> "C+"
            gpa >= 1.7 -> "C"
            gpa >= 1.3 -> "C-"
            gpa >= 1.0 -> "D"
            else -> "F"
        }
    }

    /**
     * Получает буквенную оценку для GPA 5.0.
     */
    private fun getLetterGrade5Point(gpa: Double): String {
        return when {
            gpa >= 4.5 -> "A+"
            gpa >= 4.0 -> "A"
            gpa >= 3.5 -> "B+"
            gpa >= 3.0 -> "B"
            gpa >= 2.5 -> "C+"
            gpa >= 2.0 -> "C"
            gpa >= 1.5 -> "D+"
            gpa >= 1.0 -> "D"
            else -> "F"
        }
    }

    /**
     * Получает описание GPA 4.0.
     */
    private fun getGpaDescription(gpa: Double): String {
        return when {
            gpa >= 3.7 -> "Отличные результаты"
            gpa >= 3.0 -> "Хорошие результаты"
            gpa >= 2.0 -> "Удовлетворительные результаты"
            gpa >= 1.0 -> "Неудовлетворительные результаты"
            else -> "Критические результаты"
        }
    }

    /**
     * Получает описание GPA 5.0.
     */
    private fun getGpaDescription5Point(gpa: Double): String {
        return when {
            gpa >= 4.0 -> "Выдающиеся результаты"
            gpa >= 3.0 -> "Хорошие результаты"
            gpa >= 2.0 -> "Удовлетворительные результаты"
            gpa >= 1.0 -> "Неудовлетворительные результаты"
            else -> "Критические результаты"
        }
    }

    /**
     * Получает описание взвешенного GPA.
     */
    private fun getWeightedGpaDescription(gpa: Double): String {
        return when {
            gpa >= 4.0 -> "Отличные результаты с учётом сложности"
            gpa >= 3.0 -> "Хорошие результаты с учётом сложности"
            gpa >= 2.0 -> "Удовлетворительные результаты"
            else -> "Требуется улучшение"
        }
    }

    /**
     * Получает описание немецкой оценки.
     */
    private fun getGermanDescription(grade: Double): String {
        return when {
            grade <= 1.5 -> "Sehr gut (Очень хорошо)"
            grade <= 2.5 -> "Gut (Хорошо)"
            grade <= 3.5 -> "Befriedigend (Удовлетворительно)"
            grade <= 4.0 -> "Ausreichend (Достаточно)"
            else -> "Ungenügend (Недостаточно)"
        }
    }

    /**
     * Получает описание французской оценки.
     */
    private fun getFrenchDescription(grade: Double): String {
        return when {
            grade >= 16 -> "Très bien (Очень хорошо)"
            grade >= 14 -> "Bien (Хорошо)"
            grade >= 12 -> "Assez bien (Довольно хорошо)"
            grade >= 10 -> "Passable (Удовлетворительно)"
            else -> "Insuffisant (Недостаточно)"
        }
    }

    /**
     * Рассчитывает требуемые оценки для достижения целевого GPA.
     */
    fun calculateRequiredGrades(
        currentGrades: List<Int>,
        currentCredits: List<Int>,
        targetGPA: Double,
        remainingCredits: Int,
        scale: GpaScale = GpaScale.STANDARD_4_0
    ): RequiredGradeResult {
        require(currentGrades.size == currentCredits.size) {
            "Количество текущих оценок должно совпадать с количеством кредитов"
        }

        val currentGpaResult = when (scale) {
            GpaScale.STANDARD_4_0 -> calculateStandardGPA(currentGrades, currentCredits)
            GpaScale.STANDARD_5_0 -> calculate5PointGPA(currentGrades, currentCredits)
            else -> calculateStandardGPA(currentGrades, currentCredits)
        }

        val currentWeightedSum = currentGpaResult.gpa * currentGpaResult.credits
        val totalCredits = currentGpaResult.credits + remainingCredits
        val requiredWeightedSum = targetGPA * totalCredits
        val remainingWeightedSum = requiredWeightedSum - currentWeightedSum
        val requiredAverageGPA = if (remainingCredits > 0) remainingWeightedSum / remainingCredits else 0.0

        val requiredGradeRange = when (scale) {
            GpaScale.STANDARD_4_0 -> getRequiredGradeRange4Point(requiredAverageGPA)
            GpaScale.STANDARD_5_0 -> getRequiredGradeRange5Point(requiredAverageGPA)
            else -> getRequiredGradeRange4Point(requiredAverageGPA)
        }

        return RequiredGradeResult(
            currentGPA = currentGpaResult.gpa,
            targetGPA = targetGPA,
            requiredAverageGPA = requiredAverageGPA,
            requiredGradeRange = requiredGradeRange,
            remainingCredits = remainingCredits,
            scale = scale
        )
    }

    data class RequiredGradeResult(
        val currentGPA: Double,
        val targetGPA: Double,
        val requiredAverageGPA: Double,
        val requiredGradeRange: String,
        val remainingCredits: Int,
        val scale: GpaScale
    )

    /**
     * Получает диапазон требуемых оценок для шкалы 4.0.
     */
    private fun getRequiredGradeRange4Point(requiredGPA: Double): String {
        return when {
            requiredGPA >= 3.7 -> "Требуются оценки 5 (A)"
            requiredGPA >= 3.0 -> "Требуются оценки 4-5 (B+ to A)"
            requiredGPA >= 2.0 -> "Требуются оценки 3-5 (C+ to A)"
            requiredGPA >= 1.0 -> "Требуются оценки 2-5 (D to A)"
            else -> "Цель недостижима с текущими оценками"
        }
    }

    /**
     * Получает диапазон требуемых оценок для шкалы 5.0.
     */
    private fun getRequiredGradeRange5Point(requiredGPA: Double): String {
        return when {
            requiredGPA >= 4.0 -> "Требуются оценки 5 (A+)"
            requiredGPA >= 3.0 -> "Требуются оценки 4-5 (B+ to A+)"
            requiredGPA >= 2.0 -> "Требуются оценки 3-5 (C+ to A+)"
            else -> "Цель недостижима с текущими оценками"
        }
    }
}


