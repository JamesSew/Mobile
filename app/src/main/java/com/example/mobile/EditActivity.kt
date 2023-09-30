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
import java.util.regex.Pattern

class EditActivity : AppCompatActivity() {

    private lateinit var editDisplayname: EditText
    private lateinit var editUsername: TextView
    private lateinit var editNumber: EditText
    private lateinit var database: DatabaseReference
    private lateinit var editImage: ImageView

    private var imageUri: Uri? = null

    private lateinit var storageReference: StorageReference

    companion object {
        val IMAGE_REQUEST_CODE = 1_000
    }

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                editImage.setImageURI(uri)
                imageUri = uri
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editprofile)

        editDisplayname = findViewById(R.id.editDisplayname)
        editUsername = findViewById(R.id.editUsername)
        editNumber = findViewById(R.id.editNumber)
        editImage = findViewById(R.id.editImage)

        editImage.setOnClickListener {
            pickImageFromGallery()
        }

        val username = intent.getStringExtra("username")

        database = FirebaseDatabase.getInstance().getReference("users").child(username.toString())
        storageReference = FirebaseStorage.getInstance().reference.child("profile_images")

        retrieveUserData(username.toString())

        val saveEdit = findViewById<Button>(R.id.saveEdit)

        saveEdit.setOnClickListener {
            val updatedUsername = editUsername.text.toString()
            val updatedDisplayname = editDisplayname.text.toString()
            val updatedNumber = editNumber.text.toString()

            if (isPhoneNumberValid(updatedNumber) && updatedDisplayname.isNotBlank() && updatedUsername.isNotBlank()) {
                if (imageUri != null) {
                    uploadProfileImage(imageUri, updatedUsername)
                } else {
                    updateUserData(updatedUsername, updatedDisplayname, updatedNumber, "")
                }
            } else {
                Toast.makeText(
                    this@EditActivity,
                    "Please enter valid information. Phone number should be 10 or 11 digits long and contain only digits.",
                    Toast.LENGTH_SHORT
                ).show()
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

                    editDisplayname.text =
                        Editable.Factory.getInstance().newEditable(retrievedDisplayName)
                    editUsername.text = retrievedUsername
                    editNumber.text =
                        Editable.Factory.getInstance().newEditable(retrievedPhoneNumber)

                    if (!userData?.imageUri.isNullOrEmpty()) {
                        Picasso.get()
                            .load(userData?.imageUri + "?timestamp=" + System.currentTimeMillis()).into(editImage)
                        editImage.tag = userData?.imageUri
                    } else {
                        editImage.setImageResource(R.drawable.avatar)
                    }

                    imageUri = Uri.parse(retrievedProfilePic)
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
        userData["imageUri"] = imageUri

        database.updateChildren(userData)
            .addOnSuccessListener {
                val profileIntent = Intent(this@EditActivity, ProfileActivity::class.java)
                profileIntent.putExtra("username", username)
                profileIntent.putExtra("imageUri", imageUri)
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
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()

                    updateUserData(username, editDisplayname.text.toString(), editNumber.text.toString(), imageUrl)

                    Toast.makeText(this@EditActivity, "Profile Image Updated", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this@EditActivity, "Failed to upload profile image", Toast.LENGTH_SHORT).show()
            }
        } else {
            updateUserData(username, editDisplayname.text.toString(), editNumber.text.toString(), "")
        }
    }

    private fun pickImageFromGallery() {
        imagePicker.launch("image/*")
    }

    private fun isPhoneNumberValid(phoneNumber: String): Boolean {
        val phonePattern = Pattern.compile("^[0-9]{10,11}$")
        val matcher = phonePattern.matcher(phoneNumber)
        return matcher.matches()
    }
}