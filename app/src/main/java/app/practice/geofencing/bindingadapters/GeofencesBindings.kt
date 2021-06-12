package app.practice.geofencing.bindingadapters

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.databinding.BindingAdapter
import app.practice.geofencing.R
import app.practice.geofencing.data.GeofenceEntity
import app.practice.geofencing.util.ExtensionFunctions.disable
import app.practice.geofencing.util.ExtensionFunctions.enable
import coil.load

@BindingAdapter("setVisibility")
fun View.setVisibility(data: List<GeofenceEntity>) {

    if (data.isNullOrEmpty()) {
        this.visibility = View.VISIBLE
    } else {
        this.visibility = View.INVISIBLE
    }

}

@BindingAdapter("handleMotionTransition")
fun MotionLayout.handleMotionTransition(deleteImageView: ImageView) {
    this.setTransitionListener(
        object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
            }

            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, transition: Int) {
                if (motionLayout != null && transition == R.id.start) {
                    deleteImageView.disable()
                } else if (motionLayout != null && transition == R.id.end) {
                    deleteImageView.enable()
                }
            }

            override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {
            }

        }
    )
}

@BindingAdapter("loadImage")
fun ImageView.loadImage(bitmap: Bitmap) {
    this.load(bitmap)
}

@BindingAdapter("parseCoordinates")
fun TextView.parseCoordinates(value: Double) {
    val coordinate = String.format("%.4f", value)
    this.text = coordinate
}