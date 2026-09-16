package com.example.data.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.WorkoutEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * [Thành viên 1: Quản trị Lưu trữ và Trích xuất dữ liệu]
 *
 * Module trích xuất và chuyển đổi dữ liệu lịch sử tập luyện:
 * 1. Chuyển đổi danh sách bài tập sang định dạng .csv (chuẩn UTF-8 BOM cho Excel tiếng Việt).
 * 2. Chuyển đổi danh sách bài tập sang định dạng .json có cấu trúc phân cấp chuẩn.
 * 3. Lưu tệp xuất vào thư mục Downloads của thiết bị qua MediaStore API (Scoped Storage).
 * 4. Tạo tệp và liên kết an toàn qua FileProvider để chia sẻ / mở trên các ứng dụng khác.
 */
object DataExportManager {
    private const val TAG = "DataExportManager"

    const val MIME_TYPE_CSV = "text/csv"
    const val MIME_TYPE_JSON = "application/json"

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val fileTimestampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    /**
     * Chuyển đổi danh sách bài tập sang nội dung định dạng CSV.
     * Bao gồm ký tự Byte Order Mark (\uFEFF) ở đầu để Microsoft Excel hiển thị tiếng Việt không bị lỗi font.
     */
    fun convertWorkoutsToCsv(workouts: List<WorkoutEntity>): String {
        val builder = StringBuilder()
        // Thêm UTF-8 BOM cho Excel tiếng Việt
        builder.append('\uFEFF')

        // Tiêu đề các cột
        val headers = listOf(
            "Mã bài tập",
            "Ngày tập",
            "Giờ tập",
            "Tên bài tập",
            "Thể loại",
            "Thời lượng (phút)",
            "Calo tiêu hao (kcal)",
            "Cường độ",
            "Trạng thái",
            "Ghi chú"
        )
        builder.append(headers.joinToString(separator = ",")).append("\r\n")

        for (item in workouts) {
            val dateStr = dateFormat.format(Date(item.dateMillis))
            val timeStr = timeFormat.format(Date(if (item.startTime > 0) item.startTime else item.dateMillis))
            val statusStr = if (item.completed) "Đã hoàn thành" else "Chưa hoàn thành"

            val row = listOf(
                escapeCsv(item.id),
                escapeCsv(dateStr),
                escapeCsv(timeStr),
                escapeCsv(item.title),
                escapeCsv(item.category),
                item.durationMinutes.toString(),
                item.caloriesBurned.toString(),
                escapeCsv(item.intensity),
                escapeCsv(statusStr),
                escapeCsv(item.notes)
            )
            builder.append(row.joinToString(separator = ",")).append("\r\n")
        }

        return builder.toString()
    }

    /**
     * Chuyển đổi danh sách bài tập sang chuỗi JSON có thụt lề chuẩn.
     */
    fun convertWorkoutsToJson(workouts: List<WorkoutEntity>, userId: String = ""): String {
        val root = JSONObject()
        val now = System.currentTimeMillis()

        root.put("exportVersion", "1.0")
        root.put("exportedAtMillis", now)
        root.put("exportedAt", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(now)))
        root.put("userId", userId)
        root.put("totalWorkouts", workouts.size)

        val array = JSONArray()
        for (item in workouts) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("userId", item.userId)
            obj.put("title", item.title)
            obj.put("category", item.category)
            obj.put("durationMinutes", item.durationMinutes)
            obj.put("caloriesBurned", item.caloriesBurned)
            obj.put("intensity", item.intensity)
            obj.put("dateMillis", item.dateMillis)
            obj.put("dateFormatted", dateFormat.format(Date(item.dateMillis)))
            obj.put("startTime", item.startTime)
            obj.put("completed", item.completed)
            obj.put("notes", item.notes)
            array.put(obj)
        }
        root.put("workouts", array)

        return root.toString(2)
    }

    /**
     * Tạo tên tệp mặc định có gắn nhãn thời gian thực.
     */
    fun generateDefaultFileName(extension: String): String {
        val timestamp = fileTimestampFormat.format(Date())
        return "WorkoutHistory_$timestamp.$extension"
    }

    /**
     * Lưu nội dung tệp vào thư mục Downloads của thiết bị sử dụng MediaStore API (Scoped Storage).
     * Hoạt động an toàn trên Android 10+ (API 29+) không đòi hỏi quyền nguy hiểm.
     */
    fun saveToDownloads(context: Context, fileName: String, content: String, mimeType: String): Result<String> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Sử dụng MediaStore Scoped Storage cho Android 10 trở lên
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/WorkoutManagement")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val targetUri: Uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return Result.failure(Exception("Không thể tạo mục ghi trong MediaStore"))

                resolver.openOutputStream(targetUri)?.use { outputStream ->
                    outputStream.write(content.toByteArray(Charsets.UTF_8))
                }

                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(targetUri, contentValues, null, null)

                Result.success("Đã lưu tệp vào thư mục Downloads/WorkoutManagement/$fileName")
            } else {
                // Fallback cho Android 9 trở xuống (API 26-28)
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetDir = File(downloadsDir, "WorkoutManagement")
                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }
                val targetFile = File(targetDir, fileName)
                FileOutputStream(targetFile).use { fos ->
                    fos.write(content.toByteArray(Charsets.UTF_8))
                }
                Result.success("Đã lưu tệp tại: ${targetFile.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi lưu tệp qua MediaStore", e)
            Result.failure(e)
        }
    }

    /**
     * Lưu tệp tạm vào thư mục cache riêng và tạo Intent chia sẻ qua FileProvider.
     * Cho phép mở tệp ngay trên Excel/Google Sheets hoặc gửi qua Zalo, Drive, Gmail.
     */
    fun createShareIntent(context: Context, fileName: String, content: String, mimeType: String): Intent {
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        val file = File(exportDir, fileName)
        FileOutputStream(file).use { fos ->
            fos.write(content.toByteArray(Charsets.UTF_8))
        }

        val authority = "${context.packageName}.fileprovider"
        val contentUri: Uri = FileProvider.getUriForFile(context, authority, file)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "Lịch sử tập luyện Workout Management App")
            putExtra(Intent.EXTRA_TEXT, "Đính kèm tệp lịch sử tập luyện: $fileName")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return Intent.createChooser(shareIntent, "Chia sẻ hoặc mở tệp lịch sử:")
    }

    /**
     * Thoát ký tự đặc biệt cho trường CSV theo RFC 4180.
     */
    private fun escapeCsv(value: String): String {
        var result = value
        if (result.contains("\"")) {
            result = result.replace("\"", "\"\"")
        }
        if (result.contains(",") || result.contains("\n") || result.contains("\r") || result.contains("\"")) {
            result = "\"$result\""
        }
        return result
    }
}
