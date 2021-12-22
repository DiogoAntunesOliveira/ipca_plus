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
import com.singularity.ipcaplus.LoginActivity.PreferenceHelper.customPreference
import com.singularity.ipcaplus.LoginActivity.PreferenceHelper.email
import com.singularity.ipcaplus.LoginActivity.PreferenceHelper.password
import com.singularity.ipcaplus.LoginActivity.PreferenceHelper.userId
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

        if (!prefs.email.isNullOrEmpty() || !prefs.email.isNullOrBlank()){
            startActivity(Intent(this@LoginActivity, DrawerActivty::class.java ))
        }

        email_save.setText(prefs.email.toString())
        password_save.setText(prefs.password.toString())

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

                        // Sign in success, update UI with the signed-in user's information
                        startActivity(Intent(this@LoginActivity, DrawerActivty::class.java ))
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

    object PreferenceHelper {

        val USER_ID = "USER_ID"
        val USER_EMAIL = "USER_EMAIL"
        val USER_PASSWORD = "PASSWORD"

        fun defaultPreference(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        fun customPreference(context: Context, name: String): SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

        inline fun SharedPreferences.editMe(operation: (SharedPreferences.Editor) -> Unit) {
            val editMe = edit()
            operation(editMe)
            editMe.apply()
        }

        var SharedPreferences.userId
            get() = getInt(USER_ID, 0)
            set(value) {
                editMe {
                    it.putInt(USER_ID, value)
                }
            }

        var SharedPreferences.email
            get() = getString(USER_EMAIL, "")
            set(value) {
                editMe {
                    it.putString(USER_EMAIL, value)
                }
            }

        var SharedPreferences.password
            get() = getString(USER_PASSWORD, "")
            set(value) {
                editMe {
                    it.putString(USER_PASSWORD, value)
                }
            }

        var SharedPreferences.clearValues
            get() = { }
            set(value) {
                editMe {
                    it.clear()
                }
            }
    }
}