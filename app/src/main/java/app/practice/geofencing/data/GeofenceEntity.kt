package app.practice.geofencing.data

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.practice.geofencing.util.Constants.DATABASE_TABLE_NAME
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Entity(tableName = DATABASE_TABLE_NAME)
@Parcelize
class GeofenceEntity(
    val geoId: Long,
    val name: String,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Float,
    val snapshot: Bitmap
) : Parcelable {
    @IgnoredOnParcel
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}