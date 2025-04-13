package com.example.chronosyne

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val signInButton = findViewById<Button>(R.id.btnSignIn)
        val signUpButton = findViewById<Button>(R.id.btnSignUp)

        signInButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        signUpButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}
