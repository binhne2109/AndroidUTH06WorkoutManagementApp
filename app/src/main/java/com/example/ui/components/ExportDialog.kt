package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.export.DataExportManager
import com.example.data.model.WorkoutEntity

enum class ExportFormat(val displayName: String, val extension: String, val mimeType: String) {
    CSV("Tệp CSV (Tương thích Microsoft Excel, Google Sheets)", "csv", DataExportManager.MIME_TYPE_CSV),
    JSON("Tệp JSON (Dành cho sao lưu, tích hợp hệ thống)", "json", DataExportManager.MIME_TYPE_JSON)
}

/**
 * [Thành viên 1: Quản trị Lưu trữ và Trích xuất dữ liệu]
 *
 * Hộp thoại cho phép người dùng chọn định dạng xuất dữ liệu (.csv hoặc .json)
 * và phương thức lưu trữ (Lưu vào thư mục Downloads qua MediaStore hoặc Chia sẻ qua FileProvider).
 */
@Composable
fun ExportDialog(
    workouts: List<WorkoutEntity>,
    userId: String,
    onDismiss: () -> Unit,
    onExportSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedFormat by remember { mutableStateOf(ExportFormat.CSV) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        icon = {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Xuất Lịch Sử Tập Luyện",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Tổng số bản ghi sẵn sàng xuất: ${workouts.size} bài tập.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                Text(
                    text = "Chọn định dạng tệp:",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall
                )

                ExportFormat.values().forEach { format ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedFormat = format }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedFormat == format),
                            onClick = { selectedFormat = format }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = format.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selectedFormat == format) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (selectedFormat == ExportFormat.CSV) {
                            "• Định dạng CSV có sẵn mã BOM tiếng Việt để hiển thị chuẩn xác trên Excel."
                        } else {
                            "• Định dạng JSON lưu trữ toàn bộ trường thông tin chi tiết và metadata xuất."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Nút 1: Lưu vào máy qua MediaStore
                Button(
                    onClick = {
                        val fileName = DataExportManager.generateDefaultFileName(selectedFormat.extension)
                        val content = if (selectedFormat == ExportFormat.CSV) {
                            DataExportManager.convertWorkoutsToCsv(workouts)
                        } else {
                            DataExportManager.convertWorkoutsToJson(workouts, userId)
                        }

                        val result = DataExportManager.saveToDownloads(
                            context = context,
                            fileName = fileName,
                            content = content,
                            mimeType = selectedFormat.mimeType
                        )

                        result.onSuccess { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            onExportSuccess(msg)
                            onDismiss()
                        }.onFailure { err ->
                            val errMsg = "Lỗi khi lưu tệp: ${err.message}"
                            Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lưu vào máy (Thư mục Download)")
                }

                // Nút 2: Chia sẻ qua FileProvider
                OutlinedButton(
                    onClick = {
                        val fileName = DataExportManager.generateDefaultFileName(selectedFormat.extension)
                        val content = if (selectedFormat == ExportFormat.CSV) {
                            DataExportManager.convertWorkoutsToCsv(workouts)
                        } else {
                            DataExportManager.convertWorkoutsToJson(workouts, userId)
                        }

                        try {
                            val intent = DataExportManager.createShareIntent(
                                context = context,
                                fileName = fileName,
                                content = content,
                                mimeType = selectedFormat.mimeType
                            )
                            context.startActivity(intent)
                            onExportSuccess("Đã mở hộp thoại chia sẻ tệp $fileName")
                            onDismiss()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Không thể chia sẻ tệp: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chia sẻ / Mở bằng ứng dụng khác")
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Đóng")
                }
            }
        },
        dismissButton = null
    )
}
