package com.razortm.imageeditor.viewmodels

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.razortm.imageeditor.data.ImageFilter
import com.razortm.imageeditor.repositories.EditImageRepository
import com.razortm.imageeditor.utilities.Coroutines

class EditImageViewModel(private val editImageRepository: EditImageRepository): ViewModel() {


    //region:: Prepare Image Preview
    private val imagePreviewDataState = MutableLiveData<ImagePreviewDataState>()
    val imagePreviewUiState: LiveData<ImagePreviewDataState> get() = imagePreviewDataState

    fun prepareImagePreview(imageUri: Uri) {
        Coroutines.io { runCatching {
            emitImagePreviewUiState(isLoading = true)
            editImageRepository.prepareImagePreview(imageUri)
        }.onSuccess { bitmap ->
            if(bitmap != null) {
                emitImagePreviewUiState(bitmap = bitmap)
            }else{
                emitImagePreviewUiState(error = "Unable to prepare image preview")
            }
        }.onFailure {
            emitImagePreviewUiState(error = it.message.toString())
        }

        }
    }

    private fun emitImagePreviewUiState(
        isLoading: Boolean = false,
        bitmap: Bitmap? = null,
        error: String? = null
    ) {
        val dataState = ImagePreviewDataState(isLoading, bitmap, error)
        imagePreviewDataState.postValue(dataState)
    }

    data class ImagePreviewDataState(
        val isLoading: Boolean,
        val bitmap: Bitmap?,
        val error: String?
    )
//endregion

    //region:: Load image filters
    private val imageFiltersDataState = MutableLiveData<ImageFiltersDataState>()
    val imageFiltersUIState: LiveData<ImageFiltersDataState> get() = imageFiltersDataState

    fun loadImageFilters(originalImage: Bitmap) {
        Coroutines.io {
            runCatching {
                emitImageFiltersUIState(isLoading = true)
                editImageRepository.getImageFilters(getPreviewImage(originalImage))
            }.onSuccess { imageFilters ->
                emitImageFiltersUIState(imageFilters = imageFilters)

            }.onFailure {
                emitImageFiltersUIState(error = it.message.toString())
            }
        }
    }

    private fun getPreviewImage(originalImage: Bitmap): Bitmap {
        return runCatching {
            val previewWidth = 150
            val previewHeight = originalImage.height * previewWidth / originalImage.width
            Bitmap.createScaledBitmap(originalImage,previewWidth,previewHeight, false)
        }.getOrDefault(originalImage)
    }

    private fun emitImageFiltersUIState(
        isLoading: Boolean = false,
        imageFilters: List<ImageFilter>? = null,
        error: String? = null
    ) {
        val dataState = ImageFiltersDataState(isLoading, imageFilters, error)
        imageFiltersDataState.postValue(dataState)
    }

    data class ImageFiltersDataState(
        val isLoading: Boolean,
        val imageFilters: List<ImageFilter>?,
        val error: String?
    )
    //endregion

    //region:: Save Filtered Image
    private val saveFilteredImageDataState = MutableLiveData<SaveFilteredImageDataState>()
    val saveFilteredImageUIState: LiveData<SaveFilteredImageDataState> get() = saveFilteredImageDataState

    fun saveFilteredImage(filteredBitmap: Bitmap) {
        Coroutines.io {
            runCatching {
                emitSaveFilteredImageUIState(isLoading = true)
                editImageRepository.saveFilteredImage(filteredBitmap)
            }.onSuccess { savedImageUri ->
                emitSaveFilteredImageUIState(uri = savedImageUri)
            }.onFailure {
                emitSaveFilteredImageUIState(error = it.message.toString())
            }
        }
    }

    private fun emitSaveFilteredImageUIState(
        isLoading: Boolean = false,
        uri: Uri? = null,
        error: String? = null
    ) {
        val dataState = SaveFilteredImageDataState(isLoading, uri, error)
        saveFilteredImageDataState.postValue(dataState)
    }

    data class SaveFilteredImageDataState(
        val isLoading: Boolean,
        val uri: Uri?,
        val error: String?
    )
    //endregion

}