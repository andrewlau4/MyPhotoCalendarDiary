package com.andsoftapps

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier

@HiltAndroidApp
class CalendarDiaryApplication : Application() {
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationIOScope

@Module
@InstallIn(SingletonComponent::class)
object ApplicationHiltModule {

    @ApplicationIOScope
    @Provides
    fun applicationIOScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

}