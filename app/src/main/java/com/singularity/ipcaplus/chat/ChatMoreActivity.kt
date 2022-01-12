package com.singularity.ipcaplus.chat

import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.singularity.ipcaplus.LoginActivity
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.databinding.ActivityChatMoreBinding
import com.singularity.ipcaplus.drawer.DrawerActivty
import com.singularity.ipcaplus.utils.ActivityImageHelper
import com.singularity.ipcaplus.utils.Backend
import com.singularity.ipcaplus.utils.Utilis
import com.singularity.ipcaplus.utils.Utilis.calculateInSampleSize
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.ByteArrayOutputStream

class ChatMoreActivity : ActivityImageHelper() {

    lateinit var imageViewGroup: ImageView
    lateinit var imageViewDialog: ImageView
    lateinit var chat_id: String
    var is_admin: Boolean = false
    private lateinit var binding: ActivityChatMoreBinding

    @SuppressLint("WrongConstant")
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
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.custom_bar_layout)
        findViewById<TextView>(R.id.AppBarTittle).text = "Definições de grupo"
        // Back button
        findViewById<ImageView>(R.id.BackButtonImageView).setOnClickListener{
            finish()
        }

        // Get previous data
        chat_id = intent.getStringExtra("chat_id").toString()
        val chat_name = intent.getStringExtra("chat_name").toString()
        is_admin = intent.getBooleanExtra("is_admin", false)
        binding.textViewGroupName.text = chat_name

        // Get Group Image
        imageViewGroup = binding.imageViewGroup
        Utilis.getFile(this, "chats/$chat_id/icon.png", "png") { bitmap ->
            imageViewGroup.setImageBitmap(bitmap)
        }

        binding.seeGroupMembers.setOnClickListener {
            val intent = Intent(this, ChatMembersActivity::class.java)
            intent.putExtra("chat_id", chat_id)
            intent.putExtra("is_admin", is_admin)
            startActivity(intent)
        }

        if (is_admin) {
            binding.changeGroupName.setOnClickListener {
                openSelectNameDialog()
            }

            binding.changeGroupImage.setOnClickListener {
                openSelectImageDialog()
            }
            binding.deleteChat.setOnClickListener {
                Backend.deleteChat(chat_id) {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
            }
        }
        else {
            binding.changeGroupName.visibility = View.GONE
            binding.changeGroupImage.visibility = View.GONE
            binding.deleteChat.visibility = View.GONE
        }

        binding.groupFiles.setOnClickListener {
            val intent = Intent(this, ChatFilesActivity::class.java)
            intent.putExtra("chat_id", chat_id)
            startActivity(intent)
        }

        binding.notifications.setOnClickListener {
            val dialog = BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme)
            val view = layoutInflater.inflate(R.layout.dialog_notifications_manager, null)
            dialog.setContentView(view)
            dialog.show()
        }

        binding.securityNumberVerification.setOnClickListener {
            val intent = Intent(this, VerifySecurityNumberActivity::class.java )
            intent.putExtra("chat_id", chat_id)
            startActivity(intent)
        }

        binding.leaveGroup.setOnClickListener {

            Backend.removeUserFromChat(chat_id, Firebase.auth.currentUser!!.uid)

            val intent = Intent(this, LoginActivity::class.java)
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
        val dialog = BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.dialog_select_image, null)
        dialog.setContentView(view)
        dialog.show()
        imageViewDialog = view.findViewById(R.id.imageViewChatPhoto)


        Utilis.getFile(this,"chats/$chat_id/icon.png", "png") { bitmap ->

            view.findViewById<ImageView>(R.id.imageViewChatPhoto).setImageBitmap(bitmap)
        }

        imageViewDialog.setOnClickListener {
            checkPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE)
            dialog.dismiss()
        }

    }


    /*
        This function display a dialog window with a text box to edit chat name
     */
    private fun openSelectNameDialog() {

        // Variables
        val dialog = BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme)
        val row = layoutInflater.inflate(R.layout.dialog_select_name, null)
        dialog.setContentView(row)
        dialog.show()


        row.findViewById<EditText>(R.id.editTextName).setText(binding.textViewGroupName.text.toString())

        row.findViewById<Button>(R.id.buttonSave).setOnClickListener {
            val newName = row.findViewById<EditText>(R.id.editTextName).text.toString()
            Backend.changeChatName(chat_id, newName)
            dialog.dismiss()
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

                val userId = FirebaseAuth.getInstance().currentUser!!.uid
                val storageRef = FirebaseStorage.getInstance().getReference("chats/$chat_id/icon.png")

                // compressing image
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, result.uri)
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 6, byteArrayOutputStream)
                val reducedImage: ByteArray = byteArrayOutputStream.toByteArray()

                storageRef.putBytes(reducedImage)
                    .addOnSuccessListener {

                        Log.i("xxx", "Success uploading Image to Firebase!!!")

                        storageRef.downloadUrl.addOnSuccessListener {

                            //getting image url
                            Log.i("xxx",it.toString())
                            Utilis.uploadFile(it, "chats/$chat_id/icon.png")

                        }.addOnFailureListener {

                            Log.i("xxx", "Error getting image download url")
                        }

                    }.addOnFailureListener {

                        Log.i("xxx", "Failed uploading image to server")

                    }
            }
        }
    }
}