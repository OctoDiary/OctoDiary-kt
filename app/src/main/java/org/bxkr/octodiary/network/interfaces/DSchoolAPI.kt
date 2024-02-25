package org.bxkr.octodiary.network.interfaces

import org.bxkr.octodiary.Diary
import org.bxkr.octodiary.models.classmembers.ClassMember
import org.bxkr.octodiary.models.mealbalance.MealBalance
import org.bxkr.octodiary.models.profilesid.ProfileId
import org.bxkr.octodiary.models.profilesid.ProfilesId
import org.bxkr.octodiary.network.MESOnly
import org.bxkr.octodiary.network.NetworkService.BaseUrl
import org.bxkr.octodiary.network.NetworkService.MESAPIConfig
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * Old MES API which however is used.
 *
 * baseUrl - [BaseUrl.MOS_DNEVNIK] and [BaseUrl.MOSREG_DNEVNIK]
 */
interface DSchoolAPI {
    companion object : BaseUrls() {
        override fun getBaseUrl(diary: Diary): String {
            return when (diary) {
                Diary.MES -> BaseUrl.MOS_DNEVNIK
                Diary.MySchool -> BaseUrl.MOSREG_DNEVNIK
            }
        }
    }

    /**
     * This is a final **REQUIRED** stage of authorization.
     * Token that haven't been used in this request is not eligible for other requests.
     * Basically, this method just returns list containing all user's profiles id.
     * @param authHeader Token to be activated.
     * @return List of [ProfileId]s.
     **/
    @GET("acl/api/users/profile_info")
    fun profilesId(
        @Header("auth-token") authHeader: String,
        @Header("partner-source-id") partnerSourceId: String = "MOBILE"
    ): Call<ProfilesId>

    /**
     * Gets class members.
     *
     * @param accessToken Access token.
     * @param classUnitId Class unit ID.
     * @param perPage Per page users count ([Int.MAX_VALUE] by default).
     * @param types Types to filter ("student" by default).
     * @return List of [ClassMember]s.
     */
    @GET("core/api/profiles")
    fun classMembers(
        @Header("auth-token") accessToken: String,
        @Query("class_unit_id") classUnitId: Long,
        @Query("per_page") perPage: Int = Int.MAX_VALUE,
        @Query("types") types: String = "student"
    ): Call<List<ClassMember>>

    /**
     * Gets meal balance.
     *
     * @param accessToken Access token.
     * @param contractId Contract ID.
     * @param mesSubsystem MES subsystem (["familymp"][MESAPIConfig.FAMILYMP] by default).
     * @return [MealBalance]
     */
    @MESOnly
    @GET("api/meals/v1/clients")
    fun mealBalance(
        @Header("auth-token") accessToken: String,
        @Query("contractId") contractId: Long,
        @Header("X-Mes-Subsystem") mesSubsystem: String = MESAPIConfig.FAMILYMP
    ): Call<MealBalance>

    @GET("ej/family/homework/launch")
    fun launchMaterial(
        @Header("auth-token") accessToken: String,
        @Query("homework_entry_id") homeworkEntryId: Long,
        @Query("material_id") materialId: String,
        @Header("X-Mes-Subsystem") mesSubsystem: String = MESAPIConfig.FAMILYMP
    ): Call<String>
}