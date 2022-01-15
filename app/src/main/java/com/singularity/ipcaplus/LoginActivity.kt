package com.singularity.ipcaplus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.utils.PreferenceHelper.customPreference
import com.singularity.ipcaplus.utils.PreferenceHelper.email
import com.singularity.ipcaplus.utils.PreferenceHelper.name
import com.singularity.ipcaplus.utils.PreferenceHelper.password
import com.singularity.ipcaplus.utils.PreferenceHelper.userId
import com.singularity.ipcaplus.databinding.ActivityLoginBinding
import com.singularity.ipcaplus.drawer.DrawerActivty
import com.singularity.ipcaplus.utils.Backend
import com.singularity.ipcaplus.utils.PreferenceHelper.role
import com.singularity.ipcaplus.utils.UserLoggedIn
import com.singularity.ipcaplus.utils.UserLoggedIn.email

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

        // Create Action Bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)
        supportActionBar?.title = "Voltar"

        email_save = binding.editTextEmail
        password_save = binding.editTextTextPassword

        auth = Firebase.auth

        val prefs = customPreference(this, VALID_DATA)

        UserLoggedIn.id = prefs.getString("USER_ID", null)
        UserLoggedIn.name = prefs.getString("USER_NAME", null)
        UserLoggedIn.email = prefs.getString("USER_EMAIL", null)
        UserLoggedIn.role = prefs.getString("USER_ROLE", null)
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
                        prefs.password = binding.editTextTextPassword.text.toString()
                        prefs.email = binding.editTextEmail.text.toString()
                        prefs.userId = auth.currentUser?.uid

                        Backend.getUserProfile(Firebase.auth.uid!!) {
                            prefs.name = it.name
                            prefs.role = it.role

                            // Sign in success, update UI with the signed-in user's information
                            updateUI(user, email)
                        }


                    } else {
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }

        }

        binding.textViewForgotPassword.setOnClickListener {
            println("-----------> forgot password")
            openResetPasswordDialog()
        }

        binding.registerLinearLayout.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java ))
        }
    }

    /*
        This function display a dialog window with a text box to send reset password email
     */
    private fun openResetPasswordDialog() {

        val alertDialog = AlertDialog.Builder(this)

        val row = layoutInflater.inflate(R.layout.dialog_select_name, null)
        alertDialog.setView(row)
        val show = alertDialog.show()

        // Variables
        val editTextView = row.findViewById<TextView>(R.id.editTextName)
        row.findViewById<TextView>(R.id.textView).text = "Email de recuperação"
        editTextView.hint = "Insira email de recuperação"
        row.findViewById<Button>(R.id.buttonSave).text = "Enviar"

        row.findViewById<Button>(R.id.buttonSave).setOnClickListener {
            val mAuth = FirebaseAuth.getInstance()
            val value = editTextView.text.toString()
            mAuth.sendPasswordResetEmail(value)
            show.dismiss()
        }

    }

    // When the support action bar back button is pressed, the app will go back to the previous activity
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
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