package com.singularity.ipcaplus.drawer

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import kotlin.random.Random
import androidx.core.view.ViewCompat

import androidx.core.app.ActivityOptionsCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.singularity.ipcaplus.AddButtonActivity
import com.singularity.ipcaplus.chat.CreateChatActivity
import com.singularity.ipcaplus.databinding.ActivityProfileBinding
import com.singularity.ipcaplus.models.EventCalendar
import com.singularity.ipcaplus.utils.*
import com.singularity.ipcaplus.utils.Backend.getAllTokens
import com.singularity.ipcaplus.utils.Backend.postTokenAddress


class DrawerActivty : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDrawerActivtyBinding
    private lateinit var binding2: ActivityProfileBinding
    val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDrawerActivtyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.imageView_profile).setOnClickListener {

            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("userId", Firebase.auth.currentUser!!.uid)
            startActivity(intent)
        }

        Utilis.getFile(this,"profilePictures/" + Firebase.auth.uid + ".png", "png") { bitmap ->
            binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.imageView_profile).setImageBitmap(bitmap)
        }
        if(!UserLoggedIn.name.isNullOrEmpty()){
            val name = Utilis.getFirstAndLastName(UserLoggedIn.name!!)
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.textView3).text = name
            binding.navView.getHeaderView(0).findViewById<TextView>(R.id.emailTextView).text = UserLoggedIn.email
        }

        // Getting device FCM token
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(ContentValues.TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            var fcmToken = task.result
            
            // Posting FCM token address on firebase
            postTokenAddress(fcmToken.toString(), Firebase.auth.currentUser!!.uid)
        })



        setSupportActionBar(binding.appBarMain.toolbar)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN )

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

        // Generate key for chats
        val keygen = metaGenrateKey()

        // Criação de Chat
        binding.appBarMain.fabAddChat.setOnClickListener {

            val intent = Intent(this, AddButtonActivity::class.java)
            startActivity(intent)

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

        // Vou buscar os tokens do utilizador
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
        })

        /*FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(ContentValues.TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            var fcmToken = task.result

            // Log and toast
            Log.d(ContentValues.TAG, "O FCM é $fcmToken")
            Toast.makeText(this, "O FCM é $fcmToken", Toast.LENGTH_SHORT).show()
        })

        db.collection("profile").document(Firebase.auth.currentUser!!.uid).collection("tokens")
            .add(message.toHash())
            .addOnSuccessListener { documentReference ->
                Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")

            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
            }

        binding.editTextMessage.text.clear()*/

    }


    /*
        Refresh Activity Content
    */
    override fun onResume() {
        super.onResume()

        Utilis.getFile(this,"profilePictures/" + Firebase.auth.uid!! + ".png", "png") { bitmap ->
            binding.navView.getHeaderView(0).findViewById<ImageView>(R.id.imageView_profile).setImageBitmap(bitmap)
        }
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