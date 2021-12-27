package com.singularity.ipcaplus

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
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
import com.singularity.ipcaplus.PreferenceHelper.email
import com.singularity.ipcaplus.PreferenceHelper.password
import com.singularity.ipcaplus.PreferenceHelper.userId
import com.singularity.ipcaplus.calendar.AddEventActivity
import com.singularity.ipcaplus.databinding.ActivityDrawerActivtyBinding
import com.singularity.ipcaplus.models.Chat
import com.singularity.ipcaplus.models.Message
import com.singularity.ipcaplus.profile.ProfileActivity
import java.util.concurrent.TimeUnit
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

        val name = Utilis.getFirstAndLastName(UserLoggedIn.name!!)
        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.textView3).text = name
        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.emailTextView).text = UserLoggedIn.email

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
        val stampCurrent = System.currentTimeMillis()
        val stampSec = TimeUnit.MILLISECONDS.toSeconds(stampCurrent)
        val stampNano = TimeUnit.MILLISECONDS.toNanos(stampCurrent).toInt()
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home
            ), drawerLayout
        )

        // Criação de Chat
        binding.appBarMain.fabAddChat.setOnClickListener {
            val chat = Chat(
                "Chat Teste " + Random.nextInt(256),
                "chat"

            )
            val message = Message(
                "system",
                "This is an Alpha Chat, bugs are expected, please report them if you found some. Welcome to Singularity!",
                "2021-12-22",
                Timestamp.now(),
                ""

            )
            db.collection("chat")
                .add(chat.toHash())
                .addOnSuccessListener { documentReference ->
                    db.collection("chat")
                        .document("${documentReference.id}")
                        .collection("message")
                        .add(message.toHash())
                    db.collection("profile")
                        .document("${Firebase.auth.currentUser!!.uid}")
                        .collection("chat")
                        .document("${documentReference.id}")
                        .set(chat)

                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error adding document", e)
                }

        }

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