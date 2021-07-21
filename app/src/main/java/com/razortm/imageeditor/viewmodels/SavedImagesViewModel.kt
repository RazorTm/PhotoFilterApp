package com.razortm.imageeditor.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.razortm.imageeditor.repositories.SavedImagesRepository
import com.razortm.imageeditor.utilities.Coroutines
import java.io.File

class SavedImagesViewModel(private val savedImagesRepository: SavedImagesRepository): ViewModel() {

    private val savedImagesDataState = MutableLiveData<SavedImagesDataState>()
    val savedImagesUIState: LiveData<SavedImagesDataState> get() = savedImagesDataState

    fun loadSavedImages() {
        Coroutines.io {
            runCatching {
                emitSavedImagesUIState(isLoading = true)
                savedImagesRepository.loadSavedImages()
            }.onSuccess { savedImages->
                if(savedImages.isNullOrEmpty()) {
                    emitSavedImagesUIState(error = "No Image Found")
                }else{
                    emitSavedImagesUIState(savedImages = savedImages)
                }
            }.onFailure {
                emitSavedImagesUIState(error = it.message.toString())
            }
        }
    }

    private fun emitSavedImagesUIState(
        isLoading: Boolean = false,
        savedImages: List<Pair<File, Bitmap>>? = null,
        error: String? = null
    ) {
        val dataState = SavedImagesDataState(isLoading, savedImages, error)
        savedImagesDataState.postValue(dataState)
    }

    data class SavedImagesDataState(
       val isLoading: Boolean,
       val savedImages: List<Pair<File, Bitmap>>?,
       val error: String?
    )
}