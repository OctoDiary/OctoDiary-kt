package org.bxkr.octodiary.models.profile


import com.google.gson.annotations.SerializedName

data class Children(
    @SerializedName("birth_date")
    val birthDate: String,
    @SerializedName("class_level_id")
    val classLevelId: Int,
    @SerializedName("class_name")
    val className: String,
    @SerializedName("class_unit_id")
    val classUnitId: Int,
    @SerializedName("contingent_guid")
    val contingentGuid: String,
    @SerializedName("contract_id")
    val contractId: Int,
    @SerializedName("email")
    val email: Any?,
    @SerializedName("enrollment_date")
    val enrollmentDate: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("groups")
    val groups: List<Group>,
    @SerializedName("id")
    val id: Int,
    @SerializedName("is_legal_representative")
    val isLegalRepresentative: Boolean,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("middle_name")
    val middleName: String,
    @SerializedName("parallel_curriculum_id")
    val parallelCurriculumId: Int,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("representatives")
    val representatives: List<Representative>,
    @SerializedName("school")
    val school: School,
    @SerializedName("sections")
    val sections: List<Section>,
    @SerializedName("sex")
    val sex: String,
    @SerializedName("snils")
    val snils: String,
    @SerializedName("sudir_account_exists")
    val sudirAccountExists: Boolean,
    @SerializedName("sudir_login")
    val sudirLogin: Any?,
    @SerializedName("type")
    val type: Any?,
    @SerializedName("user_id")
    val userId: Int
)