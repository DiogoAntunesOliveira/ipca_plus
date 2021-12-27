package com.singularity.ipcaplus.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.Backend
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.UserLoggedIn
import com.singularity.ipcaplus.databinding.ActivityProfileBinding
import com.singularity.ipcaplus.models.Profile

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    lateinit var profileData: Profile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Create the layout for this fragment
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create Action Bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)
        supportActionBar?.title = "Perfil"

        // Get profile
        Backend.getUserProfile(Firebase.auth.uid!!) {
            profileData = it

            // Set data
            binding.textViewName1.text = profileData.name
            binding.textViewEmail.text = UserLoggedIn.email
            binding.textViewFullName.text = profileData.name
            binding.textViewRole.text = profileData.role
            binding.textViewAge.text = profileData.age
            binding.textViewCourse.text = profileData.course
        }

    }

    // When the support action bar back button is pressed, the app will go back to the previous activity
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}