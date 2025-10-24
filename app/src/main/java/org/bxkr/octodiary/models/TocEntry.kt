package org.bxkr.octodiary.models
 
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable
 
/**
 * Элемент оглавления (TOC) учебника
 */
@Entity(tableName = "toc_entries")
data class TocEntry(
    @PrimaryKey
    @SerializedName("id") val id: String,
 
    // Связь с учебником
    @SerializedName("textbookId") val textbookId: String,

    // Заголовок элемента оглавления
    @SerializedName("title") val title: String,

    // Краткое описание или контент
    @SerializedName("summary") val summary: String? = null,

    // Номер страницы (если известен)
    @SerializedName("pageNumber") val pageNumber: Int? = null,

    // Уровень вложенности (1 для глав, 2 для разделов и т.д.)
    @SerializedName("level") val level: Int = 1,

    // Важность (1-5, где 5 - наиболее важный)
    @SerializedName("importance") val importance: Int = 3,

    // Ключевые слова/теги
    @SerializedName("keywords") val keywords: List<String> = emptyList(),

    // Порядок в оглавлении
    @SerializedName("orderIndex") val orderIndex: Int = 0,

    // Время создания
    @SerializedName("createdAt") val createdAt: Long = System.currentTimeMillis()
) : Serializable {

    /**
     * Получить отформатированный заголовок с отступами
     */
    fun getFormattedTitle(): String {
        return when (level) {
            1 -> title
            2 -> "  $title"
            3 -> "    $title"
            else -> "      $title"
        }
    }
}