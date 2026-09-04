package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.data.repository.AuthRepository

class AuthViewModel : ViewModel() {

    // Kết nối với kho chứa AuthRepository (đã gọi API Firebase)
    private val repository = AuthRepository()

    // Biến trạng thái: Xác định xem app có đang chờ mạng (loading) không
    var isLoading by mutableStateOf(false)
        private set

    // Biến trạng thái: Lưu trữ câu thông báo lỗi để hiển thị lên màn hình
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Biến trạng thái: Xác định đăng nhập/đăng ký thành công chưa
    var isSuccess by mutableStateOf(false)
        private set

    // Kiểm tra xem người dùng đã đăng nhập từ trước chưa (để điều hướng)
    val isUserLoggedIn: Boolean
        get() = repository.currentUser != null

    // Lệnh Đăng ký (Có nhận thêm biến username từ giao diện của bạn)
    fun register(username: String, email: String, password: String, onSuccess: () -> Unit) {
        isLoading = true
        errorMessage = null

        // Firebase mặc định đăng ký bằng email & password
        repository.register(email, password) { success, error ->
            isLoading = false
            if (success) {
                // (Tùy chọn nâng cao) Sau này bạn có thể dùng biến username
                // để cập nhật Profile Firebase tại đây.
                isSuccess = true
                onSuccess()
            } else {
                errorMessage = error
            }
        }
    }

    // Lệnh Đăng nhập
    fun login(email: String, password: String, onSuccess: () -> Unit) {
        isLoading = true
        errorMessage = null
        repository.login(email, password) { success, error ->
            isLoading = false
            if (success) {
                isSuccess = true
                onSuccess()
            } else {
                errorMessage = error
            }
        }
    }

    // Lệnh Đăng xuất
    fun logout() {
        repository.logout()
        isSuccess = false
    }
}