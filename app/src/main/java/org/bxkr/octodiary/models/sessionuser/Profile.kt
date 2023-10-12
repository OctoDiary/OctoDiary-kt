package org.bxkr.octodiary.models.sessionuser


import com.google.gson.annotations.SerializedName

data class Profile(
    @SerializedName("agree_pers_data")
    val agreePersData: Boolean,
    @SerializedName("id")
    val id: Int,
    @SerializedName("organization_id")
    val organizationId: String,
    @SerializedName("roles")
    val roles: List<Any>,
    @SerializedName("school_id")
    val schoolId: Int,
    @SerializedName("school_shortname")
    val schoolShortname: String,
    @SerializedName("subject_ids")
    val subjectIds: List<Any>,
    @SerializedName("type")
    val type: String,
    @SerializedName("user_id")
    val userId: Int
)