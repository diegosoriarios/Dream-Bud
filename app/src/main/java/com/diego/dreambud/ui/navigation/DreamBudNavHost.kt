package com.diego.dreambud.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.diego.dreambud.ui.screens.DreamAnalyticsScreen
import com.diego.dreambud.ui.screens.DreamCalendarScreen
import com.diego.dreambud.ui.screens.DreamDetailScreen
import com.diego.dreambud.ui.screens.DreamEditScreen
import com.diego.dreambud.ui.screens.DreamListScreen
import com.diego.dreambud.ui.screens.LlmSettingsScreen
import com.diego.dreambud.ui.screens.LucidLabScreen
import com.diego.dreambud.ui.viewmodel.DreamViewModel

sealed class Screen(val route: String) {
    object List : Screen("dream_list")
    object Edit : Screen("dream_edit/{dreamId}") {
        fun createRoute(dreamId: Long) = "dream_edit/$dreamId"
    }
    object Detail : Screen("dream_detail/{dreamId}") {
        fun createRoute(dreamId: Long) = "dream_detail/$dreamId"
    }
    object Settings : Screen("llm_settings")
    object Analytics : Screen("dream_analytics")
    object Calendar : Screen("dream_calendar")
    object LucidLab : Screen("lucid_lab")
}

@Composable
fun DreamBudNavHost(
    viewModel: DreamViewModel,
    startOnDreamEditor: Boolean = false,
    dreamEditorPrefillTitle: String? = null,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = if (startOnDreamEditor) {
            Screen.Edit.createRoute(0L)
        } else {
            Screen.List.route
        }
    ) {
        composable(Screen.List.route) {
            DreamListScreen(
                viewModel = viewModel,
                onNavigateToEdit = { dreamId ->
                    navController.navigate(Screen.Edit.createRoute(dreamId))
                },
                onNavigateToDetail = { dreamId ->
                    navController.navigate(Screen.Detail.createRoute(dreamId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToAnalytics = {
                    navController.navigate(Screen.Analytics.route)
                },
                onNavigateToCalendar = {
                    navController.navigate(Screen.Calendar.route)
                },
                onNavigateToLucidLab = {
                    navController.navigate(Screen.LucidLab.route)
                }
            )
        }

        composable(
            route = Screen.Edit.route,
            arguments = listOf(
                navArgument("dreamId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val dreamId = backStackEntry.arguments?.getLong("dreamId") ?: 0L
            DreamEditScreen(
                dreamId = dreamId,
                viewModel = viewModel,
                initialTitle = dreamEditorPrefillTitle.takeIf { dreamId == 0L },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("dreamId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val dreamId = backStackEntry.arguments?.getLong("dreamId") ?: 0L
            DreamDetailScreen(
                dreamId = dreamId,
                viewModel = viewModel,
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.Edit.createRoute(id))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            LlmSettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Analytics.route) {
            DreamAnalyticsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Calendar.route) {
            DreamCalendarScreen(
                viewModel = viewModel,
                onNavigateToEdit = { dreamId ->
                    navController.navigate(Screen.Edit.createRoute(dreamId))
                },
                onNavigateToDetail = { dreamId ->
                    navController.navigate(Screen.Detail.createRoute(dreamId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.LucidLab.route) {
            LucidLabScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
