package com.razortm.imageeditor.utilities

import android.app.Application
import com.razortm.imageeditor.dependency_injection.repositoryModule
import com.razortm.imageeditor.dependency_injection.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class AppConfig : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AppConfig)
            modules(listOf(repositoryModule, viewModelModule))
        }
    }
}