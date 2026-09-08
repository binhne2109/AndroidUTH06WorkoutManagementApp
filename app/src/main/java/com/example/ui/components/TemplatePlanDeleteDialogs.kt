package com.example.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.example.data.model.WorkoutPlan
import com.example.data.model.WorkoutTemplate

/** Xác nhận xóa một Mẫu bài tập. */
@Composable
fun DeleteTemplateDialog(
    template: WorkoutTemplate,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Xóa mẫu bài tập?", fontWeight = FontWeight.Bold) },
        text = { Text("Bạn có chắc chắn muốn xóa mẫu \"${template.name}\"?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Xóa", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

/** [Thành viên 6] Xác nhận xóa một Kế hoạch tập. */
@Composable
fun DeletePlanDialog(
    plan: WorkoutPlan,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Xóa kế hoạch tập?", fontWeight = FontWeight.Bold) },
        text = { Text("Bạn có chắc chắn muốn xóa kế hoạch \"${plan.name}\"?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Xóa", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}
