package com.singularity.ipcaplus.chat

import android.app.ActivityOptions
import android.content.Intent
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.WelcomeActivity
import com.singularity.ipcaplus.databinding.ActivityChatMoreBinding
import com.singularity.ipcaplus.drawer.DrawerActivty
import com.singularity.ipcaplus.utils.ActivityImageHelper
import com.singularity.ipcaplus.utils.Backend
import com.singularity.ipcaplus.utils.Utilis
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

class ChatMoreActivity : ActivityImageHelper() {

    lateinit var imageViewGroup: ImageView
    lateinit var imageViewDialog: ImageView
    lateinit var chat_id: String
    private lateinit var binding: ActivityChatMoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_more)

        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_stay)

        // Create the layout for this fragment
        binding = ActivityChatMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create Action Bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)
        supportActionBar?.title = "Definições do grupo"

        // Get previous data
        chat_id = intent.getStringExtra("chat_id").toString()
        val chat_name = intent.getStringExtra("chat_name").toString()
        binding.textViewGroupName.text = chat_name

        // Get Group Image
        imageViewGroup = binding.imageViewGroup
        Utilis.getFile("chats/$chat_id/icon.png", "png") { bitmap ->
            imageViewGroup.setImageBitmap(bitmap)
        }

        binding.seeGroupMembers.setOnClickListener {
            val intent = Intent(this, ChatMembersActivity::class.java)
            intent.putExtra("chat_id", chat_id)
            startActivity(intent)
        }

        binding.changeGroupName.setOnClickListener {
            openSelectNameDialog()
        }

        binding.changeGroupImage.setOnClickListener {
            openSelectImageDialog()
        }

        binding.groupFiles.setOnClickListener {
            val intent = Intent(this, ChatFilesActivity::class.java)
            intent.putExtra("chat_id", chat_id)
            startActivity(intent)
        }

        binding.notifications.setOnClickListener {
            println("-------------> 5")
        }

        binding.securityNumberVerification.setOnClickListener {
            println("-------------> 6")
            val intent = Intent(this, VerifySecurityNumberActivity::class.java )
            intent.putExtra("chat_id", chat_id)
            startActivity(intent)

        }
    }

    // When the support action bar back button is pressed, the app will go back to the previous activity
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    /*
        This function display a dialog window with a text box to edit chat name
     */
    private fun openSelectImageDialog() {

        // Variables
        val alertDialog = AlertDialog.Builder(this)
        val row = layoutInflater.inflate(R.layout.dialog_select_image, null)
        alertDialog.setView(row)
        val show = alertDialog.show()
        imageViewDialog = row.findViewById(R.id.imageViewGroup)

        Utilis.getFile("chats/$chat_id/icon.png", "png") { bitmap ->
            row.findViewById<ImageView>(R.id.imageViewGroup).setImageBitmap(bitmap)
        }

        imageViewDialog.setOnClickListener {
            checkPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE)
            show.dismiss()
        }

    }


    /*
        This function display a dialog window with a text box to edit chat name
     */
    private fun openSelectNameDialog() {

        // Variables
        val alertDialog = AlertDialog.Builder(this)
        val row = layoutInflater.inflate(R.layout.dialog_select_name, null)
        alertDialog.setView(row)
        val show = alertDialog.show()

        row.findViewById<EditText>(R.id.editTextName).setText(binding.textViewGroupName.text.toString())

        row.findViewById<Button>(R.id.buttonSave).setOnClickListener {
            val newName = row.findViewById<EditText>(R.id.editTextName).text.toString()
            Backend.changeChatName(chat_id, newName)
            show.dismiss()
            binding.textViewGroupName.text = newName
        }

    }


    /*
       This function happen after picking photo, and make changes in the activity
    */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.activity(data?.data)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this)
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                imageViewGroup.setImageURI(result.uri)
                Utilis.uploadFile(result.uri, "chats/$chat_id/icon.png")
            }
        }
    }
}