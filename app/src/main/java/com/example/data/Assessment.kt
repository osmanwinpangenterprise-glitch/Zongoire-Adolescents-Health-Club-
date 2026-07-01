package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assessments")
data class Assessment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val memberId: String,
    val memberName: String,
    val hygiene: Int, // 1 to 5 or 1 to 10
    val character: Int,
    val behaviorSchool: Int,
    val behaviorHome: Int,
    val dressing: Int,
    val contribution: Int,
    val comments: String = "",
    val assessedBy: String,
    val periodType: String, // "Weekly", "Monthly", "Quarterly", "Mid-Year", "Annual"
    val periodLabel: String, // e.g. "Week 27, 2026", "July 2026"
    val isSynced: Boolean = false
) {
    val totalScore: Int
        get() = hygiene + character + behaviorSchool + behaviorHome + dressing + contribution
        
    val averageScore: Double
        get() = totalScore / 6.0
}
