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
import com.singularity.ipcaplus.models.Chat
import com.singularity.ipcaplus.models.Profile
import com.singularity.ipcaplus.utils.Backend
import com.singularity.ipcaplus.utils.Utilis
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityRegisterBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pattern: Pattern =
            Pattern.compile("^" +
                    "(?=.*[@#$%^&+=£.!,#])" +
                    "(?=.*[A-Z])" +
                    "(?=\\S+$)" +
                    ".{8,}" +
                    "$")

        // Create Action Bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)
        supportActionBar?.title = "Voltar"

        auth = Firebase.auth


        binding.buttonRegister.setOnClickListener {
            if (!binding.editTextTextPassword.text.isNullOrBlank() && !binding.editTextEmail.text.isNullOrBlank()
                && pattern.matcher(binding.editTextTextPassword.text.toString()).matches()
            ) {

                if (binding.editTextTextPassword.text.toString() == binding.editTextTextConfirmPassword.text.toString()) {
                    val email: String = binding.editTextEmail.text.toString()
                    val password: String = binding.editTextTextPassword.text.toString()

                    val emailDomain = Utilis.getEmailDomain(email)
                    if (emailDomain != "alunos.ipca.pt" && emailDomain != "ipca.pt") {
                        Snackbar.make(binding.root,
                            "Precisas de usar um email do ipca (ipca.pt)!", Snackbar.LENGTH_SHORT)
                            .show()
                    } else {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {

                                    emailVerification()

                                    Backend.getIpcaData(email) { profile, ipcaDataId ->

                                        val userID = auth.currentUser!!.uid

                                        // Create profile with ipca data
                                        db.collection("profile")
                                            .document(userID)
                                            .set(profile!!.toHash())
                                            .addOnCompleteListener {

                                                if (profile.role != "Professor") {

                                                    // Get user course in ipca data and create a collection with that document
                                                    Backend.setUserCourseByIpcaData(userID,
                                                        ipcaDataId) {

                                                        // Create official chats for each subject
                                                        Backend.getOficialChatByTag(it) { chats ->

                                                            Backend.setOficialChat(userID, chats)
                                                        }

                                                    }

                                                } else {
                                                    Backend.setTeacherSubjectsByIpcaData(userID,
                                                        ipcaDataId)
                                                }

                                            }


                                    }

                                    startActivity(Intent(this, LoginActivity::class.java))
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(ContentValues.TAG,
                                        "createUserWithEmail:failure",
                                        task.exception)
                                    Toast.makeText(baseContext, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    Snackbar.make(binding.root,
                        "Tens de confirmar a Password corretamente!", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(baseContext, "Palavra-passe precisa conter:\n" +
                        "- Pelo menos um caracter especial\n" +
                        "- Pelo menos uma maiuscula\n" +
                        "- Minimo de 8 letras",
                    Toast.LENGTH_LONG).show()
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


    // When the support action bar back button is pressed, the app will go back to the previous activity
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}