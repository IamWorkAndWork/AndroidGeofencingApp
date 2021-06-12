package app.practice.geofencing.di

import android.content.Context
import app.practice.geofencing.data.GeofenceDatabase
import app.practice.geofencing.data.repository.DataStoreRepository
import app.practice.geofencing.data.repository.DataStoreRepositoryImpl
import app.practice.geofencing.data.repository.GeofenceRepository
import app.practice.geofencing.data.repository.GeofenceRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReositoryModule {

    @Singleton
    @Provides
    fun provideDataStoreRepository(@ApplicationContext context: Context): DataStoreRepository {
        return DataStoreRepositoryImpl(context = context)
    }

    @Singleton
    @Provides
    fun provideGeofenceRepository(geofenceDatabase: GeofenceDatabase): GeofenceRepository {
        return GeofenceRepositoryImpl(
            geofenceDao = geofenceDatabase.geofenceDao()
        )
    }

}