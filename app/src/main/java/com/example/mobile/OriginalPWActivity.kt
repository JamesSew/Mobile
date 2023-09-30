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

class OriginalPWActivity : AppCompatActivity() {

    private lateinit var originalNameEditText: EditText
    private lateinit var originalPWEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_original_pwactivity)

        // Initialize Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference("users")

        originalNameEditText = findViewById(R.id.originalName)
        originalPWEditText = findViewById(R.id.originalPW)
        submitButton = findViewById(R.id.oripwButton)

        submitButton.setOnClickListener {
            val username = originalNameEditText.text.toString()
            val originalPassword = originalPWEditText.text.toString()

            // Query the database to check for a matching record
            queryDatabaseForPassword(username, originalPassword)
        }
    }

    private fun queryDatabaseForPassword(username: String, originalPassword: String) {
        val query: Query = databaseReference.orderByChild("username").equalTo(username)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (userSnapshot in dataSnapshot.children) {
                        val userPassword = userSnapshot.child("password").value.toString()
                        // Check if the provided original password matches the database
                        if (userPassword == originalPassword) {
                            // Original password matches, navigate to your desired activity (e.g., ChangePWActivity)
                            val intent = Intent(this@OriginalPWActivity, ChangePWActivity::class.java)
                            intent.putExtra("username", username)
                            startActivity(intent)
                            finish()
                            return
                        }
                    }
                }

                // If no match is found, show a toast message
                Toast.makeText(this@OriginalPWActivity, "Invalid username or original password.", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any database query errors here
                Toast.makeText(this@OriginalPWActivity, "Database error: " + databaseError.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}
