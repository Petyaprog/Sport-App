package com.example.realmadrid.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.realmadrid.database.AppDatabase
import com.example.realmadrid.database.UserRepository
import com.example.realmadrid.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        userRepository = AppDatabase.DatabaseProvider.getUserRepository()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserData()
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                val user = userRepository.getCurrentUser()

                if (user != null) {
                    binding.usernameProfile.text = user.username
                    binding.telephoneProfile.text = user.telephone
                    binding.birthDateProfile.text = user.birthdate
                    binding.emailProfile.text = user.email
                } else {
                    showToast("Пользователь не авторизован")
                }
            } catch (e: Exception) {
                showToast("Ошибка загрузки данных: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}