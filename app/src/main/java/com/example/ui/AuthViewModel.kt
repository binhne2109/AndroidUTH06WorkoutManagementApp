package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class AuthViewModel : ViewModel() {
    // Danh sách user lưu trong bộ nhớ (sẽ mất khi khởi động lại app)
    // Đã có sẵn tài khoản admin mặc định
    private val _users = MutableStateFlow(
        mutableListOf(
            User("admin", "admin@example.com", "123456")
        )
    )

    fun register(username: String, email: String, password: String): Boolean {
        // Kiểm tra xem username đã tồn tại chưa
        if (_users.value.any { it.username == username }) {
            return false
        }
        
        _users.update { currentUsers ->
            val newList = currentUsers.toMutableList()
            newList.add(User(username, email, password))
            newList
        }
        return true
    }

    fun login(username: String, password: String): Boolean {
        return _users.value.any { it.username == username && it.password == password }
    }
}
