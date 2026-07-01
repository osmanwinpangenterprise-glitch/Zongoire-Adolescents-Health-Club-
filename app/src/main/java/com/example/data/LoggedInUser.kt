package com.example.data

enum class UserRole(val displayName: String) {
    ADMINISTRATOR("Administrator"),
    FACILITATOR("Facilitator"),
    VIEWER("Viewer")
}

data class LoggedInUser(
    val username: String,
    val role: UserRole
)
