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
import com.example.ui.WorkoutScreen
import com.example.ui.WorkoutViewModel
import com.example.ui.stats.StatisticsScreen

object Route {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val WORKOUT_LIST = "workout_list"
    const val STATISTICS = "statistics" // Bổ sung Route cho màn hình Thống kê
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val workoutViewModel: WorkoutViewModel = viewModel()

    // LẤY TRẠNG THÁI TỪ FIREBASE:
    // Nếu đã đăng nhập -> vô thẳng WORKOUT_LIST, nếu chưa -> bắt đầu từ LOGIN
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
                // Kết nối nút bấm chuyển sang Thống kê
                onNavigateToStatistics = {
                    navController.navigate(Route.STATISTICS)
                }
            )
        }

        // [Thành viên 4] Màn hình Thống kê & Biểu đồ
        composable(Route.STATISTICS) {
            val statisticsViewModel: StatisticsViewModel = viewModel()
            val workoutUiState by workoutViewModel.uiState.collectAsStateWithLifecycle()

            // TỰ ĐỘNG ĐỒNG BỘ: Cập nhật dữ liệu Thống kê ngay khi danh sách bài tập thay đổi
            LaunchedEffect(workoutUiState.filteredWorkouts) {
                statisticsViewModel.updateDataFromWorkouts(workoutUiState.filteredWorkouts)
            }

            StatisticsScreen(
                viewModel = statisticsViewModel
            )
        }
    }
}