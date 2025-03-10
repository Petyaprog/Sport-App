package com.example.realmadrid

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class Authorization : AppCompatActivity() {

    private lateinit var admin_button: ImageButton
    private lateinit var user_button: ImageButton

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.authorization)

        admin_button = findViewById(R.id.admin_button)
        user_button = findViewById(R.id.user_button)

        val myIntent = Intent(this, Authentication::class.java)

        user_button.setOnClickListener{ startActivity(myIntent) }
        admin_button.setOnClickListener{ startActivity(myIntent) }
    }
}