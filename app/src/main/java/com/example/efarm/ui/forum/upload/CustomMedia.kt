package com.example.efarm.ui.forum.upload

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.VideoView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.example.eFarm.R
import com.google.firebase.storage.FirebaseStorage
class CustomMedia:ConstraintLayout {
    private lateinit var vv_header: VideoView
    private lateinit var img_header: ImageView
    private lateinit var img_play: ImageView
    var isPlaying=false
    private fun init(context: Context) {
        val rootView = inflate(context, R.layout.view_video, this)
        vv_header = rootView.findViewById(R.id.vv_header)
        img_header = rootView.findViewById(R.id.img_header)
        img_play = rootView.findViewById(R.id.img_play)
        img_play.setOnClickListener {
            vv_header.start()
            isPlaying=true
            img_play.visibility=View.GONE
        }
        vv_header.setOnCompletionListener {
            isPlaying=false
            img_play.visibility=View.VISIBLE
        }
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }
    constructor(context: Context) : super(context) {
        init(context)
    }
    fun showMedia(filePath:String){
        val type=filePath.split("/")
        if (filePath != null) {
            if(!displayMedia(type.last(),filePath)){
                try {
                    val storage = FirebaseStorage.getInstance()
                    val storageRef = storage.getReferenceFromUrl(filePath.toString())
                    storageRef.metadata.addOnSuccessListener { storageMetadata ->
                        val contentType = storageMetadata.contentType
                        displayMedia(contentType,filePath)
                    }.addOnFailureListener { e ->
                        e.printStackTrace()
                    }
                }catch (e:Exception){
                    showPlaceHolder(R.drawable.cracked_img)
                }
            }
        }
    }

//    fun onClick(){
//        if(isPlaying) {
//            pause()
//            isPlaying=false
//            img_play.visibility=View.VISIBLE
//        } else {
//            resume()
//            isPlaying=true
//            img_play.visibility=View.GONE
//        }
//    }
    fun start(){
        vv_header.start()
    }

    fun pause(){
        vv_header.pause()
    }
    fun resume(){
        vv_header.resume()
    }
    private fun displayMedia(contentType:String?,filePath: String):Boolean{
        if (contentType != null && (contentType.startsWith("image")||contentType.endsWith(".jpg")||contentType.endsWith(".png")||contentType.endsWith(".jpeg"))) {
            vv_header.visibility= View.GONE
            img_header.visibility= View.VISIBLE
            Glide.with(this)
                .load(filePath)
                .error(R.drawable.cracked_img)
                .into(img_header)
            return true
        } else if (contentType != null && (contentType.startsWith("video")||contentType.endsWith(".mp4"))) {
            vv_header.visibility = View.VISIBLE
            img_header.visibility = View.GONE
            vv_header.setVideoPath(filePath)
            vv_header.start()
            return true
        }else return false
    }
    fun showPlaceHolder(img:Int){
        vv_header.visibility= View.GONE
        img_header.visibility= View.VISIBLE
        Glide.with(this)
            .load(img)
            .error(R.drawable.cracked_img)
            .placeholder(R.drawable.placeholder_img)
            .into(img_header)
    }
}