package com.example.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.assignment.UserData
import com.example.mobile.databinding.ActivityLoginBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")

        binding.login2button.setOnClickListener {
            val loginUsername = binding.loginUsername.text.toString() // Change "loginEmail" to "loginUsername"
            val loginPassword = binding.loginPassword.text.toString()

            if (loginUsername.isNotEmpty() && loginPassword.isNotEmpty()) {
                loginUser(loginUsername, loginPassword)
            } else {
                Toast.makeText(this@LoginActivity, "All fields are required", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signupRedirect.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
            finish()
        }
        binding.forgotpwredirect.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ForgotpwActivity::class.java))
            finish()
        }
    }

    private fun loginUser(username: String, password: String) { // Change parameter name to "username"
        databaseReference.orderByChild("username").equalTo(username) // Change "email" to "username"
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (userSnapshot in dataSnapshot.children) {
                            val userData = userSnapshot.getValue(UserData::class.java)

                            if (userData != null && userData.password == password) {
                                Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()

                                // Redirect to HomeActivity and pass user's username as an extra
                                val homeIntent = Intent(this@LoginActivity, HomeActivity::class.java)
                                homeIntent.putExtra("username", username) // Change "email" to "username"
                                startActivity(homeIntent)

                                finish()
                                return
                            }
                        }
                    }
                    Toast.makeText(this@LoginActivity, "Login Unsuccessful", Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@LoginActivity, "Database Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
