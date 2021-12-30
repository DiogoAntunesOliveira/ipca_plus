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


class ChatFilesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatFilesBinding

    private lateinit var path: String
    private lateinit var titlePath: String
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
        path = "chats/$chat_id/files"
        binding.currentfolderName.text = titlePath
        binding.arrowLeft.visibility = View.GONE

        Backend.getAllChatFolderFiles(path) { _files ->

            files.clear()
            files.addAll(_files)
            filesAdapter?.notifyDataSetChanged()

        }

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


    fun refreshView() {

        binding.currentfolderName.text = titlePath
        binding.arrowLeft.visibility = View.VISIBLE

        Backend.getAllChatFolderFiles(path) { _files ->

            files.clear()
            files.addAll(_files)
            filesAdapter?.notifyDataSetChanged()

        }

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

        when (item.itemId){
            R.id.add -> {
                addDialog()
                return true
            }
        }
        return false
    }

    fun addDialog() {
        val dialog = BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.dialog_add_file, null)

        view.findViewById<ImageView>(R.id.imageViewAddFile).setOnClickListener {

            println("--------------------------> add file ")

            //startPickAFile("ola1", "ola2")
        }

        view.findViewById<ImageView>(R.id.imageViewAddFolder).setOnClickListener {
            println("--------------------------> add folder")
        }

        dialog.setContentView(view)
        dialog.show()
    }

    /*
       This function happen after picking photo, and make changes in the activity
    */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 86 && resultCode == RESULT_OK && data != null){
            println("----------------> " + data.data?.lastPathSegment)
            //Utilis.uploadFile(result.uri, path + ".pdf")
        }


    }


    private fun goBack() {

        // Don't allow go more back than the root folder
        if (titlePath != "root") {

            // Remove last folder in the path
            val strArrayPath = Pattern.compile("/").split(path)
            var newPath = ""
            for (i in 0 until strArrayPath.size-1)
                newPath += "/${strArrayPath[i]}"
            path = newPath

            // Remove last folder in the title path
            val strArrayTitlePath = Pattern.compile("/").split(titlePath)
            var newTitlePath = "root"
            for (i in 1 until strArrayTitlePath.size-1)
                newTitlePath += "/${strArrayTitlePath[i]}"
            titlePath = newTitlePath

            binding.currentfolderName.text = titlePath
            if (titlePath == "root")
                binding.arrowLeft.visibility = View.GONE

            Backend.getAllChatFolderFiles(path) { _files ->

                files.clear()
                files.addAll(_files)
                filesAdapter?.notifyDataSetChanged()

            }

        }
    }

    // When the support action bar back button is pressed, the app will go back to the previous activity
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
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
                val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)
                textViewName.text = files[position].name
                imageViewIcon.setImageDrawable(resources.getDrawable(files[position].icon))

                linearLayout.setOnClickListener {
                    if (files[position].icon == R.drawable.ic_folder) {
                        path += "/${files[position].name}"
                        titlePath += "/${files[position].name}"
                        refreshView()
                    }
                }

            }
        }

        override fun getItemCount(): Int {
            return files.size
        }
    }

}