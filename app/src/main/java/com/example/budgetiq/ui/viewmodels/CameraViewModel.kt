package com.example.budgetiq.ui.viewmodels

import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.Manifest
import androidx.lifecycle.SavedStateHandle

@HiltViewModel
class CameraViewModel @Inject constructor(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission

    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri

    init {
        checkPermissions()
    }

    fun checkPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.CAMERA
        )
        _hasPermission.value = cameraPermission == PackageManager.PERMISSION_GRANTED
    }

    fun setPhotoUri(uri: Uri?) {
        viewModelScope.launch {
            _photoUri.value = uri
        }
    }

    fun clearPhoto() {
        viewModelScope.launch {
            _photoUri.value = null
        }
    }
} 