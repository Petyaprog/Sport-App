package com.example.realmadrid

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Registration : AppCompatActivity() {

    private lateinit var registerButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.registration)

        registerButton = findViewById(R.id.registerButton)

        val myIntent = Intent(this, Authentication::class.java)

        registerButton.setOnClickListener{ startActivity(myIntent) }
    }
}