package com.example.realmadrid

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.realmadrid.database.AppDatabase.DatabaseProvider
import com.example.realmadrid.database.UserRepository
import com.example.realmadrid.databinding.AuthenticationBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Authentication : AppCompatActivity() {

    private lateinit var binding: AuthenticationBinding
    private lateinit var userRepository: UserRepository
    private var selectedRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setupDatabase()
        setupRoleSelection()
        setupClickListeners()
    }

    private fun setupDatabase() {
        DatabaseProvider.init(applicationContext)
        userRepository = DatabaseProvider.getUserRepository()
    }

    private fun setupRoleSelection() {
        binding.userRoleButton.setOnClickListener {
            selectedRole = "user"
            updateRoleUI()
        }

        binding.adminRoleButton.setOnClickListener {
            selectedRole = "admin"
            updateRoleUI()
        }
    }

    private fun updateRoleUI() {
        val selectedColor = ContextCompat.getColor(this, R.color.purple)
        val unselectedColor = ContextCompat.getColor(this, R.color.gray)

        binding.userRoleButton.backgroundTintList = ColorStateList.valueOf(
            if (selectedRole == "user") selectedColor else unselectedColor
        )

        binding.adminRoleButton.backgroundTintList = ColorStateList.valueOf(
            if (selectedRole == "admin") selectedColor else unselectedColor
        )

        binding.signupText.visibility = if (selectedRole == "admin") View.GONE else View.VISIBLE
    }

    private fun setupClickListeners() {
        val mainIntent = Intent(this, MainActivity::class.java)
        val registrationIntent = Intent(this, Registration::class.java)

        binding.loginButton.setOnClickListener {
            val email = binding.username.text.toString().trim()
            val password = binding.password.text.toString().trim()

            when {
                email.isEmpty() || password.isEmpty() -> {
                    showToast("Пожалуйста, заполните все поля")
                }
                selectedRole == null -> {
                    showToast("Пожалуйста, выберите роль (User/Admin)")
                }
                else -> {
                    authenticateUser(email, password, selectedRole!!, mainIntent)
                }
            }
        }

        binding.signupText.setOnClickListener {
            startActivity(registrationIntent)
        }
    }

    private fun authenticateUser(email: String, password: String, role: String, successIntent: Intent) {
        lifecycleScope.launch {
            try {
                val isAuthenticated = withContext(Dispatchers.IO) {
                    userRepository.loginUser(email, password, role)
                }

                if (isAuthenticated) {
                    showToast("Вход выполнен успешно как $role")
                    startActivity(successIntent)
                    finish()
                } else {
                    showToast("Неверные учетные данные или роль")
                }
            } catch (e: Exception) {
                showToast("Ошибка при входе: ${e.localizedMessage}")
                e.printStackTrace()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.launch(Dispatchers.IO) {
            userRepository.logout()
        }
    }
}