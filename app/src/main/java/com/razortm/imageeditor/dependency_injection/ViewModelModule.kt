package com.razortm.imageeditor.dependency_injection

import com.razortm.imageeditor.viewmodels.EditImageViewModel
import com.razortm.imageeditor.viewmodels.SavedImagesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { EditImageViewModel(editImageRepository = get()) }
    viewModel { SavedImagesViewModel(savedImagesRepository = get()) }
}