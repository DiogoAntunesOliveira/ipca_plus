package com.singularity.ipcaplus.drawer

import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.databinding.ActivityProfileBinding
import com.singularity.ipcaplus.models.Profile
import com.singularity.ipcaplus.*
import com.singularity.ipcaplus.utils.ActivityImageHelper
import com.singularity.ipcaplus.utils.Backend
import com.singularity.ipcaplus.utils.UserLoggedIn
import com.singularity.ipcaplus.utils.Utilis
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream


class ProfileActivity : ActivityImageHelper() {

    // Variables
    private var imageUri: Uri? = null
    private lateinit var binding: ActivityProfileBinding
    lateinit var profileData: Profile
    lateinit var contextInfo: Context

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        contextInfo = applicationContext

        // Variables
        var userId = intent.getStringExtra("userId").toString()


        // Create the layout for this fragment
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Create Action Bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)
        supportActionBar?.title = "Perfil"
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.custom_bar_layout)


        // Get profile
        Backend.getUserProfile(userId) {
            profileData = it

            // Set data
            binding.textViewName1.text = Utilis.getFirstAndLastName(profileData.name)
            binding.textViewEmail.text = UserLoggedIn.email
            binding.textViewFullName.text = profileData.name
            binding.textViewRole.text = profileData.role
            binding.textViewAge.text = profileData.age

            if (profileData.role != "Professor") {

                Backend.db.collection("profile")
                    .document(profileData.id!!)
                    .collection("course")
                    .addSnapshotListener { documents, _ ->

                        documents?.let {

                            for (document in documents) {
                                binding.textViewCourse.text = document.data["name"] as String
                            }
                        }
                    }

            } else {
                binding.textViewCourse.visibility = View.GONE
                binding.textViewCourseTitle.visibility = View.GONE
            }

            //binding.textViewCourse.text = profileData.course

            Utilis.getFile(this, "profilePictures/" + userId + ".png", "png") { bitmap ->
                binding.imageViewProfile.setImageBitmap(bitmap)
            }
        }

        if (userId == Firebase.auth.currentUser!!.uid) {
            // Change Profile Picture
            binding.imageViewProfile.setOnClickListener {
                checkPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    STORAGE_PERMISSION_CODE)
            }
        }

        // Back button
        findViewById<ImageView>(R.id.BackButtonImageView).setOnClickListener {
            finish()
        }

    }

    /*
       This function happen after picking photo, and make changes in the activity
    */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.activity(data?.data)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this)
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                binding.imageViewProfile.setImageURI(result.uri)
                imageUri = result.uri

                CoroutineScope(Dispatchers.IO).launch {
                    val userId = FirebaseAuth.getInstance().currentUser!!.uid
                    val storageRef =
                        FirebaseStorage.getInstance().getReference("profilePictures/${userId}.png")

                    // compressing image
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 6, byteArrayOutputStream)
                    val reducedImage: ByteArray = byteArrayOutputStream.toByteArray()

                    storageRef.putBytes(reducedImage)
                        .addOnSuccessListener {

                            Log.i("xxx", "Success uploading Image to Firebase!!!")

                            storageRef.downloadUrl.addOnSuccessListener {

                                //getting image url
                                Log.i("xxx", it.toString())
                                Utilis.uploadFile(it,
                                    "profilePictures/" + Firebase.auth.uid!! + ".png")

                            }.addOnFailureListener {

                                Log.i("xxx", "Error getting image download url")
                            }

                        }.addOnFailureListener {

                            Log.i("xxx", "Failed uploading image to server")

                        }
                }

            }
        }
    }

    // When the support action bar back button is pressed, the app will go back to the previous activity
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}