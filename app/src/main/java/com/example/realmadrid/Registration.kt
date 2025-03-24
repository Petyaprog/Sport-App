package com.example.realmadrid

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.compareTo

class Registration : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var repeat_password: EditText
    private lateinit var registerButton: Button
    private lateinit var dbHelper: DatabaseHelper

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.registration)

        registerButton = findViewById(R.id.registerButton)
        email = findViewById(R.id.email)
        username = findViewById(R.id.username_register)
        password = findViewById(R.id.password_register)
        repeat_password = findViewById(R.id.repeat_password)
        dbHelper = DatabaseHelper(this)

        registerButton.setOnClickListener{
            val email = email.text.toString().trim()
            val username = username.text.toString().trim()
            val password = password.text.toString().trim()
            val repeatPassword = repeat_password.text.toString().trim()
            val validDomains = listOf("@gmail.com", "@mail.ru", "@yandex.ru")

            when {
                TextUtils.isEmpty(email) || TextUtils.isEmpty(username) ||
                        TextUtils.isEmpty(password) || TextUtils.isEmpty(repeatPassword) -> {
                    Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
                }
                !validDomains.any { email.contains(it) } -> {
                    Toast.makeText(this, "Введите корректный email (например, example@gmail.com)", Toast.LENGTH_SHORT).show()
                }
                password.length < 6 -> {
                    Toast.makeText(this, "Пароль должен быть не менее 6 символов", Toast.LENGTH_SHORT).show()
                }
                password != repeatPassword -> {
                    Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                }
                dbHelper.isEmailExists(email) -> {
                    Toast.makeText(this, "Этот email уже зарегистрирован", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    registerUser(email, username, password)
                }
            }
        }
    }

    private fun registerUser(email: String, username: String, password: String) {
        // Добавляем проверку на уникальность username
        if (dbHelper.isUsernameExists(username)) {
            Toast.makeText(this, "Этот логин уже занят", Toast.LENGTH_SHORT).show()
            return
        }

        val success = dbHelper.addUser(email, username, password)
        if (success != -1L) {
            Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Authentication::class.java))
            finish()
        } else {
            Toast.makeText(this, "Ошибка: возможно, email или логин уже заняты", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}

