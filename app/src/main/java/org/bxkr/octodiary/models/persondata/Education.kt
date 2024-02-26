package org.bxkr.octodiary.models.persondata


import com.google.gson.annotations.SerializedName

data class Education(
    @SerializedName("actual_from")
    val actualFrom: String,
    @SerializedName("actual_to")
    val actualTo: String,
    @SerializedName("class_uid")
    val classUid: String,
    @SerializedName("class")
    val classX: Class,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("created_by")
    val createdBy: String,
    @SerializedName("deduction_reason")
    val deductionReason: Any?,
    @SerializedName("deduction_reason_id")
    val deductionReasonId: Any?,
    @SerializedName("education_form")
    val educationForm: EducationForm,
    @SerializedName("education_form_id")
    val educationFormId: Int,
    @SerializedName("financing_type")
    val financingType: FinancingType,
    @SerializedName("financing_type_id")
    val financingTypeId: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("notes")
    val notes: Any?,
    @SerializedName("organization")
    val organization: OrganizationX,
    @SerializedName("organization_id")
    val organizationId: Int,
    @SerializedName("person_id")
    val personId: String,
    @SerializedName("service_type")
    val serviceType: ServiceType,
    @SerializedName("service_type_id")
    val serviceTypeId: Int,
    @SerializedName("training_begin_at")
    val trainingBeginAt: String,
    @SerializedName("training_end_at")
    val trainingEndAt: String,
    @SerializedName("updated_at")
    val updatedAt: Any?,
    @SerializedName("updated_by")
    val updatedBy: Any?
)