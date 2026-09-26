package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.AuthViewModel
import com.example.ui.LoginScreen
import com.example.ui.RegisterScreen
import com.example.ui.StatisticsViewModel
import com.example.ui.TemplatePlanScreen
import com.example.ui.TemplatePlanViewModel
import com.example.ui.WorkoutScreen
import com.example.ui.WorkoutViewModel
import com.example.ui.calendar.CalendarScreen
import com.example.ui.calendar.CalendarViewModel
import com.example.ui.stats.StatisticsScreen

object Route {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val WORKOUT_LIST = "workout_list"
    const val STATISTICS = "statistics"
    const val TEMPLATES_PLANS = "templates_plans"
    const val CALENDAR = "calendar"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val workoutViewModel: WorkoutViewModel = viewModel()
    val templatePlanViewModel: TemplatePlanViewModel = viewModel()
    val calendarViewModel: CalendarViewModel = viewModel()

    val startDestination = if (authViewModel.isUserLoggedIn) Route.WORKOUT_LIST else Route.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Route.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Route.WORKOUT_LIST) {
                        popUpTo(Route.LOGIN) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Route.REGISTER)
                }
            )
        }

        composable(Route.REGISTER) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Route.WORKOUT_LIST) {
                        popUpTo(Route.LOGIN) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                },
            )
        }

        composable(Route.WORKOUT_LIST) {
            LaunchedEffect(key1 = Unit) {
                workoutViewModel.loadWorkouts()
            }

            WorkoutScreen(
                viewModel = workoutViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Route.LOGIN) {
                        popUpTo(Route.WORKOUT_LIST) { inclusive = true }
                    }
                },
                onNavigateToStatistics = {
                    navController.navigate(Route.STATISTICS)
                },
                onOpenTemplates = {
                    navController.navigate(Route.TEMPLATES_PLANS)
                },
                onOpenCalendar = {
                    navController.navigate(Route.CALENDAR)
                }
            )
        }

        composable(Route.TEMPLATES_PLANS) {
            TemplatePlanScreen(
                templateViewModel = templatePlanViewModel,
                workoutViewModel = workoutViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Route.CALENDAR) {
            CalendarScreen(
                viewModel = calendarViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Route.STATISTICS) {
            val statisticsViewModel: StatisticsViewModel = viewModel()
            val workoutUiState by workoutViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(workoutUiState.filteredWorkouts) {
                statisticsViewModel.updateDataFromWorkouts(workoutUiState.filteredWorkouts)
            }

            StatisticsScreen(
                viewModel = statisticsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}