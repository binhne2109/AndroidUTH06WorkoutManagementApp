package com.example

import com.example.data.export.DataExportManager
import com.example.data.model.WorkoutEntity
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class DataExportManagerTest {

    @Test
    fun testCsvExport_startsWithUtf8Bom() {
        val workouts = listOf(
            WorkoutEntity(
                id = "test_1",
                userId = "user_123",
                title = "Chạy bộ sáng",
                category = "Cardio",
                durationMinutes = 30,
                caloriesBurned = 250,
                completed = true,
                notes = "Thời tiết mát mẻ"
            )
        )

        val csv = DataExportManager.convertWorkoutsToCsv(workouts)

        // Kiểm tra Byte Order Mark (\uFEFF) ở đầu tệp
        assertTrue("Tệp CSV phải bắt đầu bằng UTF-8 BOM", csv.startsWith("\uFEFF"))
        assertTrue("Phải chứa tiêu đề cột", csv.contains("Mã bài tập,Ngày tập,Giờ tập,Tên bài tập"))
        assertTrue("Phải chứa tên bài tập tiếng Việt", csv.contains("Chạy bộ sáng"))
        assertTrue("Phải chứa thể loại", csv.contains("Cardio"))
        assertTrue("Phải chứa calo và thời lượng", csv.contains("30,250"))
        assertTrue("Phải hiển thị trạng thái hoàn thành", csv.contains("Đã hoàn thành"))
    }

    @Test
    fun testCsvExport_escapesSpecialCharacters() {
        val workouts = listOf(
            WorkoutEntity(
                id = "test_2",
                userId = "user_123",
                title = "Bài tập có dấu phẩy, và ngoặc \"kép\"",
                category = "Strength",
                durationMinutes = 45,
                caloriesBurned = 320,
                completed = false,
                notes = "Ghi chú dòng 1\nDòng 2"
            )
        )

        val csv = DataExportManager.convertWorkoutsToCsv(workouts)

        // Các trường có dấu phẩy và ngoặc kép phải được bao bọc trong nháy kép và escape ""
        assertTrue(csv.contains("\"Bài tập có dấu phẩy, và ngoặc \"\"kép\"\"\""))
        assertTrue(csv.contains("Chưa hoàn thành"))
    }

    @Test
    fun testGenerateDefaultFileName() {
        val csvName = DataExportManager.generateDefaultFileName("csv")
        assertTrue(csvName.startsWith("WorkoutHistory_"))
        assertTrue(csvName.endsWith(".csv"))

        val jsonName = DataExportManager.generateDefaultFileName("json")
        assertTrue(jsonName.startsWith("WorkoutHistory_"))
        assertTrue(jsonName.endsWith(".json"))
    }
}
