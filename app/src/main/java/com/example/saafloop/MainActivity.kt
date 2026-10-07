package com.example.saafloop

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.saafloop.core.data.UserAccessState
import com.example.saafloop.core.navigation.Screen
import com.example.saafloop.core.navigation.bottomNavItems
import com.example.saafloop.core.util.NotificationChannelManager
import com.example.saafloop.feature.activity.ActivityScreen
import com.example.saafloop.feature.auth.AccessChoiceScreen
import com.example.saafloop.feature.community.ActivityDetailScreen
import com.example.saafloop.feature.community.CommunityHomeScreen
import com.example.saafloop.feature.community.CreateActivityScreen
import com.example.saafloop.feature.coordinator.CoordinatorDashboardScreen
import com.example.saafloop.feature.coordinator.ReportReviewScreen
import com.example.saafloop.feature.explore.ExploreScreen
import com.example.saafloop.feature.fieldops.FieldDashboardScreen
import com.example.saafloop.feature.fieldops.FieldTaskDetailScreen
import com.example.saafloop.feature.home.HomeScreen
import com.example.saafloop.feature.notification.NotificationCenterScreen
import com.example.saafloop.feature.notification.NotificationPreferencesScreen
import com.example.saafloop.feature.onboarding.OnboardingScreen
import com.example.saafloop.feature.profile.ProfileScreen
import com.example.saafloop.feature.report.ReportWasteScreen
import com.example.saafloop.ui.MainUiState
import com.example.saafloop.ui.MainViewModel
import com.example.saafloop.ui.theme.SaafLoopTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value is MainUiState.Loading
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Android System Notification Channels
        NotificationChannelManager.createNotificationChannels(applicationContext)

        setContent {
            SaafLoopTheme {
                val uiState by viewModel.uiState.collectAsState()

                when (val state = uiState) {
                    is MainUiState.Loading -> {}
                    is MainUiState.Success -> {
                        SaafLoopAppContent(
                            initialState = state,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaafLoopAppContent(
    initialState: MainUiState.Success,
    viewModel: MainViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val startDestination = remember {
        when {
            !initialState.isOnboardingCompleted -> Screen.Onboarding.route
            initialState.userAccessState !is UserAccessState.Guest -> Screen.AccessChoice.route
            else -> Screen.Home.route
        }
    }

    val isMainTabScreen = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        topBar = {
            if (isMainTabScreen) {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.app_name),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.NotificationCenter.route) }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notification Center",
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
            if (isMainTabScreen) {
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
                startDestination = startDestination
            ) {
                // First-Launch Onboarding Route
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onFinishOnboarding = {
                            viewModel.completeOnboarding()
                            navController.navigate(Screen.AccessChoice.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        },
                        isRevisitMode = false
                    )
                }

                // Profile Revisit Onboarding Route
                composable("${Screen.Onboarding.route}_revisit") {
                    OnboardingScreen(
                        onFinishOnboarding = {
                            navController.popBackStack()
                        },
                        isRevisitMode = true
                    )
                }

                // Access Choice Route
                composable(Screen.AccessChoice.route) {
                    AccessChoiceScreen(
                        onContinueAsGuest = {
                            viewModel.startGuestSession()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.AccessChoice.route) { inclusive = true }
                            }
                        }
                    )
                }

                // Main Home Route
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

                // Explore Route
                composable(Screen.Explore.route) {
                    ExploreScreen(
                        onReportWasteClick = {
                            navController.navigate(Screen.ReportWaste.route)
                        },
                        onReportWasteAtLocation = { lat, lng, areaName ->
                            navController.navigate("${Screen.ReportWaste.route}?lat=$lat&lng=$lng&locationName=$areaName")
                        },
                        onOpenReportDetail = { caseId ->
                            navController.navigate(Screen.ReportReview.createRoute(caseId))
                        },
                        onOpenTaskDetail = { taskId ->
                            navController.navigate(Screen.FieldTaskDetail.createRoute(taskId))
                        },
                        onOpenActivityDetail = { activityId ->
                            navController.navigate(Screen.ActivityDetail.createRoute(activityId))
                        }
                    )
                }

                // Community Home Route
                composable(Screen.CommunityHome.route) {
                    CommunityHomeScreen(
                        onOpenActivityDetail = { activityId ->
                            navController.navigate(Screen.ActivityDetail.createRoute(activityId))
                        },
                        onCreateActivityClick = {
                            navController.navigate(Screen.CreateActivity.route)
                        }
                    )
                }

                // Community Activity Detail Route
                composable(
                    route = Screen.ActivityDetail.route,
                    arguments = listOf(
                        androidx.navigation.navArgument("activityId") {
                            type = androidx.navigation.NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val activityId = backStackEntry.arguments?.getString("activityId") ?: ""
                    ActivityDetailScreen(
                        activityId = activityId,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                // Propose Community Drive Route
                composable(Screen.CreateActivity.route) {
                    CreateActivityScreen(
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                // Activity Route
                composable(Screen.Activity.route) {
                    ActivityScreen(
                        onOpenDraft = { draftId ->
                            navController.navigate("${Screen.ReportWaste.route}?draftId=$draftId")
                        },
                        onStartNewReport = {
                            navController.navigate(Screen.ReportWaste.route)
                        }
                    )
                }

                // Profile Route
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onNavigateToOnboarding = {
                            navController.navigate("${Screen.Onboarding.route}_revisit")
                        },
                        onExitGuestMode = {
                            viewModel.exitGuestMode()
                            navController.navigate(Screen.AccessChoice.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        },
                        onOpenCoordinatorDashboard = {
                            navController.navigate(Screen.CoordinatorDashboard.route)
                        },
                        onOpenFieldDashboard = {
                            navController.navigate(Screen.FieldDashboard.route)
                        }
                    )
                }

                // Coordinator Dashboard Route
                composable(Screen.CoordinatorDashboard.route) {
                    CoordinatorDashboardScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onReviewReport = { caseId ->
                            navController.navigate(Screen.ReportReview.createRoute(caseId))
                        }
                    )
                }

                // Detailed Report Review Route
                composable(
                    route = Screen.ReportReview.route,
                    arguments = listOf(
                        androidx.navigation.navArgument("caseId") {
                            type = androidx.navigation.NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val caseId = backStackEntry.arguments?.getString("caseId") ?: ""
                    ReportReviewScreen(
                        caseId = caseId,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                // Field Operations Dashboard Route
                composable(Screen.FieldDashboard.route) {
                    FieldDashboardScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onOpenTaskDetail = { taskId ->
                            navController.navigate(Screen.FieldTaskDetail.createRoute(taskId))
                        }
                    )
                }

                // Field Task Detail & Execution Route
                composable(
                    route = Screen.FieldTaskDetail.route,
                    arguments = listOf(
                        androidx.navigation.navArgument("taskId") {
                            type = androidx.navigation.NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
                    FieldTaskDetailScreen(
                        taskId = taskId,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                // Report Waste Route (with optional parameters)
                composable(
                    route = "${Screen.ReportWaste.route}?draftId={draftId}&lat={lat}&lng={lng}&locationName={locationName}",
                    arguments = listOf(
                        androidx.navigation.navArgument("draftId") {
                            type = androidx.navigation.NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                        androidx.navigation.navArgument("lat") {
                            type = androidx.navigation.NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                        androidx.navigation.navArgument("lng") {
                            type = androidx.navigation.NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                        androidx.navigation.navArgument("locationName") {
                            type = androidx.navigation.NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val draftId = backStackEntry.arguments?.getString("draftId")
                    val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
                    val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull()
                    val locationName = backStackEntry.arguments?.getString("locationName")
                    ReportWasteScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        draftId = draftId,
                        initialLat = lat,
                        initialLng = lng,
                        initialLocationName = locationName
                    )
                }

                // Notification Center Route
                composable(Screen.NotificationCenter.route) {
                    NotificationCenterScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onOpenPreferences = {
                            navController.navigate(Screen.NotificationPreferences.route)
                        },
                        onNavigateToDeepLink = { deepLink ->
                            handleDeepLink(navController, deepLink)
                        }
                    )
                }

                // Notification Preferences Route
                composable(Screen.NotificationPreferences.route) {
                    NotificationPreferencesScreen(
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

private fun handleDeepLink(navController: androidx.navigation.NavController, deepLink: String) {
    if (deepLink.isBlank()) return
    val uri = Uri.parse(deepLink)
    val path = uri.path ?: uri.schemeSpecificPart
    when {
        path.contains("report_review/") -> {
            val caseId = path.substringAfter("report_review/")
            navController.navigate(Screen.ReportReview.createRoute(caseId))
        }
        path.contains("field_task_detail/") -> {
            val taskId = path.substringAfter("field_task_detail/")
            navController.navigate(Screen.FieldTaskDetail.createRoute(taskId))
        }
        path.contains("activity_detail/") -> {
            val activityId = path.substringAfter("activity_detail/")
            navController.navigate(Screen.ActivityDetail.createRoute(activityId))
        }
        path.contains("coordinator_dashboard") -> {
            navController.navigate(Screen.CoordinatorDashboard.route)
        }
        else -> {
            navController.navigate(Screen.NotificationCenter.route)
        }
    }
}
