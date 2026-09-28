package com.example.saafloop.core.model

enum class UserRole(val label: String, val description: String) {
    CITIZEN("Citizen", "Resident reporting public waste"),
    VOLUNTEER("Volunteer", "Community helper assisting cleanups"),
    COORDINATOR("Coordinator", "Municipal/Community reviewer & dispatcher"),
    ADMINISTRATOR("Administrator", "Full system & audit log manager")
}
