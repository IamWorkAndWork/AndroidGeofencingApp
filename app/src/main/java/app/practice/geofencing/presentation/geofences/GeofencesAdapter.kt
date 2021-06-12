package app.practice.geofencing.presentation.geofences

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.practice.geofencing.R
import app.practice.geofencing.data.GeofenceEntity
import app.practice.geofencing.databinding.GeofencesRowLayoutBinding
import app.practice.geofencing.presentation.SharedViewModel
import app.practice.geofencing.util.MyDiffUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class GeofencesAdapter(private val sharedViewModel: SharedViewModel) :
    RecyclerView.Adapter<GeofencesAdapter.MyViewHolder>() {

    private var geofenceEntity = mutableListOf<GeofenceEntity>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent, sharedViewModel)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentGeofence = geofenceEntity.get(position)
        holder.bind(currentGeofence)
    }

    override fun getItemCount(): Int {
        return geofenceEntity.size
    }

    fun setData(newGeofenceEntity: MutableList<GeofenceEntity>) {
        val geofenceDiffUtil = MyDiffUtil(geofenceEntity, newGeofenceEntity)
        val diffUtilResult = DiffUtil.calculateDiff(geofenceDiffUtil)
        geofenceEntity = newGeofenceEntity
        diffUtilResult.dispatchUpdatesTo(this)
    }

    class MyViewHolder(
        private val binding: GeofencesRowLayoutBinding,
        private val sharedViewModel: SharedViewModel
    ) :
        RecyclerView.ViewHolder(binding.root) {

        private var geofenceEntity: GeofenceEntity? = null

        companion object {
            fun from(parent: ViewGroup, sharedViewModel: SharedViewModel): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = GeofencesRowLayoutBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding, sharedViewModel)
            }
        }

        init {
            binding.deleteImageView.setOnClickListener {
                geofenceEntity?.let { item ->
                    removeItem(this, item)
                }
            }

            binding.snapshotImageView.setOnClickListener {
                val action = GeofencesFragmentDirections.actionGeofencesFragmentToMapsFragment(
                    geofenceEntity
                )
                itemView.findNavController().navigate(action)
            }
        }

        private fun removeItem(holder: MyViewHolder, geofenceEntity: GeofenceEntity) {
            sharedViewModel.viewModelScope.launch {
                val geofenceStopped = sharedViewModel.stopGeofence(
                    listOf(geofenceEntity.geoId)
                )
                if (geofenceStopped) {
                    sharedViewModel.removeGeofence(geofenceEntity = geofenceEntity)
                    showSnackBar(holder, geofenceEntity)
                } else {
                    Log.d("GeofencesAdapter", "Geofence NOT REMOVED!")
                }
            }
        }

        private fun showSnackBar(
            holder: MyViewHolder,
            removedItem: GeofenceEntity
        ) {
            Snackbar.make(
                holder.itemView,
                "Removed " + removedItem.name,
                Snackbar.LENGTH_LONG
            ).setAction("UNDO") {
                undoRemoval(holder, removedItem)
            }.show()
        }

        private fun undoRemoval(
            holder: MyViewHolder,
            removedItem: GeofenceEntity
        ) {
            holder.binding.motionLayout.transitionToState(R.id.start)
            sharedViewModel.addGeofence(removedItem)
            sharedViewModel.startGeofence(
                removedItem.latitude,
                removedItem.longitude
            )
        }

        fun bind(geofenceEntity: GeofenceEntity) = with(binding) {
            this@MyViewHolder.geofenceEntity = geofenceEntity
            geofencesEntity = geofenceEntity
            executePendingBindings()
        }

    }

}