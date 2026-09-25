package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.WorkoutAppTheme

private const val TAG = "STATE_DEMO"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(
            TAG,
            "MainActivity onCreate — savedInstanceState: " +
                    if (savedInstanceState == null) "NULL (cold start)" else "CÓ Bundle (Activity tạo lại)"
        )

        // Tự động yêu cầu quyền gửi thông báo POST_NOTIFICATIONS trên Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }

        enableEdgeToEdge()
        setContent {
            WorkoutAppTheme {
                AppNavigation()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "MainActivity onDestroy")
    }
}
