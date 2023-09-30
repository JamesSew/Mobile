package com.example.mobile

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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
    private lateinit var database: DatabaseReference

    private lateinit var userPPImageView: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var userDisnameEditText: EditText
    private lateinit var userNumEditText: EditText
    private lateinit var searchUserEditText: EditText
    private lateinit var deleteConfirmationDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_user)

        database = FirebaseDatabase.getInstance().getReference("users")

        userPPImageView = findViewById(R.id.userPP)
        userNameTextView = findViewById(R.id.userName)
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
            val usernameToDelete = userNameTextView.text.toString()
            showDeleteConfirmationDialog(usernameToDelete)
        }

        val saveButton = findViewById<Button>(R.id.saveUserButton)
        saveButton.setOnClickListener {
            val username = userNameTextView.text.toString()
            val displayname = userDisnameEditText.text.toString()
            val number = userNumEditText.text.toString()

            if (isPhoneNumberValid(number) && displayname.isNotEmpty() && username.isNotEmpty()) {
                saveUserData(username, displayname, number)
            } else {
                if (!isPhoneNumberValid(number)) {
                    Toast.makeText(
                        this@AdminUserActivity,
                        "Phone number can only contain numbers and must be 10-11 digits long",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@AdminUserActivity,
                        "Please fill in all fields with valid data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }



    private fun saveUserData(username: String, displayname: String, number: String) {
        val userReference = database.child(username)

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    dataSnapshot.ref.child("displayname").setValue(displayname)
                    dataSnapshot.ref.child("number").setValue(number)
                    Toast.makeText(this@AdminUserActivity, "User data updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    val userData = UserData(username, displayname, number)

                    userReference.setValue(userData)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this@AdminUserActivity, "User data saved successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@AdminUserActivity, "Failed to save user data", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun searchUserInDatabase(username: String) {
        val query: Query = database.orderByChild("username").equalTo(username.trim())

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (userSnapshot in dataSnapshot.children) {
                        val user = userSnapshot.getValue(UserData::class.java)
                        if (user != null) {
                            Picasso.get().load(user.imageUri).into(userPPImageView)

                            userNameTextView.text = user.username
                            userDisnameEditText.setText(user.displayname)
                            userNumEditText.setText(user.number)
                            return
                        }
                    }
                } else {
                    Toast.makeText(this@AdminUserActivity, "User not found", Toast.LENGTH_SHORT).show()

                    userPPImageView.setImageResource(R.drawable.avatar)
                    userNameTextView.text = ""
                    userDisnameEditText.text.clear()
                    userNumEditText.text.clear()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error if needed
            }
        })
    }

    private fun deleteUserData(usernameToDelete: String) {
        if (usernameToDelete.isNotEmpty()) {
            val userReference = database.child(usernameToDelete)

            userReference.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@AdminUserActivity, "User data deleted successfully", Toast.LENGTH_SHORT).show()

                    userPPImageView.setImageResource(R.drawable.avatar)
                    userNameTextView.text = ""
                    userDisnameEditText.text.clear()
                    userNumEditText.text.clear()
                } else {
                    Toast.makeText(this@AdminUserActivity, "Failed to delete user data", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this@AdminUserActivity, "Username cannot be empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmationDialog(usernameToDelete: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Deletion")
        builder.setMessage("Are you sure you want to delete the user data for $usernameToDelete?")
        builder.setPositiveButton("Yes") { _, _ ->
            deleteUserData(usernameToDelete)
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        deleteConfirmationDialog = builder.create()
        deleteConfirmationDialog.show()
    }

    private fun isPhoneNumberValid(number: String): Boolean {
        return number.matches(Regex("\\d{10,11}"))
    }
}
