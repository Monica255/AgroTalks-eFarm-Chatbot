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
import android.view.inputmethod.BaseInputConnection
import android.webkit.MimeTypeMap
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.eFarm.R
import com.example.eFarm.databinding.FragmentThreadBinding
import com.example.efarm.core.data.source.remote.model.Images
import dagger.hilt.android.AndroidEntryPoint
import org.apache.commons.io.output.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class Helper(){
    companion object{

        fun uriToDrawable(context: Context, uri: Uri): Drawable? {
            return try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val compressedBitmap = compressBitmap(bitmap)
                inputStream?.close()
                BitmapDrawable(context.resources, compressedBitmap)
            } catch (e: IOException) {
                Log.d("detail",e.message.toString())
                e.printStackTrace()
                null
            }
        }
        fun compressBitmap(bitmap: Bitmap): Bitmap {
            val maxSize = 512
            var quality = 100
            var compressedBitmap = bitmap
            val outputStream = ByteArrayOutputStream()
            Log.d("sizeee",outputStream.toByteArray().size.toString())

            do {
                outputStream.reset()
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                quality -= 10
                Log.d("sizeee",outputStream.toByteArray().size.toString())
            } while (outputStream.toByteArray().size > maxSize * 1024 && quality > 0)
            return BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.size())
        }

        class DownloadImageTask(private val listener: OnImageDownloadedListener?,private val context: Context) : AsyncTask<Any, Void, List<Pair<Drawable?,Int>?>?>() {
            override fun doInBackground(vararg params: Any?): List<Pair<Drawable?,Int>?>? {
                val imageUrl = params[0] as? List<Images>?: listOf()
                val list = mutableListOf<Pair<Drawable?,Int>?>()
                return try {
                    imageUrl.forEach {pair->
                        pair?.image?.let {
                            try {
                                val url = URL(it)
                                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                                connection.doInput = true
                                connection.connect()
                                val input: InputStream = connection.inputStream
                                val bitmap: Bitmap = BitmapFactory.decodeStream(input)
                                list.add(Pair(BitmapDrawable(bitmap), pair.position))
                            } catch (e: Exception) {
                                list.add(Pair(context.resources.getDrawable(R.drawable.cracked_img,context.theme),pair.position))
                                Log.e("edit", "Failed to download image: $it", e)
                            }
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
        if(isAdded)binding.etAddImg.text =
            requireActivity().getString(R.string.tambah_gambar, viewModel.listImage.value?.size?:"0")
        viewModel.listImage.observe(requireActivity()) {
            if(isAdded)binding.etAddImg.text =
                requireActivity().getString(R.string.tambah_gambar, it.size.toString())
        }
        binding.etThread.text = viewModel.tempThread
        binding.btnClose.setOnClickListener {
            viewModel.tempThread = textSpan
            onGetDataThread.handleDataThreadc(viewModel.tempThread)
            dismiss()
        }
        val inputConnection = BaseInputConnection(binding.etThread, true)
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
                        inputConnection.sendKeyEvent(event)
                        spannable.removeSpan(imageSpans[0])
                        Log.d("photo", "spam removed "+end)
                        var list=viewModel.listImage.value?: mutableListOf()
                        list=list.filter { it.position+2!=end }.toMutableList()
                        Log.d("photo", "spam removed "+list)
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
                val image = Helper.uriToDrawable(requireActivity(),filePath)
                image?.let{
                    textSpan.insert(pos,"\nX\n")
                    Log.d("TAG","\n \n".length.toString())
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
                    viewModel.mCount+=3
                    isTyping=false
                    binding.etThread.text = textSpan
                    binding.etThread.setSelection(pos+3)
                    Log.d("photo2",pos.toString())
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

//                items[item] == getString(R.string.take_picture) -> {
//                    startTakePhoto()
//                }

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