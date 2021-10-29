package com.singularity.ipcaplus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.buttonLogin.setOnClickListener {
            val email : String = binding.editTextEmail.text.toString()
            val password : String = binding.editTextTextPassword.text.toString()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        updateUI(user, email)
                        // Sign in success, update UI with the signed-in user's information
                        startActivity(Intent(this@LoginActivity, ChatActivity::class.java ))
                    } else {
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }

        }
    }

    private fun updateUI(currentUser: FirebaseUser?, emailAdd: String) {
        if(currentUser !=null){

            // Below  if statement is added to check if email is verified
            if(currentUser.isEmailVerified){
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("emailAddress", emailAdd);
                startActivity(intent)

            // add finish() function to terminate the Sign In activity
            finish()

            //adding else with toast to display the message if email is not verified
            }else
                Toast.makeText(this,"Email Address Is not Verified. Please verify your email address",Toast.LENGTH_LONG).show()

        }
    }
}