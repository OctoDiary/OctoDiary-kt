package org.bxkr.octodiary.network.interfaces

import org.bxkr.octodiary.Diary
import org.bxkr.octodiary.models.homeworks.HomeworksResponse
import org.bxkr.octodiary.models.mark.MarkInfo
import org.bxkr.octodiary.models.marklistdate.MarkListDate
import org.bxkr.octodiary.models.marklistsubject.MarkListSubject
import org.bxkr.octodiary.models.marklistsubject.MarkListSubjectItem
import org.bxkr.octodiary.models.profile.ProfileResponse
import org.bxkr.octodiary.models.schoolinfo.SchoolInfo
import org.bxkr.octodiary.models.visits.VisitsResponse
import org.bxkr.octodiary.network.NetworkService.BaseUrl
import org.bxkr.octodiary.network.NetworkService.MESAPIConfig
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Main MES API.
 *
 * baseUrl - [BaseUrl.MOS_SCHOOL_API] or [BaseUrl.MOSREG_SCHOOL_API]
 */
interface MainSchoolAPI {
    companion object : BaseUrls() {
        override fun getBaseUrl(diary: Diary): String {
            return when (diary) {
                Diary.MES -> BaseUrl.MOS_SCHOOL_API
                Diary.MySchool -> BaseUrl.MOSREG_SCHOOL_API
            }
        }
    }

    /**
     * Gets info about mark.
     *
     * @param accessToken Access token.
     * @param markId Mark ID.
     * @param studentId Student ID.
     * @param mesSubsystem MES subsystem (["familymp"][MESAPIConfig.FAMILYMP] by default).
     * @return [MarkInfo]
     */
    @GET("family/mobile/v1/marks/{mark_id}")
    fun markInfo(
        @Header("auth-token") accessToken: String,
        @Path("mark_id") markId: Long,
        @Query("student_id") studentId: Long,
        @Header("X-Mes-Subsystem") mesSubsystem: String = MESAPIConfig.FAMILYMP
    ): Call<MarkInfo>

    /**
     * Gets user's profile details.
     *
     * @param accessToken Access token.
     * @param mesSubsystem MES subsystem (["familymp"][MESAPIConfig.FAMILYMP] by default).
     * @return [ProfileResponse]
     */
    @GET("family/mobile/v1/profile")
    fun profile(
        @Header("auth-token") accessToken: String,
        @Header("X-Mes-Subsystem") mesSubsystem: String = MESAPIConfig.FAMILYMP
    ): Call<ProfileResponse>

    /**
     * Gets info about visits.
     *
     * @param accessToken Access token.
     * @param contractId Contract ID.
     * @param fromDate Start date to get visits.
     * @param toDate End date to get visits.
     * @param mesSubsystem MES subsystem (["familymp"][MESAPIConfig.FAMILYMP] by default).
     * @return [VisitsResponse]
     */
    @GET("family/mobile/v1/visits")
    fun visits(
        @Header("auth-token") accessToken: String,
        @Query("contract_id") contractId: Long,
        @Query("from") fromDate: String,
        @Query("to") toDate: String,
        @Header("X-Mes-Subsystem") mesSubsystem: String = MESAPIConfig.FAMILYMP
    ): Call<VisitsResponse>

    /**
     * Gets marks for date range.
     *
     * @param accessToken Access token.
     * @param studentId Student ID.
     * @param fromDate Start of mark list in yyyy-MM-dd format.
     * @param toDate End of mark list in yyyy-MM-dd format.
     * @param mesSubsystem MES subsystem (["familymp"][MESAPIConfig.FAMILYMP] by default).
     * @return [MarkListDate]
     */
    @GET("family/mobile/v1/marks")
    fun markList(
        @Header("auth-token") accessToken: String,
        @Query("student_id") studentId: Long,
        @Query("from") fromDate: String,
        @Query("to") toDate: String,
        @Header("X-Mes-Subsystem") mesSubsystem: String = MESAPIConfig.FAMILYMP
    ): Call<MarkListDate>

    /**
     * Gets homeworks for date range.
     *
     * @param accessToken Access token.
     * @param studentId Student ID.
     * @param fromDate Start of homework list in yyyy-MM-dd format.
     * @param toDate End of homework list in yyyy-MM-dd format.
     * @param sortField Field by which sorting is performed (["date"][MESAPIConfig.DATE_FIELD] by default).
     * @param sortDirection Sorting direction (["asc"][MESAPIConfig.ASCENDING] by default).
     * @param mesSubsystem MES subsystem (["familymp"][MESAPIConfig.FAMILYMP] by default).
     * @return [HomeworksResponse]
     */
    @GET("family/mobile/v1/homeworks/short")
    fun homeworks(
        @Header("auth-token") accessToken: String,
        @Query("student_id") studentId: Long,
        @Query("from") fromDate: String,
        @Query("to") toDate: String,
        @Query("sort_column") sortField: String = MESAPIConfig.DATE_FIELD,
        @Query("sort_direction") sortDirection: String = MESAPIConfig.ASCENDING,
        @Header("X-Mes-Subsystem") mesSubsystem: String = MESAPIConfig.FAMILYMP
    ): Call<HomeworksResponse>

    /**
     * Gets school info.
     *
     * @param accessToken Access token.
     * @param schoolId School ID.
     * @param classUnitId Class unit ID.
     * @param mesSubsystem MES subsystem (["familymp"][MESAPIConfig.FAMILYMP] by default).
     * @return [SchoolInfo]
     */
    @GET("family/mobile/v1/school_info")
    fun schoolInfo(
        @Header("auth-token") accessToken: String,
        @Query("school_id") schoolId: Long,
        @Query("class_unit_id") classUnitId: Long,
        @Header("X-Mes-Subsystem") mesSubsystem: String = MESAPIConfig.FAMILYMP
    ): Call<SchoolInfo>

    /**
     * Gets marks by subject.
     *
     * @param accessToken Access token.
     * @param studentId Student ID.
     * @param mesSubsystem MES subsystem (["familymp"][MESAPIConfig.FAMILYMP] by default).
     * @return List of [MarkListSubjectItem]s.
     */
    @GET("family/mobile/v1/subject_marks/short")
    fun subjectMarks(
        @Header("auth-token") accessToken: String,
        @Query("student_id") studentId: Long,
        @Header("X-Mes-Subsystem") mesSubsystem: String = MESAPIConfig.FAMILYMP
    ): Call<MarkListSubject>
}