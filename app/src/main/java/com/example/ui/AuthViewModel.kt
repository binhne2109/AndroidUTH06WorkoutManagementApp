package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class AuthViewModel : ViewModel() {
    private val _users = MutableStateFlow(
        mutableListOf(
            User("admin", "admin@example.com", "123456")
        )
    )

    fun register(username: String, email: String, password: String): Boolean {
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
