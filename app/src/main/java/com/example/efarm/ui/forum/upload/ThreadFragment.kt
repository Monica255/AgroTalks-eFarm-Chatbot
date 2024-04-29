package com.example.efarm.ui.forum.upload

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ImageSpan
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.example.eFarm.R
import com.example.eFarm.databinding.FragmentThreadBinding
import com.example.efarm.core.data.source.remote.model.Images
import com.example.efarm.core.data.source.remote.model.Thread
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max

class helper(){
    companion object{
        fun uriToDrawable(context: Context, uri: Uri): Drawable? {
            return try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val drawable = Drawable.createFromStream(inputStream, uri.toString())
                inputStream?.close()
                drawable
            } catch (e: IOException) {
                Log.d("detail",e.message.toString())
                e.printStackTrace()
                null
            }
        }


        class DownloadImageTask(private val listener: OnImageDownloadedListener?) : AsyncTask<Any, Void, List<Pair<Drawable?,Int>?>?>() {
            override fun doInBackground(vararg params: Any?): List<Pair<Drawable?,Int>?>? {
                val imageUrl = params[0] as? List<Images>?: listOf()
                val list = mutableListOf<Pair<Drawable?,Int>?>()
                return try {
                    imageUrl.forEach {pair->
                        pair?.image?.let {
                            val url = URL(it)
                            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                            connection.doInput = true
                            connection.connect()
                            val input: InputStream = connection.inputStream
                            val bitmap: Bitmap = BitmapFactory.decodeStream(input)
                            list.add(Pair(BitmapDrawable(bitmap),pair.position))
                        }

                    }
                    list.toList()
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            override fun onPostExecute(result: List<Pair<Drawable?,Int>?>?) {
                listener?.onImageDownloaded(result)
            }

            interface OnImageDownloadedListener {
                fun onImageDownloaded(drawable: List<Pair<Drawable?,Int>?>?)
            }
        }

    }
}
interface OnGetDataThread {
    fun handleDataThreadc(data: SpannableStringBuilder)
}

@AndroidEntryPoint
class ThreadFragment : DialogFragment() {
    private var _binding: FragmentThreadBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MakePostViewModel by activityViewModels()
    private lateinit var onGetDataThread: OnGetDataThread
    lateinit var textSpan: SpannableStringBuilder
    private var isTyping =true
    override fun onStart() {
        super.onStart()
        dialog?.window
            ?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            onGetDataThread = activity as OnGetDataThread
        } catch (e: ClassCastException) {
            Log.e(
                "TAG", "onAttach: ClassCastException: "
                        + e.message
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textSpan = viewModel.tempThread
//        viewModel.listImage.value = mutableListOf()
        binding.etAddImg.text =
            requireActivity().getString(R.string.tambah_gambar, "0")

        viewModel.listImage.observe(requireActivity()) {
            if(isAdded) binding.etAddImg.text =
                requireActivity().getString(R.string.tambah_gambar, it.size.toString())
        }
        binding.etThread.setText(viewModel.tempThread)
        binding.btnClose.setOnClickListener {
            viewModel.tempThread = textSpan
            onGetDataThread.handleDataThreadc(viewModel.tempThread)
            dismiss()
        }

        binding.etThread.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                val cursorPosition = binding.etThread.selectionStart
                val spannable = binding.etThread.text as Spannable
                val imageSpans =
                    spannable.getSpans(cursorPosition, cursorPosition, ImageSpan::class.java)
                if (imageSpans.isNotEmpty()) {
                    val start = spannable.getSpanStart(imageSpans[0])
                    val end = spannable.getSpanEnd(imageSpans[0])
                    if (cursorPosition == start || cursorPosition == end) {
                        spannable.removeSpan(imageSpans[0])
                        Log.d("photo", "spam removed "+end)
                        var list=viewModel.listImage.value?: mutableListOf()
                        list=list.filter { it.position!=end }.toMutableList()
                        viewModel.listImage.value=list
                        return@OnKeyListener true
                    }
                }
            }
            false
        })


        binding.etThread.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val newText = s.toString()
                if(isTyping){
                    if (newText.length > viewModel.mCount) {
                        val span = s?.substring(start)
                        textSpan.append(span)
                    }
                    if (newText.length < viewModel.mCount) {
                        textSpan.delete(start,start+1)
                    }
                    viewModel.mCount = newText.length
                    Log.d("photo",start.toString())
                    Log.d("photo",textSpan.toString())
                }
                isTyping=true
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.etAddImg.setOnClickListener {
            if ((viewModel.listImage.value ?: mutableListOf()).size < 5) select()
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val filePath = result.data?.data as Uri
            val pos = binding.etThread.selectionStart

            if (filePath != null) {
                val image = helper.uriToDrawable(requireActivity(),filePath)
                image?.let{
                    textSpan.insert(pos,"\n \n")
                    Log.d("photo2",textSpan.toString())
                    val maxWidth = binding.etThread.width - binding.etThread.paddingStart - binding.etThread.paddingEnd
                    val drawableWidth = image.intrinsicWidth
                    val drawableHeight = image.intrinsicHeight
                    if(drawableHeight<=drawableWidth){
                        val scaledHeight = (maxWidth.toFloat() / drawableWidth.toFloat() * drawableHeight.toFloat()).toInt()
                        image?.setBounds(0, 0, maxWidth, scaledHeight)
                    }else{
                        val scaledWidth = (maxWidth.toFloat() / drawableHeight.toFloat() * drawableWidth.toFloat()).toInt()
                        image?.setBounds(0, 0, scaledWidth, maxWidth)
                    }
                    val imageSpan = image?.let { it1 -> ImageSpan(it1, ImageSpan.ALIGN_BOTTOM) }

                    textSpan.setSpan(
                        imageSpan,
                        pos+1,
                        pos+2,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                    Log.d("photo2",textSpan.toString())
                    val list = viewModel.listImage.value ?: mutableListOf()
                    list?.add(Images(pos, filePath.toString()))
                    viewModel.listImage.value = list
                    isTyping=false
                    binding.etThread.text = textSpan
                    binding.etThread.setSelection(pos+3)
                    Log.d("photo2",textSpan.toString())
                }
            }
        }
    }




    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, getString(R.string.choose_picture))
        launcherIntentGallery.launch(chooser)
    }

    private fun select() {
        val items = arrayOf<CharSequence>(
            getString(R.string.from_galeri),
            getString(R.string.take_picture),
            getString(R.string.cancel)
        )

        val title = TextView(requireActivity())
        title.text = getString(R.string.select_photo)
        title.gravity = Gravity.CENTER
        title.setPadding(10, 15, 15, 10)
        title.setTextColor(resources.getColor(R.color.dark_blue, requireActivity().theme))
        title.textSize = 22f
        val builder = AlertDialog.Builder(
            requireActivity()
        )
        builder.setCustomTitle(title)
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == getString(R.string.from_galeri) -> {
                    startGallery()

                }

                items[item] == getString(R.string.take_picture) -> {
//                    startTakePhoto()
                }

                items[item] == getString(R.string.cancel) -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentThreadBinding.inflate(inflater, container, false)
        return binding.root
    }

}