package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.WorkoutScreen
import com.example.ui.WorkoutViewModel

object Route {
    const val WORKOUT_LIST = "workout_list"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: WorkoutViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Route.WORKOUT_LIST,
    ) {
        composable(Route.WORKOUT_LIST) {
            WorkoutScreen(viewModel = viewModel)
        }
    }
}