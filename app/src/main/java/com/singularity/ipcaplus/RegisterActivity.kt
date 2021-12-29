package com.singularity.ipcaplus

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.utils.Backend.db
import com.singularity.ipcaplus.databinding.ActivityRegisterBinding
import com.singularity.ipcaplus.models.Profile
import com.singularity.ipcaplus.utils.Backend
import com.singularity.ipcaplus.utils.Utilis

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityRegisterBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth


        binding.buttonRegister.setOnClickListener {
            val email : String = binding.editTextEmail.text.toString()
            val password : String = binding.editTextTextPassword.text.toString()

            val emailDomain = Utilis.getEmailDomain(email)
            if(emailDomain != "alunos.ipca.pt" && emailDomain != "ipca.pt"){
                Snackbar.make(binding.root,
                    "You need to Sign Up with (ipca.pt) email!", Snackbar.LENGTH_SHORT).show()
            }else{
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {

                            emailVerification()

                            Backend.getIpcaData(email){

                                val profile = it

                                val userID = auth.currentUser!!.uid

                                db.collection("profile")
                                    .document(userID)
                                    .set(profile!!.toHash())

                                // Find Course Based on course_tag and create a collection with that document
                                Backend.getUserCoursesIds(userID, profile.courseTag) { list ->
                                    for (courseID in list)
                                        Backend.setUserCourses(userID, courseID)
                                }

                            }

                            startActivity(Intent(this, LoginActivity::class.java ))
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                            Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun emailVerification() {

        auth.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // below message changed and user is navigated to Sign In activity
                    Toast.makeText(
                        this, "Sign Up successful. Verification link sent to the Email address",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

    }
}