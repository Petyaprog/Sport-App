package com.example.realmadrid

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.realmadrid.database.AppDatabase
import com.example.realmadrid.database.UserRepository
import com.example.realmadrid.databinding.RegistrationBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

class Registration : AppCompatActivity() {

    private lateinit var binding: RegistrationBinding
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        // Инициализация базы данных и репозитория
        AppDatabase.DatabaseProvider.init(applicationContext)
        userRepository = AppDatabase.DatabaseProvider.getUserRepository()

        setupDatePicker()
        setupRegisterButton()
    }

    private fun setupDatePicker() {
        binding.birthDateContainer.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = String.format(
                        Locale.getDefault(),
                        "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear
                    )
                    binding.birthDateText.text = formattedDate
                },
                year, month, day
            ).apply {
                datePicker.maxDate = calendar.timeInMillis
                show()
            }
        }
    }

    private fun setupRegisterButton() {
        binding.registerButton.setOnClickListener {
            val emailText = binding.email.text.toString().trim()
            val telephoneText = binding.telephone.text.toString().trim()
            val usernameText = binding.usernameRegister.text.toString().trim()
            val passwordText = binding.passwordRegister.text.toString().trim()
            val repeatPasswordText = binding.repeatPassword.text.toString().trim()
            val birthDate = binding.birthDateText.text.toString().trim()
            val validDomains = listOf("@gmail.com", "@mail.ru", "@yandex.ru")

            when {
                TextUtils.isEmpty(emailText) || TextUtils.isEmpty(usernameText) ||
                        TextUtils.isEmpty(passwordText) || TextUtils.isEmpty(repeatPasswordText) ||
                        TextUtils.isEmpty(telephoneText) || TextUtils.isEmpty(birthDate) -> {
                    showToast("Пожалуйста, заполните все поля")
                }
                !validDomains.any { emailText.contains(it) } -> {
                    showToast("Введите корректный email (например, example@gmail.com)")
                }
                !isValidPhoneNumber(telephoneText) -> {
                    showToast("Введите корректный номер телефона (начинается с +7 или 8, 11 цифр)")
                }
                passwordText.length < 6 -> {
                    showToast("Пароль должен быть не менее 6 символов")
                }
                passwordText != repeatPasswordText -> {
                    showToast("Пароли не совпадают")
                }
                else -> {
                    checkAndRegisterUser(emailText, telephoneText, birthDate, usernameText, passwordText)
                }
            }
        }
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        val phoneRegex = Regex("^(\\+7|8)[0-9]{10,11}\$")
        return phone.matches(phoneRegex)
    }

    private fun checkAndRegisterUser(
        email: String,
        telephone: String,
        birthDate: String,
        username: String,
        password: String,
        role: String = "user"
    ) {
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
                    userRepository.addUser(email, telephone, birthDate, username, password, role)
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