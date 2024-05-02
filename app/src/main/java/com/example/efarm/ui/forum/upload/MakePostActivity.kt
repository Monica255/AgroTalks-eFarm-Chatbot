package com.example.efarm.ui.forum.upload

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
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
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
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
import com.example.efarm.core.util.FORUM_POST_ID
import com.example.efarm.core.util.KategoriTopik
import com.example.efarm.ui.forum.FilterTopicAdapter
import com.example.efarm.ui.forum.ForumViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class MakePostActivity : AppCompatActivity(), OnGetDataTopics, OnGetDataThread {
    lateinit var binding: ActivityMakePostBinding
    private var title = ""
    private var id: String = ""
    private lateinit var adapterTopic: FilterTopicAdapter
    private val viewModel: MakePostViewModel by viewModels()
    private val viewModelForum: ForumViewModel by viewModels()
    var uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private var filePath: Uri? = null

    companion object {
        const val FILENAME_FORMAT = "MMddyyyy"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMakePostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActionBar(resources.getString(R.string.buat_postingan))
        id = intent.getStringExtra(FORUM_POST_ID) ?: ""
        if(id!=""&&id!=null) {
            setActionBar(resources.getString(R.string.edit_postingan))
            viewModelForum.getDetailForum(id).observe(this) {
                when (it) {
                    is Resource.Loading -> {}
                    is Resource.Error -> {}
                    is Resource.Success -> {
                        it.data?.let {
                            setData(it)
                        }
                    }
                }
            }
        }

        binding.customaView.setOnClickListener {
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

        viewModel.isLoadData.observe(this) {
            showLoading(it)
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
                            val common = it1.filter { it.topic_category.trim() == "common topics" }
                            val commodity = it1.filter { it.topic_category.trim() == "commodity" }
                            viewModel.topicsCommon.value = common.toMutableList()
                            viewModel.topicsCommodity.value = commodity.toMutableList()
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

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            filePath = result.data?.data as Uri
            filePath?.let { binding.customaView.showMedia(it.toString()) }
        }
    }


    private fun startGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/* video/*" // Accepts both images and videos
        intent.putExtra(
            Intent.EXTRA_MIME_TYPES,
            arrayOf("image/*", "video/*")
        ) // Explicitly specify MIME types
        val chooser = Intent.createChooser(intent, getString(R.string.choose_picture))
        launcherIntentGallery.launch(chooser)
    }

    private var currentPhotoPath: String? = null
    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            filePath = FileProvider.getUriForFile(
                this,
                getString(R.string.package_name),
                myFile
            )
            binding.customaView.showMedia(currentPhotoPath.toString())
        }else{
            binding.customaView.start()
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
                Toast.makeText(this, "Judul tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }

            viewModel.tempThread.isEmpty() -> {
                Toast.makeText(this, "Thread tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }

            viewModel.topics.value == null -> {
                Toast.makeText(this, "Pilih setidaknya satu topik", Toast.LENGTH_SHORT).show()
            }

            viewModel.topics.value!!.isEmpty() -> {
                Toast.makeText(this, "Pilih setidaknya satu topik", Toast.LENGTH_SHORT).show()
            }

            else -> {
                var thread = Thread(viewModel.tempThread.toString(), viewModel.listImage.value)

                var data: ForumPost? = null
                uid?.let {
                    data = ForumPost(
                        id,
                        it,
                        title,
                        if (filePath != null) filePath.toString() else null,
                        DateConverter.getCurrentTimestamp(),
                        mutableListOf<String>(),
                        mutableListOf<String>(),
                        viewModel.topics.value!!.map { t -> t.topic_id },
                        verified = if (it == ADMIN_ID) "content" else null,
                        null,
                        thread
                    )
                }

                data?.let { data ->
                    lifecycleScope.launch {
                        viewModel.uploadThread(data, if (filePath != null) filePath else null)
                            .observe(this@MakePostActivity) {
                                when (it) {
                                    is Resource.Error -> {
                                        showLoading(false)
                                        it.message?.let {
                                            Toast.makeText(
                                                this@MakePostActivity,
                                                it,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                    is Resource.Success -> {
                                        showLoading(false)
                                        it.data?.let {
                                            Toast.makeText(
                                                this@MakePostActivity,
                                                it,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            setResult(RESULT_OK)
                                            finish()
                                        }
                                    }

                                    is Resource.Loading -> {
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
        val mItems = if (filePath != null) items2 else items
        builder.setItems(mItems) { dialog, item ->
            when {
                mItems[item] == getString(R.string.from_galeri) -> {
                    startGallery()
                }

                mItems[item] == getString(R.string.take_picture) -> {
                    startTakePhoto()
                }

                mItems[item] == getString(R.string.delete_image) -> {
                    filePath = null
                    currentPhotoPath = null
                    binding.customaView.showPlaceHolder(R.drawable.placeholder_img)
                    dialog.dismiss()
                }

                mItems[item] == getString(R.string.cancel) -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    val span = SpannableStringBuilder()
    private fun setData(forumPost: ForumPost) {
        viewModel.isLoadData.value = true
        title = forumPost.title
        filePath = forumPost.img_header?.toUri()
        Log.d("edit", filePath.toString())
        span.append(forumPost.thread?.thread)
        viewModel.tempThread = span
        forumPost.thread?.images?.let { imgs ->
            viewModel.listImage.value = imgs
            downloadImageTask.execute(imgs.toList())
        }

        viewModel.topicsCommodity.observe(this@MakePostActivity) { commodity ->
            viewModel.topicsCommon.observe(this@MakePostActivity) { common ->
                if (commodity != null && commodity.isNotEmpty() && common != null && common.isNotEmpty()) {
                    var data2 = setOf<Topic>()
                    forumPost.topics?.map { id ->
                        var data = mutableListOf<Topic>()
                        val sdata = viewModel.topicsCommodity.value?.filter { it.topic_id == id }
                        sdata?.let { data.addAll(it) }
                        val fdata = viewModel.topicsCommon.value?.filter { it.topic_id == id }
                        fdata?.let { data.addAll(it) }
                        data2 = data.toSet()

                    }
                    viewModel.topics.value = data2
                }
            }
        }

        binding.apply {
            if (filePath != null) {
                binding.customaView.showMedia(filePath.toString())
            } else {
                binding.customaView.showPlaceHolder(R.drawable.placeholder_img)
            }

            etTitle.setText(title)
            etThread.text = span
        }
    }

    lateinit var image: Drawable
    val downloadImageTask =
        Helper.Companion.DownloadImageTask(object :
            Helper.Companion.DownloadImageTask.OnImageDownloadedListener {

            override fun onImageDownloaded(drawable: List<Pair<Drawable?, Int>?>?) {
                val maxWidth =
                    binding.etThread.width - 74
                drawable?.let { pair ->
                    pair.forEach {
                        it?.let { img ->
                            img?.first?.let {
                                image = it
                                image?.let {
                                    val drawableWidth = image.intrinsicWidth
                                    val drawableHeight = image.intrinsicHeight
                                    if (drawableHeight <= drawableWidth) {
                                        val scaledHeight =
                                            (maxWidth.toFloat() / drawableWidth.toFloat() * drawableHeight.toFloat()).toInt()
                                        image?.setBounds(0, 0, maxWidth, scaledHeight)
                                    } else {
                                        val scaledWidth =
                                            (maxWidth.toFloat() / drawableHeight.toFloat() * drawableWidth.toFloat()).toInt()
                                        image?.setBounds(0, 0, scaledWidth, maxWidth)
                                    }
                                    val imageSpan =
                                        image?.let { it1 -> ImageSpan(it1, ImageSpan.ALIGN_BOTTOM) }
                                    span.setSpan(
                                        imageSpan, img.second + 1, img.second + 2,
                                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                                    )

                                }
                            }


                        }
                    }
                    binding.etThread.setText(span.toString())
                    viewModel.isLoadData.value = false
                }
            }
        }, this)


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setActionBar(title: String) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        binding.toolbarTitle.text = title
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