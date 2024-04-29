package com.example.efarm.ui.forum.upload

import android.content.Intent.ACTION_GET_CONTENT
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.eFarm.R
import com.example.eFarm.databinding.ActivityMakePostBinding
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.ForumPost
import com.example.efarm.core.data.source.remote.model.Thread
import com.example.efarm.core.data.source.remote.model.Topic
import com.example.efarm.core.util.ADMIN_ID
import com.example.efarm.core.util.DateConverter
import com.example.efarm.core.util.KategoriTopik
import com.example.efarm.ui.forum.FilterTopicAdapter
import com.example.efarm.ui.forum.ForumTopicFragment
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class MakePostActivity : AppCompatActivity(), OnGetDataTopics, OnGetDataThread {
    lateinit var binding: ActivityMakePostBinding
    private var title = ""
    private lateinit var adapterTopic: FilterTopicAdapter
    private val viewModel: MakePostViewModel by viewModels()
    var uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
//    private var getFile: File? = null
    private var filePath: Uri? = null

    companion object {
        const val FILENAME_FORMAT = "MMddyyyy"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMakePostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActionBar()
        binding.imgHeader.setOnClickListener {
            select()
        }
        binding.etTitle.addTextChangedListener {
            title = binding.etTitle.text.toString().trim()
        }
        binding.btnSend.setOnClickListener {
            send()
        }
        binding.btnPilihTopik.setOnClickListener {
            val topicFragment = ChooseTopicFragment()
            topicFragment.show(supportFragmentManager, "pilih_topic_dialog")
        }

        binding.etThread.setOnClickListener {
            val topicFragment = ThreadFragment()
            topicFragment.show(supportFragmentManager, "thread_dialog")
        }

        val layoutManagerCommonTopic = FlexboxLayoutManager(this)
        layoutManagerCommonTopic.flexDirection = FlexDirection.ROW
        binding.rvTopic.layoutManager = layoutManagerCommonTopic
        adapterTopic = FilterTopicAdapter(false) {}

        binding.rvTopic.adapter = adapterTopic

        viewModel.topics.observe(this) {
            adapterTopic.submitList(it.toMutableList())
        }

        lifecycleScope.launch {
            viewModel.getListTopik(KategoriTopik.SEMUA).observe(this@MakePostActivity) {
                when (it) {
                    is Resource.Loading -> {
                        Log.d("TAG", "Loading common topics")
                    }

                    is Resource.Success -> {
                        if (it.data == null || it.data.isEmpty()) {
                            Log.d("TAG", "common null")
                        }

                        it.data?.toMutableList()?.let { it1 ->
                            val common=it1.filter { it.topic_category.trim()=="common topics"  }
                            val commodity= it1.filter { it.topic_category.trim()=="commodity" }
                            viewModel.topicsCommon.value = common.toMutableList()
                            viewModel.topicsCommodity.value=commodity.toMutableList()
                        }
                    }

                    is Resource.Error -> {
                        Toast.makeText(
                            this@MakePostActivity,
                            "Failed to get topics",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> {}
                }
            }
        }
    }

//    private fun uriToFile(selectedImg: Uri, context: Context): File {
//        val contentResolver: ContentResolver = context.contentResolver
//        val myFile = createCustomTempFile(context)
//
//        val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
//        val outputStream: OutputStream = FileOutputStream(myFile)
//        val buf = ByteArray(1024)
//        var len: Int
//        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
//        outputStream.close()
//        inputStream.close()
//
//        return myFile
//    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            filePath = result.data?.data as Uri
            if (filePath != null) {
                anyPhoto=true
//                val temp: Uri = filePath!!
//                val myFile = uriToFile(temp, this)
//                getFile = myFile
                binding.imgHeader.setImageURI(filePath)

            }
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, getString(R.string.choose_picture))
        launcherIntentGallery.launch(chooser)
    }

    private var anyPhoto = false
    private var currentPhotoPath: String? = null
    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {result ->
        if (result.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            filePath = FileProvider.getUriForFile(
                this,
                getString(R.string.package_name),
                myFile
            )
            anyPhoto = true
            val resultBitmap = BitmapFactory.decodeFile(myFile.path)
            binding.imgHeader.setImageBitmap(resultBitmap)
        }
    }
    private val timeStamp: String = SimpleDateFormat(
        FILENAME_FORMAT,
        Locale.US
    ).format(System.currentTimeMillis())

    private fun createCustomTempFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(timeStamp, ".jpg", storageDir)
    }

    private fun startTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)
        createCustomTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                getString(R.string.package_name),
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private fun send() {
        when {
            title == "" -> {
                Toast.makeText(this,"Judul tidak boleh kosong",Toast.LENGTH_SHORT).show()
            }

            viewModel.tempThread.isEmpty()-> {
                Toast.makeText(this,"Thread tidak boleh kosong",Toast.LENGTH_SHORT).show()
            }

            viewModel.topics.value == null -> {
                Toast.makeText(this,"Pilih setidaknya satu topik",Toast.LENGTH_SHORT).show()
            }

            viewModel.topics.value!!.isEmpty() -> {
                Toast.makeText(this,"Pilih setidaknya satu topik",Toast.LENGTH_SHORT).show()
            }
            else -> {
                var thread=Thread(viewModel.tempThread.toString(),viewModel.listImage.value)

                var data:ForumPost?=null
                uid?.let {
                    data=ForumPost(
                        "",
                        it,
                        title,
                        null,
                        DateConverter.getCurrentTimestamp(),
                        mutableListOf<String>(),
                        mutableListOf<String>(),
                        viewModel.topics.value!!.map { t ->t.topic_id },
                        verified = if(it== ADMIN_ID)"content" else null,
                        null,
                        thread
                    )
                }

                data?.let{data->
                    lifecycleScope.launch {
                        viewModel.uploadThread(data,if(anyPhoto)filePath else null).observe(this@MakePostActivity){
                            when(it){
                                is Resource.Error->{
                                    showLoading(false)
                                    it.message?.let{
                                        Toast.makeText(this@MakePostActivity,it,Toast.LENGTH_SHORT).show()
                                    }
                                }
                                is Resource.Success->{
                                    showLoading(false)
                                    it.data?.let{
                                        Toast.makeText(this@MakePostActivity,it,Toast.LENGTH_SHORT).show()
                                        setResult(RESULT_OK)
                                        finish()
                                    }
                                }
                                is Resource.Loading->{
                                    showLoading(true)
                                }
                            }

                        }

                    }
                }
            }

        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
    }


    private fun select() {
        val items = arrayOf<CharSequence>(
            getString(R.string.from_galeri),
            getString(R.string.take_picture),
            getString(R.string.cancel)
        )
        val items2 = arrayOf<CharSequence>(
            getString(R.string.from_galeri),
            getString(R.string.take_picture),
            getString(R.string.delete_image),
            getString(R.string.cancel)
        )

        val title = TextView(this)
        title.text = getString(R.string.select_photo)
        title.gravity = Gravity.CENTER
        title.setPadding(10, 15, 15, 10)
        title.setTextColor(resources.getColor(R.color.dark_blue, theme))
        title.textSize = 22f
        val builder = AlertDialog.Builder(
            this
        )
        builder.setCustomTitle(title)
        val mItems = if (anyPhoto) items2 else items
        builder.setItems(mItems) { dialog, item ->
            when {
                mItems[item] == getString(R.string.from_galeri) -> {
                    startGallery()

                }

                mItems[item] == getString(R.string.take_picture) -> {
                    startTakePhoto()

                }

                mItems[item] == getString(R.string.delete_image) -> {
//                    Log.d("HEADER", getFile.toString())
                    filePath = null
                    currentPhotoPath = null
                    anyPhoto = false
                    Glide.with(this)
                        .load(R.drawable.placeholder_img)
                        .placeholder(R.drawable.placeholder_img)
                        .into(binding.imgHeader)
                    dialog.dismiss()
                }

                mItems[item] == getString(R.string.cancel) -> {
                    dialog.dismiss()
                }


            }
        }
        builder.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
    }

    override fun handleDataTopic(data: Set<Topic>) {
        viewModel.topics.value = data.toSet()
    }

    override fun handleDataThreadc(data: SpannableStringBuilder) {
        this.window
            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        binding.etThread.setText(data.toString())
    }
}