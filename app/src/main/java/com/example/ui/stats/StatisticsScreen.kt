package com.example.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.StatisticsViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showGoalDialog by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Thống kê & Biểu đồ tiến độ",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // 1. Thẻ Tổng quan hoạt động
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Tổng quan hoạt động", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(label = "Calo", value = "${uiState.totalCalories} kcal")
                        StatItem(label = "Thời gian", value = "${uiState.totalDurationMinutes} phút")
                        StatItem(label = "Số buổi", value = "${uiState.completedWorkoutsCount} buổi")
                    }
                }
            }

            // 2. Thẻ Thống kê theo nhóm bài tập (Category)
            if (uiState.categoryStats.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Thống kê theo nhóm bài tập", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        uiState.categoryStats.forEach { stat ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = stat.categoryName, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                Text(
                                    text = "${stat.totalMinutes} phút | ${stat.totalCalories} kcal",
                                    fontSize = 13.sp,
                                    color = Color.DarkGray
                                )
                            }
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            // 3. Thẻ Thiết lập & Theo dõi Mục tiêu (Goals)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Mục tiêu tuần này", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        IconButton(onClick = { showGoalDialog = true }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Chỉnh sửa mục tiêu")
                        }
                    }

                    Text(text = "Số buổi tập: ${uiState.completedWorkoutsCount} / ${uiState.targetWorkouts}", fontSize = 13.sp)
                    LinearProgressIndicator(
                        progress = uiState.workoutProgress,
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Thời lượng: ${uiState.totalDurationMinutes} / ${uiState.targetDurationMinutes} phút", fontSize = 13.sp)
                    LinearProgressIndicator(
                        progress = uiState.durationProgress,
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )
                }
            }

            // 4. Biểu đồ Vico Chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Thời lượng tập luyện (phút)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Chart(
                        chart = columnChart(),
                        chartModelProducer = viewModel.chartEntryModelProducer,
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis(),
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                }
            }
        }
    }

    // Dialog cho phép người dùng đặt mục tiêu mới
    if (showGoalDialog) {
        GoalSettingDialog(
            currentTargetWorkouts = uiState.targetWorkouts,
            currentTargetDuration = uiState.targetDurationMinutes,
            onDismiss = { showGoalDialog = false },
            onSave = { newWorkouts, newDuration ->
                viewModel.updateGoals(newWorkouts, newDuration)
                showGoalDialog = false
            }
        )
    }
}

@Composable
private fun GoalSettingDialog(
    currentTargetWorkouts: Int,
    currentTargetDuration: Int,
    onDismiss: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    var workoutsText by remember { mutableStateOf(currentTargetWorkouts.toString()) }
    var durationText by remember { mutableStateOf(currentTargetDuration.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Thiết lập mục tiêu") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = workoutsText,
                    onValueChange = { workoutsText = it },
                    label = { Text("Số buổi tập / tuần") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it },
                    label = { Text("Tổng thời lượng (phút)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newWorkouts = workoutsText.toIntOrNull() ?: currentTargetWorkouts
                    val newDuration = durationText.toIntOrNull() ?: currentTargetDuration
                    onSave(newWorkouts, newDuration)
                }
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}