package com.singularity.ipcaplus.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.singularity.ipcaplus.R
import com.singularity.ipcaplus.databinding.ActivityChatFilesBinding
import com.singularity.ipcaplus.models.FirebaseFile
import com.singularity.ipcaplus.utils.Backend
import java.util.regex.Pattern
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment.DIRECTORY_DOWNLOADS
import androidx.core.net.toUri
import com.singularity.ipcaplus.utils.Utilis
import android.provider.OpenableColumns
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import com.google.firebase.storage.FirebaseStorage

class ChatFilesActivity : AppCompatActivity() {

    // Variables
    private lateinit var binding: ActivityChatFilesBinding
    private lateinit var currentPath: String
    private lateinit var titlePath: String
    var selectedFile = ""
    var files = arrayListOf<FirebaseFile>()
    private var filesAdapter: RecyclerView.Adapter<*>? = null
    private var filesLayoutManager: LinearLayoutManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_files)

        // Create the layout for this fragment
        binding = ActivityChatFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get previous data
        val chat_id = intent.getStringExtra("chat_id").toString()

        titlePath = "root"
        currentPath = "chats/$chat_id/files"
        binding.currentfolderName.text = titlePath
        binding.arrowLeft.visibility = View.GONE

        // Get All data
        refreshList()

        binding.linearLayoutPathDisplay.setOnClickListener {
            goBack()
        }

        // List
        filesLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.filesRecyclerView.layoutManager = filesLayoutManager
        filesAdapter = FileAdapter()
        binding.filesRecyclerView.itemAnimator = DefaultItemAnimator()
        binding.filesRecyclerView.adapter = filesAdapter
    }


    /*
       This function create the action bar above the activity
    */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_add, menu)

        // Create Action Bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24)
        supportActionBar?.title = "Ficheiros"

        return true
    }


    /*
        This function define the events of the action bar buttons
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        when (item.itemId) {
            R.id.add -> {
                addDialog()
                return true
            }
        }
        return false
    }


    /*
        This function is responsible for upload a selected file
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var path = ""
        var fileName = ""
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                val clipData = data?.clipData
                if (clipData == null) {

                    var cursor: Cursor? = null
                    try {
                        contentResolver.query(data?.data!!, null, null, null, null).use {

                            cursor = it

                            if (cursor != null && cursor!!.moveToFirst()) {

                                fileName =
                                    cursor!!.getString(cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                        .toInt())

                            }

                        }
                    } finally {
                        cursor?.close()

                    }

                    path += data?.data.toString()
                } else {
                    for (i in 0 until clipData.itemCount) {
                        val item = clipData.getItemAt(i)
                        val uri: Uri = item.uri
                        path += uri.toString() + "\n"
                    }
                }

            }
        }

        // Upload and refresh list
        Utilis.uploadFile(path.toUri(), "$currentPath/$fileName")
        files.add(FirebaseFile(fileName, Utilis.getFileIcon(fileName)))
        filesAdapter?.notifyDataSetChanged()
    }


    /*
        When the support action bar back button is pressed, the app will go back to the previous activity
    */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    /*
        Context Menu. When its a folder display option delete, when its a file display, download and delete options
    */
    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        selectedFile = v.findViewById<TextView>(R.id.textViewName).text.toString()

        // Show Download Option if its a file
        if (selectedFile.contains(".")) {
            menu.add(0, v.id, 0, "Transferir")
        }

        menu.add(0, v.id, 0, "Apagar")
    }


    /*
        Each context menu item function when its selected
    */
    override fun onContextItemSelected(item: MenuItem): Boolean {

        val fileRef = Firebase.storage.reference.child("$currentPath/$selectedFile")

        if (item.title === "Transferir") {
            downloadFileRequest(currentPath, selectedFile)
        } else if (item.title === "Apagar") {

            // Verify If the selected File is a folder or a file
            if (selectedFile.contains(".")) {

                confirmationDialog("Apagar Ficheiro",
                    "Tens certeza que queres apagar este ficheiro?") {

                    fileRef.delete()
                        .addOnCompleteListener {
                            refreshList()
                        }
                }

            } else {

                confirmationDialog("Apagar Pasta",
                    "Tens certeza que queres apagar esta pasta e tudo dentro dela?") {

                    Backend.deleteAllFilesInsideFolder("$currentPath/$selectedFile") {
                        refreshList()
                    }
                }

            }

        }
        return true
    }


    /*
        Refresh the entire view (path title and list)
    */
    fun refreshView() {

        binding.currentfolderName.text = titlePath
        binding.arrowLeft.visibility = View.VISIBLE

        refreshList()
    }


    /*
        Refresh the list
    */
    private fun refreshList() {
        Backend.getAllChatFolderFiles(currentPath) { _files ->
            files.clear()
            files.addAll(_files)
            filesAdapter?.notifyDataSetChanged()
        }
    }


    /*
        Add new file / folder dialog
    */
    private fun addDialog() {
        val dialog = BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.dialog_add_file, null)
        dialog.setContentView(view)
        dialog.show()

        view.findViewById<ImageView>(R.id.imageViewAddFile).setOnClickListener {
            chooseFile()
            dialog.dismiss()
        }

        view.findViewById<ImageView>(R.id.imageViewAddFolder).setOnClickListener {
            openSelectFolderNameDialog()
            dialog.dismiss()
        }

    }


    /*
        Confirmation Dialog Display Yes / No Options
    */
    private fun confirmationDialog(title: String, description: String, callBack: () -> Unit) {
        val alertDialog = AlertDialog.Builder(this)

        alertDialog.setTitle(title)
        alertDialog.setMessage(description)

        alertDialog.setPositiveButton("Sim") { _, _ ->
            callBack()
        }

        alertDialog.setNegativeButton("Não") { _, _ ->
            alertDialog.show().dismiss()
        }

        alertDialog.show()
    }


    /*
        This function display a dialog window with a text box to send reset password email
     */
    private fun openSelectFolderNameDialog() {

        val alertDialog = AlertDialog.Builder(this)

        val row = layoutInflater.inflate(R.layout.dialog_select_name, null)
        alertDialog.setView(row)
        val show = alertDialog.show()

        // Variables
        val editTextView = row.findViewById<TextView>(R.id.editTextName)
        row.findViewById<TextView>(R.id.textView).text = "Criar Pasta"
        editTextView.hint = "Nome"
        row.findViewById<Button>(R.id.buttonSave).text = "Criar"

        row.findViewById<Button>(R.id.buttonSave).setOnClickListener {

            val name = if (editTextView.text != "") editTextView.text.toString() else "Nova pasta"
            createFolder(this, name)
            show.dismiss()
        }

    }


    /*
        Open Select a file window
    */
    private fun chooseFile() {
        val intent = Intent()
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        val extraMimeTypes = arrayOf("application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
            "text/plain",
            "application/pdf",
            "application/zip",
            "image/gif",
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/svg+xml",
            "image/webp",
            "image/vnd.wap.wbmp",
            "image/vnd.nok-wallpaper",
            "text/xml",
            "application/json",
            "text/json",
            "text/javascript"
        )
        intent.putExtra(Intent.EXTRA_MIME_TYPES, extraMimeTypes)
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, 1)
    }


    /*
        Create a folder in the current path by creating a invisible file inside the folder
    */
    private fun createFolder(context: Context, folderName: String) {

        val storage = Firebase.storage
        val storageRef = storage.reference

        // Create a folder with a temp file
        val outputDir: File = context.cacheDir // context being the Activity pointer
        val outputFile: File = File.createTempFile("temp", ".invisible", outputDir)

        val ref: StorageReference = storageRef.child("$currentPath/$folderName/temp.invisible")
        ref.putFile(outputFile.toUri())
            .addOnSuccessListener {

                // Refresh Folder
                refreshList()
            }
    }


    /*
        Go back to the previous folder when the user click on the path title, refresh the list
    */
    private fun goBack() {

        // Don't allow go more back than the root folder
        if (titlePath != "root") {

            // Remove last folder in the path
            val strArrayPath = Pattern.compile("/").split(currentPath)
            var newPath = ""
            for (i in 0 until strArrayPath.size - 1)
                newPath += "/${strArrayPath[i]}"
            currentPath = newPath

            // Remove last folder in the title path
            val strArrayTitlePath = Pattern.compile("/").split(titlePath)
            var newTitlePath = "root"
            for (i in 1 until strArrayTitlePath.size - 1)
                newTitlePath += "/${strArrayTitlePath[i]}"
            titlePath = newTitlePath

            binding.currentfolderName.text = titlePath
            if (titlePath == "root")
                binding.arrowLeft.visibility = View.GONE

            refreshList()

        }
    }


    /*
        Request confirmation before download the file, then download and store the file in the download folder in the mobile device
    */
    fun downloadFileRequest(path: String, name: String) {

        confirmationDialog("Transferir Ficheiro",
            "Tens certeza que queres transferir este ficheiro?") {

            val fileRef = Firebase.storage.reference.child("$path/${name}")
            val strArray = Pattern.compile("[.]").split(name)
            val fileName = strArray[0]
            val fileExtension = strArray[strArray.size - 1]

            fileRef.downloadUrl.addOnSuccessListener {
                Utilis.downloadFile(this, fileName, ".$fileExtension", DIRECTORY_DOWNLOADS, it)
            }
        }
    }


    inner class FileAdapter : RecyclerView.Adapter<FileAdapter.ViewHolder>() {

        inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.row_file, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.v.apply {

                val textViewName = findViewById<TextView>(R.id.textViewName)
                val imageViewIcon = findViewById<ImageView>(R.id.imageViewIcon)
                val linearLayout = findViewById<ConstraintLayout>(R.id.linearLayout)

                textViewName.text = files[position].name
                imageViewIcon.setImageDrawable(resources.getDrawable(files[position].icon))

                if (files[position].icon == R.drawable.ic_folder) {
                    linearLayout.setOnClickListener {
                        currentPath += "/${files[position].name}"
                        titlePath += "/${files[position].name}"
                        refreshView()
                    }
                } else {
                    linearLayout.setOnClickListener {

                        // Verify if its an image
                        val extensionArray = Pattern.compile("[.]").split(files[position].name)
                        val extension = extensionArray[extensionArray.size - 1]

                        // Show Preview if its an image. Ask to download if dont
                        if (extension == "png" || extension == "jpg" || extension == "jpeg" || extension == "jep" || extension == "jfif" || extension == "gif") {

                            Backend.getFileUrl("$currentPath/${files[position].name}") {

                                val intent = Intent(this.context, FilePreviewActivity::class.java)
                                intent.putExtra("url", it.toString())
                                intent.putExtra("fileName", files[position].name)
                                intent.putExtra("currentPath", currentPath)
                                startActivity(intent)

                            }

                        } else {

                            downloadFileRequest(currentPath, files[position].name)

                        }
                    }
                }

                registerForContextMenu(linearLayout)
            }

        }

        override fun getItemCount(): Int {
            return files.size
        }
    }

}