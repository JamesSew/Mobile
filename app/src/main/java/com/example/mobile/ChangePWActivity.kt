package com.example.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ChangePWActivity : AppCompatActivity() {
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmNewPasswordEditText: EditText
    private lateinit var username: String // Assuming you pass the username from the previous activity
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pwactivity)

        newPasswordEditText = findViewById(R.id.changedPW)
        confirmNewPasswordEditText = findViewById(R.id.connchgedPW)

        val resetButton = findViewById<Button>(R.id.oripwButton)
        resetButton.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString()
            val confirmPassword = confirmNewPasswordEditText.text.toString()

            // Validate password
            if (isValidPassword(newPassword)) {
                if (newPassword == confirmPassword) {
                    // Update the password in the database
                    updatePasswordInDatabase(username, newPassword)

                    // Display a success toast
                    Toast.makeText(this@ChangePWActivity, "Password updated successfully", Toast.LENGTH_SHORT).show()

                    // Navigate back to com.example.mobile.ProfileActivity or any other activity
                    val profileIntent = Intent(this@ChangePWActivity, ProfileActivity::class.java)
                    profileIntent.putExtra("username", username)
                    startActivity(profileIntent)
                    finish()
                } else {
                    // Passwords do not match, display error toast
                    Toast.makeText(this@ChangePWActivity, "Confirm password did not match with password", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@ChangePWActivity, "At least 1 uppercase, 1 lowercase, 1 number, 1 symbol, and be at least 8 characters long", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize your Firebase Realtime Database reference here
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
        username = intent.getStringExtra("username") ?: ""
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$".toRegex()
        return passwordPattern.matches(password)
    }

    private fun updatePasswordInDatabase(username: String, newPassword: String) {
        // Use the username to locate the user data in the database
        val userReference = databaseReference.child(username)

        // Update the password field
        userReference.child("password").setValue(newPassword)
    }
}
