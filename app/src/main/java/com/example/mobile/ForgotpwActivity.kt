package com.example.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class ForgotpwActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var phoneNumberEditText: EditText

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgotpw)

        usernameEditText = findViewById(R.id.forgotpwUsername)
        phoneNumberEditText = findViewById(R.id.forgotpwNumber)

        database = FirebaseDatabase.getInstance().getReference("users") // Assuming your database reference is correct

        val submitButton = findViewById<Button>(R.id.resetpwButton)
        submitButton.setOnClickListener {
            val enteredUsername = usernameEditText.text.toString()
            val enteredPhoneNumber = phoneNumberEditText.text.toString()

            // Validate the input and check if it matches a database record
            validateAndNavigate(enteredUsername, enteredPhoneNumber)
        }
    }

    private fun validateAndNavigate(username: String, phoneNumber: String) {
        // Query the database to check for a matching record
        val query: Query = database.orderByChild("username").equalTo(username)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (userSnapshot in dataSnapshot.children) {
                        val userPhoneNumber = userSnapshot.child("number").value.toString()
                        // Check if the provided phone number matches the database
                        if (userPhoneNumber == phoneNumber) {
                            // Match found, navigate to com.example.mobile.ResetpwActivity
                            val intent = Intent(this@ForgotpwActivity, ResetpwActivity::class.java)
                            intent.putExtra("username", username)
                            startActivity(intent)
                            return
                        }
                    }
                }

                // If no match is found, show a toast message
                Toast.makeText(this@ForgotpwActivity, "Invalid Username or Phone number", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any database query errors here
            }
        })
    }
}
