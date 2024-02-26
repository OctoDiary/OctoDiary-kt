package org.bxkr.octodiary.models.persondata

import com.google.gson.annotations.SerializedName

data class PersonData(
    @SerializedName("actual_from")
    val actualFrom: String,
    @SerializedName("actual_to")
    val actualTo: String,
    @SerializedName("addresses")
    val addresses: List<Addresse>,
    @SerializedName("agents")
    val agents: Any?,
    @SerializedName("birthdate")
    val birthdate: String,
    @SerializedName("birthplace")
    val birthplace: String,
    @SerializedName("categories")
    val categories: Any?,
    @SerializedName("children")
    val children: Any?,
    @SerializedName("citizenship")
    val citizenship: Any?,
    @SerializedName("citizenship_id")
    val citizenshipId: Int,
    @SerializedName("contacts")
    val contacts: Any?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("created_by")
    val createdBy: String,
    @SerializedName("documents")
    val documents: List<Document>,
    @SerializedName("education")
    val education: List<Education>,
    @SerializedName("firstname")
    val firstname: String,
    @SerializedName("gender_id")
    val genderId: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("lastname")
    val lastname: String,
    @SerializedName("merged_to")
    val mergedTo: Any?,
    @SerializedName("patronymic")
    val patronymic: String,
    @SerializedName("person_id")
    val personId: String,
    @SerializedName("preventions")
    val preventions: Any?,
    @SerializedName("snils")
    val snils: String,
    @SerializedName("updated_at")
    val updatedAt: Any?,
    @SerializedName("updated_by")
    val updatedBy: Any?,
    @SerializedName("validated_at")
    val validatedAt: String,
    @SerializedName("validation_errors")
    val validationErrors: Any?,
    @SerializedName("validation_state_id")
    val validationStateId: Int
)