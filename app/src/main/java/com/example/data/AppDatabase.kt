package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM members ORDER BY id DESC")
    fun getAllMembers(): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE id = :id")
    suspend fun getMemberById(id: Int): Member?

    @Query("SELECT * FROM members WHERE memberId = :memberId")
    suspend fun getMemberByMemberId(memberId: String): Member?

    @Query("SELECT MAX(id) FROM members")
    suspend fun getMaxId(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member): Long

    @Update
    suspend fun updateMember(member: Member)

    @Query("UPDATE members SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Int, synced: Boolean)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY date DESC, id DESC")
    fun getAllAttendance(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE memberId = :memberId")
    fun getAttendanceForMember(memberId: String): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long

    @Query("UPDATE attendance SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Int, synced: Boolean)
}

@Dao
interface HealthTalkDao {
    @Query("SELECT * FROM health_talks ORDER BY date DESC, id DESC")
    fun getAllHealthTalks(): Flow<List<HealthTalk>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthTalk(healthTalk: HealthTalk): Long

    @Query("UPDATE health_talks SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Int, synced: Boolean)
}

@Dao
interface AssessmentDao {
    @Query("SELECT * FROM assessments ORDER BY date DESC, id DESC")
    fun getAllAssessments(): Flow<List<Assessment>>

    @Query("SELECT * FROM assessments WHERE memberId = :memberId ORDER BY date DESC")
    fun getAssessmentsForMember(memberId: String): Flow<List<Assessment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessment(assessment: Assessment): Long

    @Query("UPDATE assessments SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Int, synced: Boolean)
}

@Dao
interface BehaviorAssessmentDao {
    @Query("SELECT * FROM behavior_assessments ORDER BY date DESC, id DESC")
    fun getAllBehaviorAssessments(): Flow<List<BehaviorAssessment>>

    @Query("SELECT * FROM behavior_assessments WHERE memberId = :memberId ORDER BY date DESC")
    fun getBehaviorAssessmentsForMember(memberId: String): Flow<List<BehaviorAssessment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBehaviorAssessment(assessment: BehaviorAssessment): Long

    @Query("UPDATE behavior_assessments SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Int, synced: Boolean)
}

@Database(entities = [Member::class, Attendance::class, HealthTalk::class, Assessment::class, BehaviorAssessment::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun healthTalkDao(): HealthTalkDao
    abstract fun assessmentDao(): AssessmentDao
    abstract fun behaviorAssessmentDao(): BehaviorAssessmentDao
}
