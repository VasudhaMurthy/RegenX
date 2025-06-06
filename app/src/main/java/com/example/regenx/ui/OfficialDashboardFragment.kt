package com.example.regenx.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.regenx.databinding.FragmentOfficialDashboardBinding
import com.example.regenx.viewmodel.GrievanceViewModel
import com.example.regenx.adapter.GrievanceAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OfficialDashboardFragment : Fragment() {
    private val viewModel: GrievanceViewModel by viewModels()
    private var _binding: FragmentOfficialDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOfficialDashboardBinding.inflate(inflater, container, false)
        val adapter = GrievanceAdapter()
        binding.rvGrievances.adapter = adapter

        viewModel.grievances.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
