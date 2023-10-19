package org.bxkr.octodiary.models.classmembers


import com.google.gson.annotations.SerializedName

data class ClassMember(
    @SerializedName("agree_pers_data")
    val agreePersData: Boolean,
    @SerializedName("class_unit")
    val classUnit: ClassUnit?,
    @SerializedName("id")
    val id: Int,
    @SerializedName("person_id")
    val personId: String?,
    @SerializedName("roles")
    val roles: List<Any>,
    @SerializedName("school")
    val school: School,
    @SerializedName("staff_id")
    val staffId: String?,
    @SerializedName("type")
    val type: String,
    @SerializedName("user")
    val user: User,
    @SerializedName("user_id")
    val userId: Int
)