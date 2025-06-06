package com.example.regenx.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.regenx.databinding.FragmentResidentGrievanceBinding
import com.example.regenx.viewmodel.GrievanceViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResidentGrievanceFragment : Fragment() {
    private val viewModel: GrievanceViewModel by viewModels()
    private var _binding: FragmentResidentGrievanceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentResidentGrievanceBinding.inflate(inflater, container, false)

        binding.btnSubmit.setOnClickListener {
            val description = binding.etDescription.text.toString()
            viewModel.submitResidentGrievance(description)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
