package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbarMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Android_UTH_06", fontWeight = FontWeight.ExtraBold) }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddWorkoutDialog() },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Thêm bài tập", fontWeight = FontWeight.Bold) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    WorkoutStatsHeader(
                        totalWorkouts = uiState.totalWorkouts,
                        totalMinutes = uiState.totalDurationMinutes,
                        totalCalories = uiState.totalCaloriesBurned
                    )
                }

                item {
                    WorkoutFilterBar(
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChange = viewModel::onSearchQueryChange,
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = viewModel::onCategorySelected
                    )
                }

                items(uiState.filteredWorkouts, key = { it.id }) { workout ->
                    WorkoutCard(
                        workout = workout,
                        onEditClick = viewModel::openEditWorkoutDialog,
                        onDeleteClick = viewModel::requestDeleteWorkout
                    )
                }
            }
        }
    }

    if (uiState.isAddEditSheetOpen) {
        WorkoutAddEditSheet(
            editingWorkout = uiState.editingWorkout,
            onDismiss = viewModel::closeAddEditDialog,
            onSave = viewModel::saveWorkout
        )
    }

    uiState.deletingWorkout?.let { target ->
        DeleteConfirmationDialog(
            workout = target,
            onConfirm = viewModel::confirmDeleteWorkout,
            onDismiss = viewModel::cancelDeleteWorkout
        )
    }
}