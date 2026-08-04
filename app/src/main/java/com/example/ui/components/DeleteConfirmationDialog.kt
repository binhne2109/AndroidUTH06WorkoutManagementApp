package com.example.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.example.data.model.WorkoutEntity

@Composable
fun DeleteConfirmationDialog(
    workout: WorkoutEntity,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Xóa bài tập?", fontWeight = FontWeight.Bold) },
        text = { Text("Bạn có chắc chắn muốn xóa bài tập \"${workout.title}\"?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Xóa", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}