package com.example.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        val viewProfileButton = findViewById<Button>(R.id.viewprofile)
        val adminButton = findViewById<Button>(R.id.adminButton)

        viewProfileButton.setOnClickListener {


            val username = intent.getStringExtra("username")


            val profileIntent = Intent(this@HomeActivity, ProfileActivity::class.java)
            profileIntent.putExtra("username", username)
            startActivity(profileIntent)

        }

        adminButton.setOnClickListener {
            val username = intent.getStringExtra("username")

            val AdminIntent = Intent(this@HomeActivity, AdminUserActivity::class.java)
            AdminIntent.putExtra("username", username)
            startActivity(AdminIntent)
        }
    }
}
