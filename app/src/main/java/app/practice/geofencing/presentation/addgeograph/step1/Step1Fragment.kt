package app.practice.geofencing.presentation.addgeograph.step1

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.practice.geofencing.R
import app.practice.geofencing.R.*
import app.practice.geofencing.databinding.FragmentStep1Binding
import app.practice.geofencing.presentation.SharedViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.launch

class Step1Fragment : Fragment() {

    private var _binding: FragmentStep1Binding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val step1ViewModel: Step1ViewModel by viewModels()

    private lateinit var geoCoder: Geocoder
    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Places.initialize(requireContext(), getString(string.google_maps_key))
        placesClient = Places.createClient(requireContext())
        geoCoder = Geocoder(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStep1Binding.inflate(layoutInflater, container, false)
        binding.sharedViewModel = sharedViewModel
        binding.step1ViewModel = step1ViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.step1Back.setOnClickListener {
            onStep1BackClicked()
        }

        getCountryCodeFromCurrentLocation()

    }

    @SuppressLint("MissingPermission")
    private fun getCountryCodeFromCurrentLocation() {
        lifecycleScope.launch {
            val placeFields = listOf(Place.Field.LAT_LNG)
            val request = FindCurrentPlaceRequest.newInstance(placeFields)

            val placeResponse = placesClient.findCurrentPlace(request)
            placeResponse.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    try {
                        val response = task.result
                        val latLng = response.placeLikelihoods.first().place.latLng
                        latLng?.let {
                            val address = geoCoder.getFromLocation(
                                latLng.latitude,
                                latLng.longitude,
                                1
                            )
                            sharedViewModel.geoCountryCode = address.first().countryCode
                        }
                    } catch (exception: Exception) {
                        Log.e("Step1Fragment", "getFromLocation FAILED")
                    } finally {
                        enableNextButton()
                    }
                } else {
                    val exception = task.exception
                    if (exception is ApiException) {
                        Log.e("Step1Fragment", exception.statusCode.toString())
                        Log.e("Step1Fragment", exception.message.toString())
                        Log.e("Step1Fragment", exception.cause?.stackTrace.toString())
                    }
                    enableNextButton()
                }
            }
        }
    }

    private fun enableNextButton() {
        if (sharedViewModel.geoName.isNotEmpty()) {
            step1ViewModel.enableNextButton(true)
        }
    }

    private fun onStep1BackClicked() {
        findNavController().navigate(R.id.action_step1Fragment_to_mapFragment)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }


}