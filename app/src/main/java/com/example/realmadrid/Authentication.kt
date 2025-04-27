package com.example.realmadrid

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.realmadrid.database.AppDatabase.DatabaseProvider
import com.example.realmadrid.database.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Authentication : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var signupText: TextView
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.authentication)

        initViews()
        setupDatabase()
        setupClickListeners()
    }

    private fun initViews() {
        etEmail = findViewById(R.id.username)
        etPassword = findViewById(R.id.password)
        btnLogin = findViewById(R.id.loginButton)
        signupText = findViewById(R.id.signupText)
    }

    private fun setupDatabase() {
        DatabaseProvider.init(applicationContext)
        userRepository = DatabaseProvider.getUserRepository()
    }

    private fun setupClickListeners() {
        val mainIntent = Intent(this, MainActivity::class.java)
        val registrationIntent = Intent(this, Registration::class.java)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Пожалуйста, заполните все поля")
                return@setOnClickListener
            }

            authenticateUser(email, password, mainIntent)
        }

        signupText.setOnClickListener {
            startActivity(registrationIntent)
        }
    }

    private fun authenticateUser(email: String, password: String, successIntent: Intent) {
        lifecycleScope.launch {
            try {
                val isValidUser = withContext(Dispatchers.IO) {
                    userRepository.checkUser(email, password)
                }

                if (isValidUser) {
                    showToast("Вход выполнен успешно")
                    startActivity(successIntent)
                    finish()
                } else {
                    showToast("Неверный email или пароль")
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
}