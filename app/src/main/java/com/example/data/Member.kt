package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class Member(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val memberId: String,
    val registrationDate: String,
    val fullName: String,
    val dob: String,
    val age: Int,
    val gender: String, // Male, Female, Prefer not to say
    val ageGroup: String, // Junior (10–14), Senior (15–19)
    val community: String,
    val religion: String,
    val schoolStatus: String, // Currently in school, Out of school, Completed school
    val schoolName: String,
    val classYear: String,
    val occupation: String,
    val guardianName: String,
    val relationship: String,
    val contactNumber: String,
    val parentAware: String, // Yes, No
    val consentSigned: String, // Yes, No
    val healthConditionKnown: String, // Yes, No
    val healthConditionDetails: String,
    val visitedChpsLast6Months: String, // Yes, No
    val nhisCard: String, // Yes, No
    val registeredBy: String,
    val oathTaken: String, // Yes, No
    val membershipCardIssued: String, // Yes, No
    val membershipTier: String, // Associate, Active, Star
    val isSynced: Boolean = false
)
