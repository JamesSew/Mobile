package com.example.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.assignment.UserData
import com.example.mobile.databinding.ActivitySignupBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.regex.Pattern

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signup2button.setOnClickListener {

            val username = binding.signupUsername.text.toString()
            val displayname = binding.signupDisplayname.text.toString()
            val phoneNumber = binding.signupNumber.text.toString()
            val password = binding.signupPassword.text.toString()
            val confirmPassword = binding.signupConpass.text.toString()

            if (!isValidPhoneNumber(phoneNumber)) {
                Toast.makeText(this, "Phone numbers should be within 10-11 digits and not contain alphabets", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidPassword(password, confirmPassword)) {
                Toast.makeText(this, "Password should contain at least 1 uppercase, 1 lowercase, 1 number, and 1 symbol", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = username.toString()

            database = FirebaseDatabase.getInstance().getReference("users")

            val defaultAvatarUrl = "android.resource://com.example.mobile/${R.drawable.avatar}"

            val userData = UserData(username, displayname, phoneNumber, password, defaultAvatarUrl)

            database.child(username).setValue(userData).addOnSuccessListener {
                binding.signupDisplayname.text.clear()
                binding.signupNumber.text.clear()
                binding.signupPassword.text.clear()
                binding.signupConpass.text.clear()
                Toast.makeText(this, "Sign up successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@SignupActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "There was an error", Toast.LENGTH_SHORT).show()
            }
        }


        val loginRedirectTextView = findViewById<TextView>(R.id.loginRedirect)
        loginRedirectTextView.setOnClickListener {
            val loginIntent = Intent(this@SignupActivity, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return Pattern.matches("[0-9]+", phoneNumber) && (phoneNumber.length == 10 || phoneNumber.length == 11)
    }

    private fun isValidPassword(password: String, confirmPassword: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$".toRegex()
        return passwordPattern.matches(password) && password == confirmPassword
    }
}
