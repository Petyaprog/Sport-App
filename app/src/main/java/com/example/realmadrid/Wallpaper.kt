package com.example.realmadrid

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class Wallpaper : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.wallpaper)

        Handler().postDelayed({
            startActivity(Intent(this, Authentication::class.java))
            finish() // Завершение текущей активности
        }, 5000)
    }
}