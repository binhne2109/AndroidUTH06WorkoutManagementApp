package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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