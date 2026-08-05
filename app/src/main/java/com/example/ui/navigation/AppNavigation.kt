package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.AuthViewModel
import com.example.ui.LoginScreen
import com.example.ui.RegisterScreen
import com.example.ui.WorkoutScreen
import com.example.ui.WorkoutViewModel

object Route {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val WORKOUT_LIST = "workout_list"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val workoutViewModel: WorkoutViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Route.LOGIN,
    ) {
        composable(Route.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Route.WORKOUT_LIST) {
                        popUpTo(Route.LOGIN) { inclusive = true }
                    }
                },
            ) {
                navController.navigate(Route.REGISTER)
            }
        }
        composable(Route.REGISTER) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.popBackStack()
                },
                onBackToLogin = {
                    navController.popBackStack()
                },
            )
        }
        composable(Route.WORKOUT_LIST) {
            WorkoutScreen(viewModel = workoutViewModel)
        }
    }
}
