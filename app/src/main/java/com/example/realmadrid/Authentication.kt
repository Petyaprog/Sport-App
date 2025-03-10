package com.example.realmadrid

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Authentication : AppCompatActivity() {

    private lateinit var btnLodin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.authentication)

        btnLodin = findViewById(R.id.btnLogin)
        val myIntent = Intent(this, MainActivity::class.java)

        btnLodin.setOnClickListener {
            startActivity(myIntent)
            finish()
        }
    }
}