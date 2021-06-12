package app.practice.geofencing.bindingadapters

import android.widget.TextView
import androidx.databinding.BindingAdapter
import app.practice.geofencing.R
import app.practice.geofencing.presentation.SharedViewModel
import com.google.android.material.slider.Slider

@BindingAdapter("updateSliderValueTextView", "getGeoRadius", requireAll = true)
fun Slider.updateSliderValue(textView: TextView, sharedViewModel: SharedViewModel) {
    updateSliderValueTextView(sharedViewModel.geoRedius, textView)
    this.addOnChangeListener { slider, value, fromUser ->
        sharedViewModel.geoRedius = value
        updateSliderValueTextView(sharedViewModel.geoRedius, textView)
    }
}

fun Slider.updateSliderValueTextView(geoRedius: Float, textView: TextView) {
    val kilometers = geoRedius / 1000
    if (geoRedius >= 1000f) {
        textView.text = context.getString(R.string.display_kilometers, kilometers.toString())
    } else {
        textView.text = context.getString(R.string.display_meters, geoRedius.toString())
    }
    this.value = geoRedius
}
