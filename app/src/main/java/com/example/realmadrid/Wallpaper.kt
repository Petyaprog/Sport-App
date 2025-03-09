package com.example.realmadrid

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class Wallpaper : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.wallpaper)

        Handler().postDelayed({
            val myIntent = Intent(this, Authorization::class.java)
            startActivity(myIntent)
            finish() // Завершение текущей активности
        }, 5000)
    }
}