package com.example.mobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.assignment.UserData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class EditActivity : AppCompatActivity() {

    private lateinit var editDisplayname: EditText
    private lateinit var editUsername: TextView
    private lateinit var editNumber: EditText
    private lateinit var database: DatabaseReference
    private lateinit var editImage: ImageView

    private var imageUri: Uri? = null // Initialize imageUri as null

    private lateinit var storageReference: StorageReference

    companion object {
        val IMAGE_REQUEST_CODE = 1_000
    }

    // Register the contract to handle the image selection
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                // Set the selected image to the ImageView
                editImage.setImageURI(uri)
                imageUri = uri
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofile)

        // Initialize views
        editDisplayname = findViewById(R.id.editDisplayname)
        editUsername = findViewById(R.id.editUsername)
        editNumber = findViewById(R.id.editNumber)
        editImage = findViewById(R.id.editImage)

        // Set a click listener for the editImage ImageView
        editImage.setOnClickListener {
            pickImageFromGallery()
        }

        // Retrieve data and populate the views
        val username = intent.getStringExtra("username")

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().getReference("users").child(username.toString())

        // Initialize Firebase Storage reference
        storageReference = FirebaseStorage.getInstance().reference.child("profile_images")

        retrieveUserData(username.toString())

        // Initialize save button
        val saveEdit = findViewById<Button>(R.id.saveEdit)

        saveEdit.setOnClickListener {
            // Get the updated data from EditText fields
            val updatedUsername = editUsername.text.toString()
            val updatedDisplayname = editDisplayname.text.toString()
            val updatedNumber = editNumber.text.toString()

            // Check if any of the text fields have changed
            if (updatedUsername.isNotBlank()) {
                if (updatedDisplayname.isNotBlank()) {
                    if (updatedNumber.isNotBlank() && (updatedNumber.length == 10 || updatedNumber.length == 11)) {
                        // Only update the user data if all fields are valid

                        if (imageUri != null) {
                            // An image is selected, so upload it to Firebase Storage
                            uploadProfileImage(imageUri, updatedUsername)
                        } else {
                            // No image selected, update only textual information in the database
                            updateUserData(updatedUsername, updatedDisplayname, updatedNumber, "")
                        }
                    } else {
                        // Number is invalid (either empty or not 10 or 11 digits)
                        Toast.makeText(
                            this@EditActivity,
                            "Please enter a valid number with 10 or 11 digits",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // Displayname is invalid (empty)
                    Toast.makeText(
                        this@EditActivity,
                        "Display Name cannot be blank",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // Username is invalid (empty)
                Toast.makeText(this@EditActivity, "Username cannot be blank", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

        private fun retrieveUserData(username: String) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userData = snapshot.getValue(UserData::class.java)
                    val retrievedDisplayName = snapshot.child("displayname").value.toString()
                    val retrievedUsername = snapshot.child("username").value.toString()
                    val retrievedPhoneNumber = snapshot.child("number").value.toString()
                    val retrievedProfilePic = snapshot.child("imageUri").value.toString()

                    // Display data in TextViews
                    editDisplayname.text =
                        Editable.Factory.getInstance().newEditable(retrievedDisplayName)
                    editUsername.text = retrievedUsername
                    editNumber.text =
                        Editable.Factory.getInstance().newEditable(retrievedPhoneNumber)

                    // Load the user's profile picture using Picasso
                    if (!userData?.imageUri.isNullOrEmpty()) {
                        // Load the user's profile picture using Picasso with a unique query parameter
                        Picasso.get().load(userData?.imageUri + "?timestamp=" + System.currentTimeMillis()).into(editImage)
                        editImage.tag = userData?.imageUri // Store the current image URI as a tag
                    } else {
                        // Set the default profile picture from the drawable resources
                        editImage.setImageResource(R.drawable.avatar)
                    }

                    // Set the imageUri variable to the retrieved profile picture URL
                    imageUri = Uri.parse(retrievedProfilePic) // Convert the URL to Uri
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUserData(username: String, displayName: String, phoneNumber: String, imageUri: String) {
        val userData = HashMap<String, Any>()
        userData["username"] = username
        userData["displayname"] = displayName
        userData["number"] = phoneNumber
        userData["imageUri"] = imageUri // Store the image URI

        // Update the user data in the database
        database.updateChildren(userData)
            .addOnSuccessListener {
                // Start the ProfileActivity and pass the updated image URL as an extra
                val profileIntent = Intent(this@EditActivity, ProfileActivity::class.java)
                profileIntent.putExtra("username", username)
                profileIntent.putExtra("imageUri", imageUri) // Pass the updated image URL
                startActivity(profileIntent)

                Toast.makeText(this@EditActivity, "Profile Updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this@EditActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadProfileImage(imageUri: Uri?, username: String) {
        if (imageUri != null) {
            val storageRef = storageReference.child("profile_images").child("$username.jpg")

            val uploadTask = storageRef.putFile(imageUri)
            uploadTask.addOnSuccessListener {
                // Image uploaded successfully
                // Get the download URL
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString() // This is the HTTPS URL of the uploaded image

                    // Update the user's data with the new image URI (imageUrl)
                    updateUserData(username, editDisplayname.text.toString(), editNumber.text.toString(), imageUrl)

                    Toast.makeText(this@EditActivity, "Profile Image Updated", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this@EditActivity, "Failed to upload profile image", Toast.LENGTH_SHORT).show()
            }
        } else {
            // No image selected, update only textual information in the database
            updateUserData(username, editDisplayname.text.toString(), editNumber.text.toString(), "")
        }
    }

    private fun pickImageFromGallery() {
        // Use the imagePicker contract to select an image from the gallery
        imagePicker.launch("image/*")
    }
}
