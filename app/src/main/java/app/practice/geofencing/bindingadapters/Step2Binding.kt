package app.practice.geofencing.bindingadapters

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import app.practice.geofencing.util.ExtensionFunctions.hide
import app.practice.geofencing.util.ExtensionFunctions.show
import com.google.android.material.textfield.TextInputLayout


@BindingAdapter("handleNetworkConnection", "handleRecyclerView", requireAll = true)
fun TextInputLayout.handleNetworkConnection(networkAvailable: Boolean, recyclerView: RecyclerView) {
    if (!networkAvailable) {
        this.isErrorEnabled = true
        this.error = "No Internet Connection"
        recyclerView.hide()
    } else {
        this.isErrorEnabled = false
        this.error = null
        recyclerView.show()
    }
}