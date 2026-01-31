package org.bxkr.octodiary.models.persondata


import androidx.compose.material.icons.Icons
import com.google.gson.annotations.SerializedName

data class PersonData(
    @SerializedName("documents")
    val documents: List<Document> = emptyList(),
)


