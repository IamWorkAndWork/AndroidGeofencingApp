package app.practice.geofencing.data.repository

import app.practice.geofencing.data.GeofenceDao
import app.practice.geofencing.data.GeofenceEntity
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface GeofenceRepository {
    suspend fun addGeofence(geofenceEntity: GeofenceEntity)
    suspend fun removeGeofence(geofenceEntity: GeofenceEntity)
    fun readGeofences(): Flow<List<GeofenceEntity>>
}

@ViewModelScoped
class GeofenceRepositoryImpl @Inject constructor(
    private val geofenceDao: GeofenceDao
) : GeofenceRepository {

    override suspend fun addGeofence(geofenceEntity: GeofenceEntity) {
        geofenceDao.addGeofence(geofenceEntity = geofenceEntity)
    }

    override suspend fun removeGeofence(geofenceEntity: GeofenceEntity) {
        geofenceDao.removeGeofence(geofenceEntity = geofenceEntity)
    }

    override fun readGeofences(): Flow<List<GeofenceEntity>> {
        return geofenceDao.readGeofences()
    }

}