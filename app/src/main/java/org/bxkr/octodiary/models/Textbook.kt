package org.bxkr.octodiary.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Модель учебника (PDF)
 */
data class Textbook(
    @SerializedName("id") val id: String,
    @SerializedName("subjectName") val subjectName: String,
    @SerializedName("title") val title: String,
    @SerializedName("author") val author: String? = null,
    @SerializedName("fileName") val fileName: String,
    @SerializedName("filePath") val filePath: String,
    @SerializedName("fileSize") val fileSize: Long,
    @SerializedName("addedDate") val addedDate: Long = System.currentTimeMillis(),
    @SerializedName("pageCount") val pageCount: Int? = null
) : Serializable
