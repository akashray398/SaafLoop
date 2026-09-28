package com.example.saafloop.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Onboarding : Screen("onboarding", "Onboarding", Icons.Default.Info)
    data object AccessChoice : Screen("access_choice", "Access Choice", Icons.Default.Lock)
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Explore : Screen("explore", "Explore", Icons.Default.Search)
    data object Activity : Screen("activity", "Activity", Icons.AutoMirrored.Filled.List)
    data object Profile : Screen("profile", "Profile", Icons.Default.AccountCircle)
    data object ReportWaste : Screen("report_waste", "Report Waste", Icons.Default.Home)
    data object CoordinatorDashboard : Screen("coordinator_dashboard", "Coordinator Dashboard", Icons.Default.SupervisorAccount)
    data object ReportReview : Screen("report_review/{caseId}", "Review Report", Icons.Default.RateReview) {
        fun createRoute(caseId: String) = "report_review/$caseId"
    }
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Explore,
    Screen.Activity,
    Screen.Profile,
)
