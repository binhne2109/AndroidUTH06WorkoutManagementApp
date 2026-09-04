package com.example.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // Lấy thông tin người dùng đang đăng nhập
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    // 1. Đăng ký tài khoản với Email & Mật khẩu
    fun register(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Đăng ký thất bại")
                }
            }
    }

    // 2. Đăng nhập với Email & Mật khẩu
    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Đăng nhập thất bại")
                }
            }
    }

    // 3. Đăng xuất
    fun logout() {
        firebaseAuth.signOut()
    }
}