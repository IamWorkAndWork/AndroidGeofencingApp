package app.practice.geofencing.di

import android.content.Context
import androidx.room.Room
import app.practice.geofencing.data.GeofenceDatabase
import app.practice.geofencing.util.Constants.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): GeofenceDatabase {
        return Room.databaseBuilder(
            context,
            GeofenceDatabase::class.java,
            DATABASE_NAME
        ).build()
    }

}