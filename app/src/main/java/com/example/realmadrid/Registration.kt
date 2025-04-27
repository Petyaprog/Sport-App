package com.example.realmadrid

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.realmadrid.database.AppDatabase
import com.example.realmadrid.database.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Registration : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var repeatPassword: EditText
    private lateinit var registerButton: Button
    private lateinit var userRepository: UserRepository

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.registration)

        // Инициализация базы данных и репозитория
        AppDatabase.DatabaseProvider.init(applicationContext)
        userRepository = AppDatabase.DatabaseProvider.getUserRepository()

        initViews()
        setupRegisterButton()
    }

    private fun initViews() {
        registerButton = findViewById(R.id.registerButton)
        email = findViewById(R.id.email)
        username = findViewById(R.id.username_register)
        password = findViewById(R.id.password_register)
        repeatPassword = findViewById(R.id.repeat_password)
    }

    private fun setupRegisterButton() {
        registerButton.setOnClickListener {
            val emailText = email.text.toString().trim()
            val usernameText = username.text.toString().trim()
            val passwordText = password.text.toString().trim()
            val repeatPasswordText = repeatPassword.text.toString().trim()
            val validDomains = listOf("@gmail.com", "@mail.ru", "@yandex.ru")

            when {
                TextUtils.isEmpty(emailText) || TextUtils.isEmpty(usernameText) ||
                        TextUtils.isEmpty(passwordText) || TextUtils.isEmpty(repeatPasswordText) -> {
                    showToast("Пожалуйста, заполните все поля")
                }
                !validDomains.any { emailText.contains(it) } -> {
                    showToast("Введите корректный email (например, example@gmail.com)")
                }
                passwordText.length < 6 -> {
                    showToast("Пароль должен быть не менее 6 символов")
                }
                passwordText != repeatPasswordText -> {
                    showToast("Пароли не совпадают")
                }
                else -> {
                    checkAndRegisterUser(emailText, usernameText, passwordText)
                }
            }
        }
    }

    private fun checkAndRegisterUser(email: String, username: String, password: String) {
        lifecycleScope.launch {
            try {
                // Проверка email и username в базе данных
                val emailExists = withContext(Dispatchers.IO) {
                    userRepository.isEmailExists(email)
                }

                if (emailExists) {
                    showToast("Этот email уже зарегистрирован")
                    return@launch
                }

                val usernameExists = withContext(Dispatchers.IO) {
                    userRepository.isUsernameExists(username)
                }

                if (usernameExists) {
                    showToast("Этот логин уже занят")
                    return@launch
                }

                // Регистрация пользователя
                val userId = withContext(Dispatchers.IO) {
                    userRepository.addUser(email, username, password)
                }

                if (userId > 0) {
                    showToast("Регистрация успешна!")
                    startActivity(Intent(this@Registration, Authentication::class.java))
                    finish()
                } else {
                    showToast("Ошибка при регистрации")
                }
            } catch (e: Exception) {
                showToast("Ошибка: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}