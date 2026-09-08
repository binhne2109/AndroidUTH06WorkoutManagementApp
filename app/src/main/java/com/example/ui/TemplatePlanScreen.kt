package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*

/**
 * Màn hình "Mẫu bài tập & Kế hoạch tập".
 *
 * Gồm 2 tab:
 *  - Mẫu bài tập: lưu cấu hình tĩnh (Tên, Loại, Địa điểm, Thời lượng) để tái sử dụng,
 *    hỗ trợ Auto-fill khi tạo bài tập mới.
 *  - Kế hoạch tập: một chuỗi bài tập liên tiếp có tổ chức, có thể "Bắt đầu" để tạo
 *    hàng loạt bài tập thật vào nhật ký theo đúng thứ tự.
 *
 * [workoutViewModel] chỉ được dùng để gọi `saveWorkout(...)` sẵn có — màn hình này
 * không đọc/ghi trực tiếp vào state của WorkoutViewModel, nên không ảnh hưởng tới
 * tính năng nhật ký bài tập của các thành viên khác.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatePlanScreen(
    templateViewModel: TemplatePlanViewModel,
    workoutViewModel: WorkoutViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by templateViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            templateViewModel.clearSnackbarMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Mẫu & Kế hoạch tập", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (selectedTab == 0) templateViewModel.openAddTemplateDialog()
                    else templateViewModel.openCreatePlanEditor()
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(if (selectedTab == 0) "Thêm mẫu" else "Thêm kế hoạch", fontWeight = FontWeight.Bold) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Mẫu bài tập") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Kế hoạch tập") }
                )
            }

            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                if (selectedTab == 0) {
                    if (uiState.templates.isEmpty()) {
                        EmptyHint(text = "Chưa có mẫu bài tập nào. Bấm \"Thêm mẫu\" để tạo mẫu đầu tiên.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(uiState.templates, key = { it.id }) { template ->
                                TemplateCard(
                                    template = template,
                                    onUseClick = templateViewModel::selectTemplateToApply,
                                    onEditClick = templateViewModel::openEditTemplateDialog,
                                    onDeleteClick = templateViewModel::requestDeleteTemplate
                                )
                            }
                        }
                    }
                } else {
                    if (uiState.plans.isEmpty()) {
                        EmptyHint(text = "Chưa có kế hoạch tập nào. Bấm \"Thêm kế hoạch\" để tạo chuỗi bài tập đầu tiên.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(uiState.plans, key = { it.id }) { plan ->
                                PlanCard(
                                    plan = plan,
                                    onStartClick = { selected ->
                                        templateViewModel.startPlan(selected, workoutViewModel::saveWorkout)
                                    },
                                    onEditClick = templateViewModel::openEditPlanEditor,
                                    onDeleteClick = templateViewModel::requestDeletePlan
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Tạo/sửa mẫu ---
    if (uiState.isTemplateSheetOpen) {
        TemplateAddEditSheet(
            editingTemplate = uiState.editingTemplate,
            onDismiss = templateViewModel::closeTemplateDialog,
            onSave = templateViewModel::saveTemplate
        )
    }

    // --- Auto-fill: chọn mẫu -> mở biểu mẫu tạo bài tập đã điền sẵn ---
    uiState.templateToApply?.let { template ->
        ApplyTemplateSheet(
            template = template,
            onDismiss = templateViewModel::clearTemplateToApply,
            onConfirm = { title, category, duration, calories, intensity, notes ->
                workoutViewModel.saveWorkout(title, category, duration, calories, intensity, notes)
                templateViewModel.clearTemplateToApply()
            }
        )
    }

    uiState.deletingTemplate?.let { target ->
        DeleteTemplateDialog(
            template = target,
            onConfirm = templateViewModel::confirmDeleteTemplate,
            onDismiss = templateViewModel::cancelDeleteTemplate
        )
    }

    // --- Tạo/sửa kế hoạch ---
    if (uiState.isPlanEditorOpen) {
        PlanEditorSheet(
            editingPlan = uiState.editingPlan,
            draftItems = uiState.planDraftItems,
            availableTemplates = uiState.templates,
            onDismiss = templateViewModel::closePlanEditor,
            onAddItem = templateViewModel::addDraftItem,
            onAddFromTemplate = templateViewModel::addTemplateToDraft,
            onRemoveItem = templateViewModel::removeDraftItem,
            onMoveItem = templateViewModel::moveDraftItem,
            onSave = templateViewModel::savePlan
        )
    }

    uiState.deletingPlan?.let { target ->
        DeletePlanDialog(
            plan = target,
            onConfirm = templateViewModel::confirmDeletePlan,
            onDismiss = templateViewModel::cancelDeletePlan
        )
    }
}

@Composable
private fun EmptyHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
