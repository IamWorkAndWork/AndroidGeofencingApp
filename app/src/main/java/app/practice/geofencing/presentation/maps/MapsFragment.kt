package app.practice.geofencing.presentation.maps

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.practice.geofencing.R
import app.practice.geofencing.R.*
import app.practice.geofencing.databinding.FragmentMapsBinding
import app.practice.geofencing.presentation.SharedViewModel
import app.practice.geofencing.util.ExtensionFunctions.disable
import app.practice.geofencing.util.ExtensionFunctions.enable
import app.practice.geofencing.util.ExtensionFunctions.hide
import app.practice.geofencing.util.ExtensionFunctions.show
import app.practice.geofencing.util.Permissions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener,
    EasyPermissions.PermissionCallbacks, GoogleMap.SnapshotReadyCallback {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<MapsFragmentArgs>()

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var map: GoogleMap
    private lateinit var circle: Circle

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListener()
        initWidget()
    }

    private fun initWidget() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    private fun initListener() {
        binding.addGeofenceFab.setOnClickListener {
            findNavController().navigate(R.id.action_mapsFragment_to_add_geofence_graph)
        }

        binding.geofencesFab.setOnClickListener {
            findNavController().navigate(R.id.action_mapsFragment_to_geofencesFragment)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.mapstyle))
        map.isMyLocationEnabled = true
        map.setOnMapLongClickListener(this)
        map.uiSettings.apply {
            isMyLocationButtonEnabled = true
            isMapToolbarEnabled = false
        }
        onGeofenceReady()
        observeDatabase()
        backFromGeofencesFragment()
    }

    private fun onGeofenceReady() {
        if (sharedViewModel.geofenceReady) {
            sharedViewModel.geofenceReady = false
            sharedViewModel.geofencePrepared = true
            displayInfoMessage()
            zoomToSelectedLocation()
        }
    }

    private fun displayInfoMessage() {
        lifecycleScope.launch {
            binding.infoMessageTextView.show()
            delay(2000)
            binding.infoMessageTextView.animate().alpha(0f).duration = 800
            delay(1000)
            binding.infoMessageTextView.hide()
        }
    }

    private fun zoomToSelectedLocation() {
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                sharedViewModel.geoLatLng,
                10f
            ), 2000,
            null
        )
    }

    private fun observeDatabase() {
        sharedViewModel.readGeofences.observe(viewLifecycleOwner) { geofenceEntity ->
            map.clear()
            geofenceEntity.forEach { geofence ->
                drawCircle(LatLng(geofence.latitude, geofence.longitude), geofence.radius)
                drawMarker(LatLng(geofence.latitude, geofence.longitude), geofence.name)
            }
        }
    }

    private fun backFromGeofencesFragment() {
        if (args.geofenceEntity != null) {
            args.geofenceEntity?.let {
                val selectedGeofence = LatLng(
                    it.latitude,
                    it.longitude
                )
                zoomToGeofence(selectedGeofence, it.radius)
            }
        }
    }

    override fun onMapLongClick(location: LatLng) {
        if (Permissions.hasBackgroundLocationPermission(requireContext())) {
            if (sharedViewModel.geofencePrepared && location != null) {
                setupGeofence(location)
            } else {
                Toast.makeText(
                    requireContext(),
                    "You need to create a new Geofence first.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Permissions.requestBackgroundLocationPermission(this)
        }
    }

    private fun setupGeofence(location: LatLng) {
        lifecycleScope.launch {
            if (sharedViewModel.checkDeviceLocationSettings(requireContext())) {
                binding.geofencesFab.disable()
                binding.addGeofenceFab.disable()
                binding.geofenceProgressBar.show()

                drawCircle(location, sharedViewModel.geoRedius)
                drawMarker(location, sharedViewModel.geoName)
                zoomToGeofence(circle.center, circle.radius.toFloat())

                delay(1500)
                map.snapshot(this@MapsFragment)
                delay(2000)
                sharedViewModel.addGeofenceToDatabase(location)
                delay(2000)
                sharedViewModel.startGeofence(location.latitude, location.longitude)
                sharedViewModel.resetSharedViewModel()

                binding.geofencesFab.enable()
                binding.addGeofenceFab.enable()
                binding.geofenceProgressBar.hide()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enable Location Settings.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun drawCircle(location: LatLng, radius: Float) {
        circle = map.addCircle(
            CircleOptions().center(location).radius(radius.toDouble())
                .strokeColor(ContextCompat.getColor(requireContext(), R.color.blue_700))
                .fillColor(ContextCompat.getColor(requireContext(), R.color.blue_transparent))
        )
    }

    private fun drawMarker(location: LatLng, name: String) {
        map.addMarker(
            MarkerOptions().position(location).title(name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        )
    }

    private fun zoomToGeofence(center: LatLng, radius: Float) {
        map.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                sharedViewModel.getBounds(center = center, redius = radius), 10
            ), 1000, null
        )
    }

    override fun onSnapshotReady(snapshot: Bitmap?) {
        sharedViewModel.geoSnapshot = snapshot
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(requireContext()).build().show()
        } else {
            Permissions.requestBackgroundLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        onGeofenceReady()
        Toast.makeText(
            requireContext(),
            "Permission Granted! Long Press on the Map to add a Geofence.",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}