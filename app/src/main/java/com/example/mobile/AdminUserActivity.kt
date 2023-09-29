package com.example.mobile

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.assignment.UserData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso


class AdminUserActivity : AppCompatActivity() {
    // Declare your Firebase Database reference
    private lateinit var database: DatabaseReference

    private lateinit var userPPImageView: ImageView
    private lateinit var userNameEditText: EditText
    private lateinit var userDisnameEditText: EditText
    private lateinit var userNumEditText: EditText
    private lateinit var searchUserEditText: EditText
    private lateinit var deleteConfirmationDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_user)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().getReference("users")

        userPPImageView = findViewById(R.id.userPP)
        userNameEditText = findViewById(R.id.userName)
        userDisnameEditText = findViewById(R.id.userDisname)
        userNumEditText = findViewById(R.id.userNum)
        searchUserEditText = findViewById(R.id.searchUser)

        val searchButton = findViewById<Button>(R.id.searchButton)
        searchButton.setOnClickListener {
            val usernameToSearch = searchUserEditText.text.toString()
            searchUserInDatabase(usernameToSearch)
        }

        val deleteButton = findViewById<Button>(R.id.deleteButton)
        deleteButton.setOnClickListener {
            val usernameToDelete = userNameEditText.text.toString()
            showDeleteConfirmationDialog(usernameToDelete)
        }
    }

    private fun searchUserInDatabase(username: String) {
        val query: Query = database.orderByChild("username").equalTo(username)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (userSnapshot in dataSnapshot.children) {
                        val user = userSnapshot.getValue(UserData::class.java)
                        if (user != null) {
                            // User found, populate the UI with user data
                            Picasso.get().load(user.imageUri).into(userPPImageView)
                            userNameEditText.setText(user.username)
                            userDisnameEditText.setText(user.displayname)
                            userNumEditText.setText(user.number)
                            return
                        }
                    }
                } else {
                    // User not found, display a toast message
                    Toast.makeText(this@AdminUserActivity, "User not found", Toast.LENGTH_SHORT).show()

                    // You can also clear the UI fields here if needed
                    userPPImageView.setImageResource(R.drawable.avatar) // Set a default image
                    userNameEditText.text.clear()
                    userDisnameEditText.text.clear()
                    userNumEditText.text.clear()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any database query errors here
            }
        })
    }
    private fun deleteUserData(usernameToDelete: String) {
        // Check if the username is not empty
        if (usernameToDelete.isNotEmpty()) {
            // Get a reference to the user's data using the username
            val userReference = database.child(usernameToDelete)

            // Remove the user's data from the database
            userReference.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // User data deleted successfully
                    Toast.makeText(this@AdminUserActivity, "User data deleted successfully", Toast.LENGTH_SHORT).show()

                    // Clear the UI fields
                    userPPImageView.setImageResource(R.drawable.avatar) // Set a default image
                    userNameEditText.text.clear()
                    userDisnameEditText.text.clear()
                    userNumEditText.text.clear()
                } else {
                    // Error occurred while deleting user data
                    Toast.makeText(this@AdminUserActivity, "Failed to delete user data", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Username is empty, show a message or handle it as needed
            Toast.makeText(this@AdminUserActivity, "Username cannot be empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmationDialog(usernameToDelete: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Deletion")
        builder.setMessage("Are you sure you want to delete the user data for $usernameToDelete?")
        builder.setPositiveButton("Yes") { _, _ ->
            // User confirmed deletion, delete the data
            deleteUserData(usernameToDelete)
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            // User canceled the deletion, dismiss the dialog
            dialog.dismiss()
        }

        // Create and show the dialog
        deleteConfirmationDialog = builder.create()
        deleteConfirmationDialog.show()
    }


}

