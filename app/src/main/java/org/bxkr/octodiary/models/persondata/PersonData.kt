package org.bxkr.octodiary.models.persondata

import com.google.gson.annotations.SerializedName

data class PersonData(
    @SerializedName("documents")
    val documents: List<Document> = emptyList(),
)