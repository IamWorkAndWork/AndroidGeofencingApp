package app.practice.geofencing.presentation

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import app.practice.geofencing.broadcastreceiver.GeofenceBroadcastReceiver
import app.practice.geofencing.data.GeofenceEntity
import app.practice.geofencing.data.repository.DataStoreRepository
import app.practice.geofencing.data.repository.GeofenceRepository
import app.practice.geofencing.util.Permissions
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.sqrt

@HiltViewModel
class SharedViewModel @Inject constructor(
    application: Application,
    private val dataStoreRepository: DataStoreRepository,
    private val geofenceRepository: GeofenceRepository
) : AndroidViewModel(application) {

    val app = application
    private val geofencingClient = LocationServices.getGeofencingClient(app.applicationContext)

    var geoId = 0L
    var geoName = "Default"
    var geoCountryCode = ""
    var geoLocationName = "Search a City"
    var geoLatLng = LatLng(0.0, 0.0)
    var geoRedius = 500f
    var geoSnapshot: Bitmap? = null

    var geoCitySelected = false
    var geofenceReady = false
    var geofencePrepared = false

    fun resetSharedViewModel() {
        geoId = 0L
        geoName = "Default"
        geoCountryCode = ""
        geoLocationName = "Search A City"
        geoLatLng = LatLng(0.0, 0.0)
        geoRedius = 500f
        geoSnapshot = null

        geoCitySelected = false
        geofenceReady = false
        geofencePrepared = false
    }

    //DataStore
    val readFirstLaunch = dataStoreRepository.readFirstLaunch().asLiveData()

    fun saveFirstLaunch(firstLaunch: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveFirstLaunch(firstLaunch)
        }
    }

    //Database
    val readGeofences = geofenceRepository.readGeofences().asLiveData()

    fun addGeofence(geofenceEntity: GeofenceEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            geofenceRepository.removeGeofence(geofenceEntity = geofenceEntity)
        }
    }

    fun removeGeofence(geofenceEntity: GeofenceEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            geofenceRepository.removeGeofence(geofenceEntity = geofenceEntity)
        }
    }

    fun addGeofenceToDatabase(location: LatLng) {
        geoSnapshot?.let { snapshot ->
            val geofenceEntity = GeofenceEntity(
                geoId = geoId,
                name = geoName,
                location = geoLocationName,
                latitude = location.latitude,
                longitude = location.longitude,
                radius = geoRedius,
                snapshot = snapshot
            )
            addGeofence(geofenceEntity = geofenceEntity)
        }
    }

    private fun setPendingIntent(geoId: Int): PendingIntent {
        val intent = Intent(app, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            app,
            geoId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    @SuppressLint("MissingPermission")
    fun startGeofence(
        latitude: Double,
        longitude: Double
    ) {
        if (Permissions.hasBackgroundLocationPermission(app)) {
            val geofence = Geofence.Builder()
                .setRequestId(geoId.toString())
                .setCircularRegion(
                    latitude,
                    longitude,
                    geoRedius
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(
                    Geofence.GEOFENCE_TRANSITION_ENTER
                            or Geofence.GEOFENCE_TRANSITION_EXIT
                            or Geofence.GEOFENCE_TRANSITION_DWELL
                )
                .setLoiteringDelay(5000)
                .build()

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(
                    GeofencingRequest.INITIAL_TRIGGER_ENTER
                            or GeofencingRequest.INITIAL_TRIGGER_EXIT
                            or GeofencingRequest.INITIAL_TRIGGER_DWELL
                )
                .build()

            val pending = setPendingIntent(geoId = geoId.toInt())
            geofencingClient.addGeofences(geofencingRequest, pending).run {
                addOnSuccessListener {
                    Log.d("Geofence", "Successfully added.")
                }
                addOnFailureListener {
                    Log.e("Geofence", it.message.toString())
                }
            }
        } else {
            Log.d("Geofence", "Permission not granted.")
        }
    }

    suspend fun stopGeofence(geoIds: List<Long>): Boolean {
        return if (Permissions.hasBackgroundLocationPermission(app)) {
            val result = CompletableDeferred<Boolean>()
            val pending = setPendingIntent(geoId = geoIds.first().toInt())
            geofencingClient.removeGeofences(pending)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        result.complete(true)
                    } else {
                        result.complete(false)
                    }
                }
            result.await()
        } else {
            false
        }
    }

    fun getBounds(center: LatLng, redius: Float): LatLngBounds {
        val distanceFromCenterToCornet = redius * sqrt(2.0)
        val southWestCorner = SphericalUtil.computeOffset(center, distanceFromCenterToCornet, 225.0)
        val northEastCornet = SphericalUtil.computeOffset(center, distanceFromCenterToCornet, 45.0)
        return LatLngBounds(southWestCorner, northEastCornet)
    }

    fun checkDeviceLocationSettings(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.isLocationEnabled
        } else {
            val mode: Int = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            )
            mode != Settings.Secure.LOCATION_MODE_OFF
        }
    }

}