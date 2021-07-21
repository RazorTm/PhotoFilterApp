package com.razortm.imageeditor.dependency_injection

import com.razortm.imageeditor.repositories.EditImageRepository
import com.razortm.imageeditor.repositories.EditImageRepositoryImpl
import com.razortm.imageeditor.repositories.SavedImagesRepository
import com.razortm.imageeditor.repositories.SavedImagesRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    factory<EditImageRepository> { EditImageRepositoryImpl(androidContext()) }
    factory<SavedImagesRepository> { SavedImagesRepositoryImpl(androidContext())}
}