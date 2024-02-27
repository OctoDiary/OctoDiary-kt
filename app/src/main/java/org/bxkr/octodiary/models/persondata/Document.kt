package org.bxkr.octodiary.models.persondata


import com.google.gson.annotations.SerializedName
import org.bxkr.octodiary.R

data class Document(
    @SerializedName("actual_from")
    val actualFrom: String,
    @SerializedName("actual_to")
    val actualTo: String,
    @SerializedName("attachments")
    val attachments: List<Attachment>,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("created_by")
    val createdBy: String,
    @SerializedName("document_type")
    val documentType: DocumentType,
    @SerializedName("document_type_id")
    val documentTypeId: Int,
    @SerializedName("expiration")
    val expiration: Any?,
    @SerializedName("id")
    val id: Int,
    @SerializedName("issued")
    val issued: String?,
    @SerializedName("issuer")
    val issuer: String?,
    @SerializedName("number")
    val number: String?,
    @SerializedName("person_id")
    val personId: String,
    @SerializedName("series")
    val series: String?,
    @SerializedName("subdivision_code")
    val subdivisionCode: String?,
    @SerializedName("updated_at")
    val updatedAt: Any?,
    @SerializedName("updated_by")
    val updatedBy: Any?,
    @SerializedName("validated_at")
    val validatedAt: String?,
    @SerializedName("validation_errors")
    val validationErrors: Any?,
    @SerializedName("validation_state_id")
    val validationStateId: Int,
) {
    val displayData
        get() = mapOf(
            R.string.series to series,
            R.string.number to number,
            R.string.issued to issued,
            R.string.issuer to issuer,
            R.string.subdivision_code to subdivisionCode
        )
}