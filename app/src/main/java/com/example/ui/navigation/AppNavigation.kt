package com.example.ui.navigation

import androidx.compose.runtime.LaunchedEffect
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

    // LẤY TRẠNG THÁI TỪ FIREBASE:
    // Nếu đã đăng nhập -> vô thẳng WORKOUT_LIST, nếu chưa -> bắt đầu từ LOGIN
    val startDestination = if (authViewModel.isUserLoggedIn) Route.WORKOUT_LIST else Route.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination, // Cập nhật biến startDestination vào đây
    ) {
        composable(Route.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Route.WORKOUT_LIST) {
                        popUpTo(Route.LOGIN) { inclusive = true }
                    }
                },
                onRegisterClick = { // Gọi đúng tên tham số onRegisterClick bên LoginScreen
                    navController.navigate(Route.REGISTER)
                }
            )
        }
        composable(Route.REGISTER) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    // Firebase tự động đăng nhập khi đăng ký thành công,
                    // nên ta cho người dùng vào thẳng màn hình chính luôn
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

            // THÊM ĐOẠN NÀY: Tự động gọi tải dữ liệu mỗi khi mở màn hình Bài tập
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
                }
            )
        }
    }
}