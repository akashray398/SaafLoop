package com.example.saafloop.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Explore : Screen("explore", "Explore", Icons.Default.Search)
    object Activity : Screen("activity", "Activity", Icons.AutoMirrored.Filled.List)
    object Profile : Screen("profile", "Profile", Icons.Default.AccountCircle)
    object ReportWaste : Screen("report_waste", "Report Waste", Icons.Default.Home)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Explore,
    Screen.Activity,
    Screen.Profile,
)
