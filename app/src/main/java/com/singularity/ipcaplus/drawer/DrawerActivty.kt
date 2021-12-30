package com.singularity.ipcaplus.drawer

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.WelcomeActivity
import com.singularity.ipcaplus.utils.PreferenceHelper.email
import com.singularity.ipcaplus.utils.PreferenceHelper.password
import com.singularity.ipcaplus.utils.PreferenceHelper.userId
import com.singularity.ipcaplus.chat.SearchActivity
import com.singularity.ipcaplus.cryptography.encryptMeta
import com.singularity.ipcaplus.cryptography.metaGenrateKey
import com.singularity.ipcaplus.databinding.ActivityDrawerActivtyBinding
import com.singularity.ipcaplus.models.Chat
import com.singularity.ipcaplus.models.Message
import com.singularity.ipcaplus.utils.PreferenceHelper
import com.singularity.ipcaplus.utils.UserLoggedIn
import com.singularity.ipcaplus.utils.Utilis
import kotlin.random.Random


class DrawerActivty : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDrawerActivtyBinding

    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDrawerActivtyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.imageView_profile).setOnClickListener {

            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        Utilis.getUserImage(Firebase.auth.uid!!) { bitmap ->
            binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.imageView_profile).setImageBitmap(bitmap)
        }
        if(!UserLoggedIn.name.isNullOrEmpty()){
            val name = Utilis.getFirstAndLastName(UserLoggedIn.name!!)
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.textView3).text = name
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.emailTextView).text = UserLoggedIn.email
        }

        setSupportActionBar(binding.appBarMain.toolbar)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);


        binding.appBarMain.fabAddChat.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home
            ), drawerLayout
        )

        // Log Out Button
        binding.logoutLayout.setOnClickListener {

            val prefs = PreferenceHelper.customPreference(this, "User_data")
            prefs.password = null
            prefs.email = null
            prefs.userId = null

            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }


        // Passing each fragment ID as a set of Ids
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.drawer, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.search_btn -> {
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}