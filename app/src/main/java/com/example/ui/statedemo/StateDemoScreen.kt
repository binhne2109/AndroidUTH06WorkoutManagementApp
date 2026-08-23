package com.example.ui.statedemo

import android.util.Log
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private const val TAG = "STATE_DEMO"

enum class StateMechanism(val displayName: String) {
    REMEMBER("remember"),
    REMEMBER_SAVEABLE("rememberSaveable"),
    VIEW_MODEL("ViewModel"),
    SAVED_STATE_HANDLE("SavedStateHandle"),
    DATA_STORE("DataStore")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StateDemoScreen(
    viewModel: StateDemoViewModel,
    onNavigateToSecondScreen: () -> Unit,
    onBack: () -> Unit
) {
    // ==========================================
    // Lifecycle log ghi nhận sự kiện Compose/Dispose
    // ==========================================
    DisposableEffect(Unit) {
        Log.i(TAG, "StateDemoScreen COMPOSED")
        onDispose {
            Log.w(TAG, "StateDemoScreen DISPOSED (composition huỷ → remember mất)")
        }
    }

    // ==========================================
    // QUAN TRỌNG: Khai báo remember & rememberSaveable ở TOP-LEVEL composable
    // Tuyệt đối không đặt trong if/when để tránh bị huỷ khi chuyển đổi qua lại giữa các chip
    // ==========================================
    var rName by remember { mutableStateOf("") }
    var rCounter by remember { mutableIntStateOf(0) }
    var rSelected by remember { mutableStateOf(false) }

    var rsName by rememberSaveable { mutableStateOf("") }
    var rsCounter by rememberSaveable { mutableIntStateOf(0) }
    var rsSelected by rememberSaveable { mutableStateOf(false) }

    // State của ViewModel thuần
    val vmName by viewModel.vmName.collectAsStateWithLifecycle()
    val vmCounter by viewModel.vmCounter.collectAsStateWithLifecycle()
    val vmSelected by viewModel.vmSelected.collectAsStateWithLifecycle()

    // State của SavedStateHandle
    val sshName by viewModel.sshName.collectAsStateWithLifecycle()
    val sshCounter by viewModel.sshCounter.collectAsStateWithLifecycle()
    val sshSelected by viewModel.sshSelected.collectAsStateWithLifecycle()

    // State của DataStore
    val dsState by viewModel.dsState.collectAsStateWithLifecycle()

    // Cơ chế đang chọn để kiểm tra (lưu bằng rememberSaveable để giữ chip khi xoay)
    var selectedMechanism by rememberSaveable { mutableStateOf(StateMechanism.REMEMBER) }

    val scrollState = rememberScrollState()
    val chipScrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Demo State Vòng Đời") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Cơ chế đang kiểm tra & Process ID (PID)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Cơ chế đang kiểm tra: ${selectedMechanism.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "PID: ${android.os.Process.myPid()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Thanh chọn 5 cơ chế lưu state
            Text(
                text = "Chọn cơ chế cần kiểm chứng:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(chipScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StateMechanism.values().forEach { mechanism ->
                    FilterChip(
                        selected = selectedMechanism == mechanism,
                        onClick = { selectedMechanism = mechanism },
                        label = { Text(mechanism.displayName) }
                    )
                }
            }

            HorizontalDivider()

            // 3 Control tương tác với state của cơ chế đang chọn
            val currentName: String
            val currentCounter: Int
            val currentSelected: Boolean

            when (selectedMechanism) {
                StateMechanism.REMEMBER -> {
                    currentName = rName
                    currentCounter = rCounter
                    currentSelected = rSelected
                }
                StateMechanism.REMEMBER_SAVEABLE -> {
                    currentName = rsName
                    currentCounter = rsCounter
                    currentSelected = rsSelected
                }
                StateMechanism.VIEW_MODEL -> {
                    currentName = vmName
                    currentCounter = vmCounter
                    currentSelected = vmSelected
                }
                StateMechanism.SAVED_STATE_HANDLE -> {
                    currentName = sshName
                    currentCounter = sshCounter
                    currentSelected = sshSelected
                }
                StateMechanism.DATA_STORE -> {
                    currentName = dsState.name
                    currentCounter = dsState.counter
                    currentSelected = dsState.isSelected
                }
            }

            Text(
                text = "Dữ liệu thử nghiệm (${selectedMechanism.displayName}):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            // 1. Ô nhập tên
            OutlinedTextField(
                value = currentName,
                onValueChange = { newValue ->
                    when (selectedMechanism) {
                        StateMechanism.REMEMBER -> rName = newValue
                        StateMechanism.REMEMBER_SAVEABLE -> rsName = newValue
                        StateMechanism.VIEW_MODEL -> viewModel.setVmName(newValue)
                        StateMechanism.SAVED_STATE_HANDLE -> viewModel.setSshName(newValue)
                        StateMechanism.DATA_STORE -> viewModel.saveDsName(newValue)
                    }
                },
                label = { Text("Nhập dữ liệu kiểm thử (Tên)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // 2. Nút Tăng biến đếm (BẮT BUỘC ĐÚNG TÊN "Tăng biến đếm")
            Button(
                onClick = {
                    when (selectedMechanism) {
                        StateMechanism.REMEMBER -> rCounter++
                        StateMechanism.REMEMBER_SAVEABLE -> rsCounter++
                        StateMechanism.VIEW_MODEL -> viewModel.incrementVmCounter()
                        StateMechanism.SAVED_STATE_HANDLE -> viewModel.incrementSshCounter()
                        StateMechanism.DATA_STORE -> viewModel.incrementDsCounter()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tăng biến đếm ($currentCounter)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 3. Checkbox lựa chọn
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = currentSelected,
                        onCheckedChange = { checked ->
                            when (selectedMechanism) {
                                StateMechanism.REMEMBER -> rSelected = checked
                                StateMechanism.REMEMBER_SAVEABLE -> rsSelected = checked
                                StateMechanism.VIEW_MODEL -> viewModel.setVmSelected(checked)
                                StateMechanism.SAVED_STATE_HANDLE -> viewModel.setSshSelected(checked)
                                StateMechanism.DATA_STORE -> viewModel.saveDsSelected(checked)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Checkbox trạng thái (${if (currentSelected) "Đã chọn" else "Chưa chọn"})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Nút Chuyển sang màn hình phụ (BẮT BUỘC ĐÚNG TÊN "Chuyển sang màn hình phụ")
            OutlinedButton(
                onClick = onNavigateToSecondScreen,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Chuyển sang màn hình phụ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}
