package com.singularity.ipcaplus

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.singularity.ipcaplus.databinding.ActivityContactsBinding
import com.singularity.ipcaplus.databinding.ActivityTermsAndCondictionsBinding

class TermsAndCondictionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTermsAndCondictionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_and_condictions)

        // Create the layout for this fragment
        binding = ActivityTermsAndCondictionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create Action Bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)
        supportActionBar?.title = "Voltar"

    }

    // When the support action bar back button is pressed, the app will go back to the previous activity
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}