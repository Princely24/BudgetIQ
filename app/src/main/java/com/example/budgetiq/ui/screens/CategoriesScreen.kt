package com.example.budgetiq.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.budgetiq.ui.viewmodels.CategoriesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onNavigateBack: () -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<CategoryUiState?>(null) }
    var newCategoryName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadCategories()
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAddDialog = false
                newCategoryName = ""
            },
            title = { Text("Add Category") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            viewModel.addCategory(newCategoryName)
                            showAddDialog = false
                            newCategoryName = ""
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showAddDialog = false
                        newCategoryName = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditDialog && selectedCategory != null) {
        AlertDialog(
            onDismissRequest = { 
                showEditDialog = false
                selectedCategory = null
                newCategoryName = ""
            },
            title = { Text("Edit Category") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            selectedCategory?.let { category ->
                                viewModel.updateCategory(category.id, newCategoryName)
                            }
                            showEditDialog = false
                            selectedCategory = null
                            newCategoryName = ""
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showEditDialog = false
                        selectedCategory = null
                        newCategoryName = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteDialog && selectedCategory != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                selectedCategory = null
            },
            title = { Text("Delete Category") },
            text = { Text("Are you sure you want to delete this category? All expenses in this category will also be deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedCategory?.let { category ->
                            viewModel.deleteCategory(category.id)
                        }
                        showDeleteDialog = false
                        selectedCategory = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteDialog = false
                        selectedCategory = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = uiState !is CategoriesViewModel.UiState.Loading
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Category")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is CategoriesViewModel.UiState.Success -> {
                if (state.categories.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No categories yet")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.categories) { category ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Row {
                                        IconButton(
                                            onClick = {
                                                selectedCategory = category
                                                newCategoryName = category.name
                                                showEditDialog = true
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit Category"
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                selectedCategory = category
                                                showDeleteDialog = true
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete Category"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is CategoriesViewModel.UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            CategoriesViewModel.UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

data class CategoryUiState(
    val id: Long,
    val name: String
) 