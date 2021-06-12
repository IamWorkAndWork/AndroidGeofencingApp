package app.practice.geofencing.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GeofenceDao {

    @Query("select * from geofence_table order by id asc")
    fun readGeofences(): Flow<MutableList<GeofenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addGeofence(geofenceEntity: GeofenceEntity)

    @Delete
    suspend fun removeGeofence(geofenceEntity: GeofenceEntity)

}