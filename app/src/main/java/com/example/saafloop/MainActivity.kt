package com.example.saafloop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.saafloop.core.navigation.Screen
import com.example.saafloop.core.navigation.bottomNavItems
import com.example.saafloop.feature.activity.ActivityScreen
import com.example.saafloop.feature.explore.ExploreScreen
import com.example.saafloop.feature.home.HomeScreen
import com.example.saafloop.feature.profile.ProfileScreen
import com.example.saafloop.feature.report.ReportWasteScreen
import com.example.saafloop.ui.theme.SaafLoopTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize the standard Android Core Splashscreen API
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            SaafLoopTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    topBar = {
                        if (currentRoute != Screen.ReportWaste.route) {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = "SaafLoop",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                },
                                actions = {
                                    IconButton(onClick = { /* Future Notifications workflow */ }) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsNone,
                                            contentDescription = "Notifications Placeholder",
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background,
                                    titleContentColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    },
                    bottomBar = {
                        if (currentRoute != Screen.ReportWaste.route) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 8.dp
                            ) {
                                bottomNavItems.forEach { screen ->
                                    val selected = currentRoute == screen.route
                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                imageVector = screen.icon,
                                                contentDescription = screen.title
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = screen.title,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        selected = selected,
                                        onClick = {
                                            if (currentRoute != screen.route) {
                                                navController.navigate(screen.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Home.route
                        ) {
                            composable(Screen.Home.route) {
                                HomeScreen(
                                    onReportWasteClick = {
                                        navController.navigate(Screen.ReportWaste.route)
                                    },
                                    onNavigateToExplore = {
                                        navController.navigate(Screen.Explore.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    onNavigateToActivity = {
                                        navController.navigate(Screen.Activity.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                            composable(Screen.Explore.route) {
                                ExploreScreen()
                            }
                            composable(Screen.Activity.route) {
                                ActivityScreen()
                            }
                            composable(Screen.Profile.route) {
                                ProfileScreen()
                            }
                            composable(Screen.ReportWaste.route) {
                                ReportWasteScreen(
                                    onBackClick = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
