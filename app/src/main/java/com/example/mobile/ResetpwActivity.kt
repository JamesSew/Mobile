package com.example.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ResetpwActivity : AppCompatActivity() {

    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resetpw)

        newPasswordEditText = findViewById(R.id.newPW)
        confirmPasswordEditText = findViewById(R.id.connewPW)
        database = FirebaseDatabase.getInstance().getReference("users")

        val resetButton = findViewById<Button>(R.id.newPWButton)
        resetButton.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (newPassword.isNotEmpty() && newPassword == confirmPassword) {
                val username = intent.getStringExtra("username")
                if (username != null) {
                    updatePasswordInDatabase(username, newPassword)
                } else {
                    Toast.makeText(
                        this@ResetpwActivity,
                        "Invalid username. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this@ResetpwActivity,
                    "Passwords do not match. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updatePasswordInDatabase(username: String, newPassword: String) {
        val userRef = database.child(username)
        userRef.child("password").setValue(newPassword)

        val intent = Intent(this@ResetpwActivity, LoginActivity::class.java)
        startActivity(intent)

        finish()
    }
}
