package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AppRepository private constructor(context: Context) {

    private val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "zongoire_health_club.db"
    ).fallbackToDestructiveMigration().build()

    val memberDao = database.memberDao()
    val attendanceDao = database.attendanceDao()
    val healthTalkDao = database.healthTalkDao()
    val assessmentDao = database.assessmentDao()
    val behaviorAssessmentDao = database.behaviorAssessmentDao()

    val allMembers: Flow<List<Member>> = memberDao.getAllMembers()
    val allAttendance: Flow<List<Attendance>> = attendanceDao.getAllAttendance()
    val allHealthTalks: Flow<List<HealthTalk>> = healthTalkDao.getAllHealthTalks()
    val allAssessments: Flow<List<Assessment>> = assessmentDao.getAllAssessments()
    val allBehaviorAssessments: Flow<List<BehaviorAssessment>> = behaviorAssessmentDao.getAllBehaviorAssessments()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    companion object {
        @Volatile
        private var INSTANCE: AppRepository? = null

        fun getInstance(context: Context): AppRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = AppRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun getNextMemberId(): String = withContext(Dispatchers.IO) {
        val maxId = memberDao.getMaxId() ?: 0
        val nextId = maxId + 1
        String.format("ZAC-%04d", nextId)
    }

    suspend fun registerMember(member: Member): Long = withContext(Dispatchers.IO) {
        memberDao.insertMember(member)
    }

    suspend fun recordAttendance(attendance: Attendance): Long = withContext(Dispatchers.IO) {
        attendanceDao.insertAttendance(attendance)
    }

    suspend fun recordHealthTalk(healthTalk: HealthTalk): Long = withContext(Dispatchers.IO) {
        healthTalkDao.insertHealthTalk(healthTalk)
    }

    suspend fun recordAssessment(assessment: Assessment): Long = withContext(Dispatchers.IO) {
        assessmentDao.insertAssessment(assessment)
    }

    suspend fun recordBehaviorAssessment(assessment: BehaviorAssessment): Long = withContext(Dispatchers.IO) {
        behaviorAssessmentDao.insertBehaviorAssessment(assessment)
    }

    // Google Sheets Sync Method
    suspend fun syncWithGoogleSheets(webAppUrl: String): SyncResult = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) {
            return@withContext SyncResult(false, "Web App URL is empty. Please configure it in Sync settings.")
        }

        var membersSyncedCount = 0
        var attendanceSyncedCount = 0
        var healthTalksSyncedCount = 0
        var assessmentsSyncedCount = 0
        var errorOccurred = false
        var lastErrorMessage = ""

        try {
            // 1. Sync Members
            val unsyncedMembers = allMembers.first().filter { !it.isSynced }
            for (member in unsyncedMembers) {
                val success = postToGoogleSheets(webAppUrl, "MEMBERS", memberToMap(member))
                if (success) {
                    memberDao.updateSyncStatus(member.id, true)
                    membersSyncedCount++
                } else {
                    errorOccurred = true
                    lastErrorMessage = "Failed to sync Member ${member.memberId}"
                }
            }

            // 2. Sync Attendance
            val unsyncedAttendance = allAttendance.first().filter { !it.isSynced }
            for (att in unsyncedAttendance) {
                val success = postToGoogleSheets(webAppUrl, "ATTENDANCE", attendanceToMap(att))
                if (success) {
                    attendanceDao.updateSyncStatus(att.id, true)
                    attendanceSyncedCount++
                } else {
                    errorOccurred = true
                    lastErrorMessage = "Failed to sync Attendance for ${att.memberId}"
                }
            }

            // 3. Sync Health Talks
            val unsyncedTalks = allHealthTalks.first().filter { !it.isSynced }
            for (talk in unsyncedTalks) {
                val success = postToGoogleSheets(webAppUrl, "HEALTH_TALKS", healthTalkToMap(talk))
                if (success) {
                    healthTalkDao.updateSyncStatus(talk.id, true)
                    healthTalksSyncedCount++
                } else {
                    errorOccurred = true
                    lastErrorMessage = "Failed to sync Health Talk on ${talk.topic}"
                }
            }

            // 4. Sync Assessments
            val unsyncedAssessments = allAssessments.first().filter { !it.isSynced }
            for (assessment in unsyncedAssessments) {
                val success = postToGoogleSheets(webAppUrl, "ASSESSMENTS", assessmentToMap(assessment))
                if (success) {
                    assessmentDao.updateSyncStatus(assessment.id, true)
                    assessmentsSyncedCount++
                } else {
                    errorOccurred = true
                    lastErrorMessage = "Failed to sync Assessment for ${assessment.memberName}"
                }
            }

            if (errorOccurred) {
                SyncResult(false, "Sync completed with errors. $lastErrorMessage. Synced: $membersSyncedCount members, $attendanceSyncedCount attendance, $healthTalksSyncedCount health talks, $assessmentsSyncedCount assessments.")
            } else {
                SyncResult(true, "Successfully synced $membersSyncedCount members, $attendanceSyncedCount attendance, $healthTalksSyncedCount health education, and $assessmentsSyncedCount assessments to Google Sheets!")
            }

        } catch (e: Exception) {
            SyncResult(false, "Sync failed: ${e.message}")
        }
    }

    private fun postToGoogleSheets(webAppUrl: String, action: String, record: Map<String, Any>): Boolean {
        return try {
            val payload = JSONObject().apply {
                put("action", action)
                put("record", JSONObject(record))
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = payload.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(webAppUrl)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: ""
                    val json = JSONObject(bodyString)
                    json.optString("status") == "success"
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun memberToMap(m: Member): Map<String, Any> {
        return mapOf(
            "Member ID" to m.memberId,
            "Registration Date" to m.registrationDate,
            "Full Name" to m.fullName,
            "Date of Birth" to m.dob,
            "Age" to m.age,
            "Gender" to m.gender,
            "Age Group" to m.ageGroup,
            "Community" to m.community,
            "Religion" to m.religion,
            "School Status" to m.schoolStatus,
            "School Name" to m.schoolName,
            "Class or Year" to m.classYear,
            "Occupation" to m.occupation,
            "Guardian Name" to m.guardianName,
            "Relationship" to m.relationship,
            "Contact Number" to m.contactNumber,
            "Parent Aware" to m.parentAware,
            "Consent Signed" to m.consentSigned,
            "Health Condition Known" to m.healthConditionKnown,
            "Health Condition Details" to m.healthConditionDetails,
            "Visited CHPS Last 6 Months" to m.visitedChpsLast6Months,
            "NHIS Card" to m.nhisCard,
            "Registered By" to m.registeredBy,
            "Oath Taken" to m.oathTaken,
            "Membership Card Issued" to m.membershipCardIssued,
            "Membership Tier" to m.membershipTier
        )
    }

    private fun attendanceToMap(a: Attendance): Map<String, Any> {
        return mapOf(
            "Date" to a.date,
            "Member ID" to a.memberId,
            "Member Name" to a.memberName,
            "Session Type" to a.sessionType,
            "Status" to a.status,
            "Facilitator" to a.facilitator
        )
    }

    private fun healthTalkToMap(h: HealthTalk): Map<String, Any> {
        return mapOf(
            "Date" to h.date,
            "Topic" to h.topic,
            "Facilitator" to h.facilitator,
            "Location" to h.location,
            "Participants Count" to h.participantsCount,
            "Key Issues" to h.keyIssues,
            "Action Points" to h.actionPoints
        )
    }

    private fun assessmentToMap(a: Assessment): Map<String, Any> {
        return mapOf(
            "Date" to a.date,
            "Member ID" to a.memberId,
            "Member Name" to a.memberName,
            "Personal Hygiene" to a.hygiene,
            "Character" to a.character,
            "Behavior School" to a.behaviorSchool,
            "Behavior Home" to a.behaviorHome,
            "Dressing" to a.dressing,
            "Contribution" to a.contribution,
            "Comments" to a.comments,
            "Assessed By" to a.assessedBy,
            "Period Type" to a.periodType,
            "Period Label" to a.periodLabel
        )
    }
}

data class SyncResult(val success: Boolean, val message: String)
