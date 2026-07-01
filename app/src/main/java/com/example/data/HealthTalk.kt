package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_talks")
data class HealthTalk(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val topic: String,
    val facilitator: String,
    val location: String,
    val participantsCount: Int,
    val keyIssues: String,
    val actionPoints: String,
    val isSynced: Boolean = false
)
