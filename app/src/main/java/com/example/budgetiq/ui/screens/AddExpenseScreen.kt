package com.example.budgetiq.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.budgetiq.ui.viewmodels.AddExpenseViewModel
import com.example.budgetiq.ui.viewmodels.CameraViewModel
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: AddExpenseViewModel = hiltViewModel(),
    cameraViewModel: CameraViewModel = hiltViewModel()
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Long?>(null) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var hasReceipt by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val hasPermission = cameraViewModel.hasPermission.collectAsStateWithLifecycle()

    // Camera permission launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraViewModel.checkPermissions()
        }
    }

    // Camera launcher
    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri = cameraViewModel.photoUri.value
        }
    }

    LaunchedEffect(viewModel.saveState) {
        if (viewModel.saveState is AddExpenseViewModel.SaveState.Success) {
            onNavigateToHome()
        }
    }

    // Handle save button click
    fun handleSave() {
        val amountValue = amount.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) {
            // Show error
            return
        }
        if (selectedCategory == null) {
            // Show error
            return
        }
        viewModel.saveExpense(
            amount = amountValue,
            description = description,
            categoryId = selectedCategory!!,
            date = LocalDate.now(),
            startTime = LocalTime.now(),
            endTime = LocalTime.now(),
            photoUri = photoUri?.toString()
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { handleSave() }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = amount,
                onValueChange = { 
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*\$"))) {
                        amount = it
                    }
                },
                label = { Text("Amount") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            if (viewModel.categories.isEmpty()) {
                Text(
                    text = "No categories available. Please create some categories first.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = it }
                ) {
                    OutlinedTextField(
                        value = viewModel.categories.find { it.id == selectedCategory }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .padding(bottom = 16.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        viewModel.categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category.id
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Receipt option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Add Receipt")
                Switch(
                    checked = hasReceipt,
                    onCheckedChange = { hasReceipt = it }
                )
            }

            if (hasReceipt) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    ElevatedButton(
                        onClick = {
                            if (!hasPermission.value) {
                                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                            } else {
                                val photoFile = File(
                                    context.getExternalFilesDir(null),
                                    "receipt_${System.currentTimeMillis()}.jpg"
                                )
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    photoFile
                                )
                                cameraViewModel.setPhotoUri(uri)
                                takePicture.launch(uri)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Take Photo",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Take Photo")
                    }

                    photoUri?.let { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "Receipt Photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(top = 8.dp)
                        )
                        TextButton(
                            onClick = {
                                photoUri = null
                                cameraViewModel.clearPhoto()
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove Photo",
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("Remove")
                        }
                    }
                }
            }

            Button(
                onClick = {
                    selectedCategory?.let { categoryId ->
                        viewModel.saveExpense(
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            description = description,
                            categoryId = categoryId,
                            date = LocalDate.now(),
                            startTime = LocalTime.now(),
                            endTime = LocalTime.now()
                        )
                    }
                },
                enabled = amount.isNotEmpty() && selectedCategory != null && description.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Expense")
            }

            if (viewModel.saveState is AddExpenseViewModel.SaveState.Error) {
                Text(
                    text = (viewModel.saveState as AddExpenseViewModel.SaveState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
} 