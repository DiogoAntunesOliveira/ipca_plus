package com.singularity.ipcaplus

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.PreferenceHelper.customPreference
import com.singularity.ipcaplus.PreferenceHelper.email
import com.singularity.ipcaplus.PreferenceHelper.name
import com.singularity.ipcaplus.PreferenceHelper.password
import com.singularity.ipcaplus.PreferenceHelper.userId
import com.singularity.ipcaplus.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    val VALID_DATA = "User_data"

    private lateinit var email_save : EditText
    private lateinit var password_save : EditText



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email_save = binding.editTextEmail
        password_save = binding.editTextTextPassword

        auth = Firebase.auth
        val prefs = customPreference(this, VALID_DATA)

        UserLoggedIn.id = prefs.getString("USER_ID", null)
        UserLoggedIn.name = prefs.getString("USER_NAME", null)
        UserLoggedIn.email = prefs.getString("USER_EMAIL", null)
        UserLoggedIn.password = prefs.getString("USER_PASSWORD", null)

        if (UserLoggedIn.id != null){
            startActivity(Intent(this@LoginActivity, DrawerActivty::class.java ))
        }

        binding.buttonLogin.setOnClickListener {

            var email : String = binding.editTextEmail.text.toString()
            var password : String = binding.editTextTextPassword.text.toString()


            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        updateUI(user, email)
                        prefs.password = binding.editTextTextPassword.text.toString()
                        prefs.email = binding.editTextEmail.text.toString()
                        prefs.userId = auth.currentUser?.uid

                        Backend.getUserProfile(Firebase.auth.uid!!) {
                            prefs.name = it.name

                            // Sign in success, update UI with the signed-in user's information
                            startActivity(Intent(this@LoginActivity, DrawerActivty::class.java ))
                        }


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
                val intent = Intent(this, DrawerActivty::class.java)
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