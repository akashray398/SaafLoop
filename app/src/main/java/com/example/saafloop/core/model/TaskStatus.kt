package com.example.saafloop.core.model

/**
 * Task lifecycle states for Field Operations and Worker assignment.
 */
enum class TaskStatus(val label: String, val description: String) {
    CREATED("Created", "Task generated from verified citizen report"),
    UNASSIGNED("Unassigned", "Awaiting worker or team assignment"),
    ASSIGNED("Assigned", "Assigned to field worker or team, awaiting acceptance"),
    DECLINED("Declined", "Declined by assigned worker, needs reassignment"),
    ACCEPTED("Accepted", "Accepted by field worker"),
    EN_ROUTE("En Route", "Worker traveling to waste site"),
    IN_PROGRESS("In Progress", "Cleanup work actively underway"),
    COMPLETION_SUBMITTED("Submitted", "Worker submitted completion with evidence"),
    UNDER_VERIFICATION("Under Review", "Coordinator reviewing completed work"),
    REWORK_REQUESTED("Rework Needed", "Coordinator requested additional cleanup"),
    COMPLETED("Completed", "Approved by coordinator & issue resolved"),
    CANCELLED("Cancelled", "Task cancelled")
}
