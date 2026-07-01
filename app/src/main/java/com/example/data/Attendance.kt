package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val memberId: String,
    val memberName: String,
    val sessionType: String,
    val status: String, // Present, Absent
    val facilitator: String,
    val isSynced: Boolean = false
)
