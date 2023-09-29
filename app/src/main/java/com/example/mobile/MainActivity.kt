package com.example.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Set the content view to your layout XML

        // Find the button with the ID signup_button
        val signUpButton = findViewById<Button>(R.id.signup_button)

        // Set an OnClickListener on the "signup_button"
        signUpButton.setOnClickListener {
            // Create an Intent to navigate to com.example.mobile.com.example.mobile.SignupActivity
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        // Find the button with the ID login_button
        val loginButton = findViewById<Button>(R.id.login_button)

        // Set an OnClickListener on the "login_button"
        loginButton.setOnClickListener {
            // Create an Intent to navigate to com.example.mobile.com.example.mobile.LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
