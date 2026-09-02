package com.micca.taskmanager

import android.app.Application
import com.micca.taskmanager.data.di.RepositoryProviderImpl
import com.micca.taskmanager.domain.di.AppContainer

class CustomApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        AppContainer.setup(
            repositoryProvider = RepositoryProviderImpl(this)
        )
    }
}
