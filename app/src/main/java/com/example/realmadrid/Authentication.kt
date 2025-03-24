package com.example.realmadrid

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Authentication : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var signupText: TextView
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.authentication)

        etEmail = findViewById(R.id.username)
        etPassword = findViewById(R.id.password)
        btnLogin = findViewById(R.id.loginButton)
        signupText = findViewById(R.id.signupText)
        dbHelper = DatabaseHelper(this)

        val myIntent = Intent(this, MainActivity::class.java)
        val myIntent2 = Intent(this, Registration::class.java)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            } else {
                // Проверка пользователя в базе данных
                if (dbHelper.checkUser(email, password)) {
                    Toast.makeText(this, "Вход выполнен успешно", Toast.LENGTH_SHORT).show()
                    startActivity(myIntent)
                    finish()
                } else {
                    Toast.makeText(this, "Неверный email или пароль", Toast.LENGTH_SHORT).show()
                }
            }
        }
        signupText.setOnClickListener{ startActivity(myIntent2) }
    }
    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}