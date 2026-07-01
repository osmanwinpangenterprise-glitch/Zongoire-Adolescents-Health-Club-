package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "behavior_assessments")
data class BehaviorAssessment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val memberId: String,
    val memberName: String,
    
    // Mother/Guardian Assessment (1 to 10)
    val motherObedience: Int,
    val motherHelpfulness: Int,
    val motherCommunication: Int,
    val motherRuleCompliance: Int,
    
    // Teacher/School Assessment (1 to 10)
    val teacherPunctuality: Int,
    val teacherAttentiveness: Int,
    val teacherRespect: Int,
    val teacherHomework: Int,
    
    // Metadata
    val comments: String = "",
    val assessedBy: String,
    val isSynced: Boolean = false
) {
    val totalMotherScore: Int
        get() = motherObedience + motherHelpfulness + motherCommunication + motherRuleCompliance
        
    val totalTeacherScore: Int
        get() = teacherPunctuality + teacherAttentiveness + teacherRespect + teacherHomework
        
    val grandTotalScore: Int
        get() = totalMotherScore + totalTeacherScore
}
