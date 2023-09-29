package com.example.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.assignment.UserData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var profileNameTextView: TextView
    private lateinit var profileUsernameTextView: TextView
    private lateinit var profileNumberTextView: TextView
    private lateinit var profileImage: ImageView
    private lateinit var editProfileImageView: ImageView
    private lateinit var signOutImageView: ImageView
    private lateinit var changepassTextView: TextView
    private lateinit var imageUri: String // Variable to store the imageUri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_profile)

        profileNameTextView = findViewById(R.id.profiledisplayname)
        profileUsernameTextView = findViewById(R.id.profileusername)
        profileNumberTextView = findViewById(R.id.profilenumber)
        profileImage = findViewById(R.id.profilepic)
        editProfileImageView = findViewById(R.id.editProfile)
        signOutImageView = findViewById(R.id.signOut)
        changepassTextView = findViewById(R.id.changePass)

        val username = intent.getStringExtra("username")
        imageUri = intent.getStringExtra("imageUri") ?: "" // Get the imageUri from the intent or set it as an empty string if null

        if (!username.isNullOrBlank()) {
            database = FirebaseDatabase.getInstance().getReference("users").child(username)
            readUserData()
        } else {
            // Handle the case of an invalid username as needed
        }

        editProfileImageView.setOnClickListener {
            val editIntent = Intent(this@ProfileActivity, EditActivity::class.java)
            editIntent.putExtra("username", username)
            startActivity(editIntent)
        }

        signOutImageView.setOnClickListener{
            val signOut = Intent(this@ProfileActivity, LoginActivity::class.java)
            startActivity(signOut)
        }

        changepassTextView.setOnClickListener {
            val changepassIntent = Intent(this@ProfileActivity, OriginalPWActivity::class.java)
            changepassIntent.putExtra("username", username)
            startActivity(changepassIntent)
        }
    }

    private fun readUserData() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userData = snapshot.getValue(UserData::class.java)

                    if (userData != null) {
                        profileNameTextView.text = userData.displayname
                        profileUsernameTextView.text = userData.username
                        profileNumberTextView.text = userData.number

                        // Load the user's profile picture using Picasso from the imageUri in the intent
                        if (!imageUri.isNullOrEmpty()) {
                            Picasso.get().load(imageUri + "?timestamp=" + System.currentTimeMillis()).into(profileImage)
                        } else if (!userData.imageUri.isNullOrEmpty()) {
                            // If no imageUri is available in the intent, load the image from the database
                            Picasso.get().load(userData.imageUri + "?timestamp=" + System.currentTimeMillis()).into(profileImage)
                        } else {
                            // Set the default profile picture from the drawable resources
                            profileImage.setImageResource(R.drawable.avatar)
                        }
                    } else {
                        // Handle the case of null profile data as needed
                    }
                } else {
                    // Handle the case of a profile not found as needed
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the database error as needed
            }
        })
    }
}
