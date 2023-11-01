package org.bxkr.octodiary.models.auth


import com.google.gson.annotations.SerializedName

data class RegionalProfile(
    @SerializedName("agree_pers_data")
    val agreePersData: Boolean,
    @SerializedName("id")
    val id: Long,
    @SerializedName("organization_id")
    val organizationId: String,
    @SerializedName("original_user_id")
    val originalUserId: Long,
    @SerializedName("person_id")
    val personId: String,
    @SerializedName("roles")
    val roles: List<Any>,
    @SerializedName("school_id")
    val schoolId: Long,
    @SerializedName("school_shortname")
    val schoolShortname: String,
    @SerializedName("subject_ids")
    val subjectIds: List<Any>,
    @SerializedName("type")
    val type: String,
    @SerializedName("user_id")
    val userId: Long
)