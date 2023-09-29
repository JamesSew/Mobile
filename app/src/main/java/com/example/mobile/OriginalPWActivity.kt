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

    private lateinit var originalPasswordEditText: EditText
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_original_pwactivity)

        originalPasswordEditText = findViewById(R.id.originalPW)
        database = FirebaseDatabase.getInstance()
            .getReference("users") // Assuming your database reference is correct

        val submitButton = findViewById<Button>(R.id.oripwButton)

        submitButton.setOnClickListener {
            val enteredOriginalPassword = originalPasswordEditText.text.toString()
            validateOriginalPassword(enteredOriginalPassword)
        }
    }

    private fun validateOriginalPassword(password: String) {
        if (password.isNotEmpty()) {
            val query: Query = database.orderByChild("password").equalTo(password)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (userSnapshot in dataSnapshot.children) {
                            val storedPassword = userSnapshot.child("password").value.toString()
                            if (storedPassword == password) {
                                // Password is correct, navigate to com.example.mobile.ChangePWActivity
                                val intent = Intent(this@OriginalPWActivity, ChangePWActivity::class.java)
                                intent.putExtra("username", password)
                                startActivity(intent)
                                return
                            }
                        }
                    }
                    Toast.makeText(this@OriginalPWActivity, "Invalid Password", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle any database query errors here
                }
            })
        } else {
            // Password is empty, show a toast message
            Toast.makeText(this@OriginalPWActivity, "Password cannot be empty", Toast.LENGTH_SHORT)
                .show()
        }
    }
}
