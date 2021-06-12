package app.practice.geofencing.presentation.geofences

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import app.practice.geofencing.R
import app.practice.geofencing.databinding.FragmentGeofencesBinding
import app.practice.geofencing.presentation.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GeofencesFragment : Fragment() {

    private var _binding: FragmentGeofencesBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val geofencesAdapter by lazy {
        GeofencesAdapter(sharedViewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeofencesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.sharedViewModel = sharedViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        observeDatabase()
    }

    private fun setupRecyclerView() = with(binding) {
        geofencesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = geofencesAdapter
        }
    }

    private fun observeDatabase() {
        sharedViewModel.readGeofences.observe(viewLifecycleOwner) { itemList ->
            geofencesAdapter.setData(itemList.toMutableList())
            binding.geofencesRecyclerView.scheduleLayoutAnimation()
        }
    }

    private fun setupToolbar() = with(binding) {
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}