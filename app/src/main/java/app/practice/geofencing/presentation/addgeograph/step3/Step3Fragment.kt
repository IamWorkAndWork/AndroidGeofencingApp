package app.practice.geofencing.presentation.addgeograph.step3

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import app.practice.geofencing.R
import app.practice.geofencing.databinding.FragmentStep3Binding
import app.practice.geofencing.presentation.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

class Step3Fragment : Fragment() {

    private var _binding: FragmentStep3Binding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStep3Binding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.step3Back.setOnClickListener {
            findNavController().navigate(R.id.action_step3Fragment_to_step2Fragment)
        }

        binding.step3Done.setOnClickListener {
            sharedViewModel.geoRedius = binding.slider.value
            sharedViewModel.geofenceReady = true
            findNavController().navigate(R.id.action_step3Fragment_to_mapsFragment)
        }

    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}