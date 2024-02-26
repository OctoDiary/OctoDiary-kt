package org.bxkr.octodiary.models.persondata


import com.google.gson.annotations.SerializedName

data class Class(
    @SerializedName("academic_year_id")
    val academicYearId: Int?,
    @SerializedName("actual_from")
    val actualFrom: String,
    @SerializedName("actual_to")
    val actualTo: String,
    @SerializedName("age_group_id")
    val ageGroupId: Any?,
    @SerializedName("building_id")
    val buildingId: Int?,
    @SerializedName("close_at")
    val closeAt: Any?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("created_by")
    val createdBy: String,
    @SerializedName("data")
    val `data`: Any?,
    @SerializedName("education_stage_id")
    val educationStageId: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("letter")
    val letter: String?,
    @SerializedName("name")
    val name: String,
    @SerializedName("notes")
    val notes: Any?,
    @SerializedName("open_at")
    val openAt: String,
    @SerializedName("organization")
    val organization: OrganizationX,
    @SerializedName("organization_id")
    val organizationId: Int,
    @SerializedName("parallel")
    val parallel: Parallel?,
    @SerializedName("parallel_id")
    val parallelId: Int?,
    @SerializedName("staff_ids")
    val staffIds: List<Int>,
    @SerializedName("uid")
    val uid: String,
    @SerializedName("updated_at")
    val updatedAt: Any?,
    @SerializedName("updated_by")
    val updatedBy: Any?
)