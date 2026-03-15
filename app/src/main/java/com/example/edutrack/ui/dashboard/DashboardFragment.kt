package com.example.edutrack.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.edutrack.R
import com.example.edutrack.databinding.FragmentDashboardBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tampilkan nama user dari Firebase
        val user = Firebase.auth.currentUser
        val username = user?.email?.substringBefore("@") ?: "Pengguna"
        binding.tvGreeting.text = "Halo, $username!"

        // Placeholder stats (akan diisi Sprint 2+)
        binding.tvTaskCount.text = "0"
        binding.tvStreakCount.text = "0 hari"

        // Tombol logout
        binding.btnLogout.setOnClickListener {
            Firebase.auth.signOut()
            findNavController().navigate(R.id.action_dashboardFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
