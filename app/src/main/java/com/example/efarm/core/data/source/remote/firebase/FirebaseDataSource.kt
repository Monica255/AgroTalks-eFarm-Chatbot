package com.example.efarm.core.data.source.remote.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.eFarm.R
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.Chat
import com.example.efarm.core.data.source.remote.model.CommentForumPost
import com.example.efarm.core.data.source.remote.model.ForumPost
import com.example.efarm.core.data.source.remote.model.Topic
import com.example.efarm.core.data.source.remote.model.UserData
import com.example.efarm.core.util.KategoriTopik
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class FirebaseDataSource @Inject constructor(
    firebaseDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    firebaseFirestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) {
    val currentUser: FirebaseUser?
        get() {
            return firebaseAuth.currentUser
        }
    private val storageUserRef = firebaseStorage.reference.child("thread_headers")
    private val userDataRef = firebaseDatabase.reference.child("user_data/")
    private val chatsRef = firebaseDatabase.reference.child("chats/")
    private val commentRef = firebaseFirestore.collection("forum_comments")

    fun getChats(): MutableLiveData<List<Chat>?> {
        val chats = MutableLiveData<List<Chat>?>()
        val x = currentUser?.uid
        x?.let {
            chatsRef.child(it)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            Log.d("chat",snapshot.toString())
                            try {
                                val chatList = mutableListOf<Chat>()
                                for (chatSnapshot in snapshot.children) {
                                    val chat = chatSnapshot.getValue(Chat::class.java)
                                    chat?.let {
                                        chatList.add(it)
                                    }
                                }

                                chats.value = chatList
                            } catch (e: Exception) {
                                chats.value = null
                                Log.d("TAG", e.message.toString())
                            }
                        }else{
                            chats.value = null
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        chats.value = null
                        Log.d("TAG", error.toString())
                    }

                })
        }
        return chats
    }

    suspend fun sendChat(data: Chat): MutableLiveData<Resource<String>> {
        val key = chatsRef.push().key
        val chat = MutableLiveData<Resource<String>>()
        val x = currentUser?.uid
        val data2 = Chat(key, data.actor, data.message, data.timestamp)

        x?.let {
            chatsRef.child(it).child(key!!).setValue(data2).addOnCompleteListener {
                if(it.isSuccessful){
                    chat.value = Resource.Success("Berhasil mengirim data")
                }else{
                    chat.value = Resource.Error("gagal mengirim data")
                }
            }.await()
        }
        return chat

    }

    suspend fun registerAccount(
        email: String,
        pass: String,
        name: String,
        telepon: String,
    ): Flow<Resource<String>> {
        return flow {
            try {
                emit(Resource.Loading())
                val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
                if (result.user != null) {
                    val x = setUserData(email, name, telepon)
//                    setConnectionData(true)
                    emit(Resource.Success(x.second))
                } else {
                    emit(Resource.Error<String>("Error"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.toString()))
                Log.e("TAG", e.toString())
            }
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun setUserData(
        email: String = "",
        name: String = "",
        telepon: String = ""
    ): Pair<Boolean, String> {
        if (name != "") {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            currentUser?.updateProfile(profileUpdates)
        }

        val photo = if (currentUser?.photoUrl != null) currentUser?.photoUrl.toString() else ""
        val user = UserData(
            currentUser?.email ?: email,
            currentUser?.displayName ?: name,
            telepon,
            currentUser?.uid ?: "",
            photo
        )
        var result: Pair<Boolean, String> = Pair(true, "Gagal masuk")
        currentUser?.uid?.let { it ->
            userDataRef.child(it).setValue(user).addOnCompleteListener { v ->
                result = if (v.isSuccessful) {
                    if (name != "") {
                        Pair(false, "Berhasil masuk")
                    } else {
                        Pair(false, "Berhasil masuk dengan akun google")
                    }
                } else {
                    Pair(true, v.exception?.message.toString()).also {
                        currentUser?.delete()
                        signOut()
                    }
                }
            }.await()
        }
        return result
    }

    fun login(email: String, pass: String): Flow<Resource<String>> {
        return flow {
            try {
                emit(Resource.Loading())
                val result = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
                result?.let {
                    if (result.user != null) {
                        emit(Resource.Success(context.getString(R.string.berhasil_login)))
//                        setConnectionData(true)
                    } else {
                        emit(Resource.Error(context.getString(R.string.email_atau_password_mungkin_salah)))
                    }
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.toString()))
                Log.e("TAG", e.toString())
            }
        }
    }

    fun getUserData(uid: String?): MutableLiveData<UserData?> {
        val userData = MutableLiveData<UserData?>()
        val x = uid ?: currentUser?.uid
        x?.let {
            userDataRef.child(it)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            try {
                                val user = snapshot.getValue(UserData::class.java)
                                userData.value = user
                            } catch (e: Exception) {
                                userData.value = null
                                Log.d("TAG", e.message.toString())
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        userData.value = null
                        Log.d("TAG", error.toString())
                    }

                })
        }
        return userData
    }

    fun signOut() {
        if (currentUser != null) {
            firebaseAuth.signOut()
        }
    }

    private val forumRef = firebaseFirestore.collection("forum_posts")
    fun getPagingForum(
        topic: Topic? = null
    ): Flow<PagingData<ForumPost>> {
        var query: Query
        if (topic != null) {
            query = forumRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .whereArrayContains("topics", topic.topic_id)
                .limit(4)
        } else {
            query = forumRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(4)
        }

        val pager = Pager(
            config = PagingConfig(
                pageSize = 4
            ),
            pagingSourceFactory = {
                ForumPostPagingStore(query)
            }
        )
        return pager.flow
    }

    fun getComments(
        idForum: String,
        idBestComment: CommentForumPost?
    ): Flow<PagingData<CommentForumPost>> {
        val query: Query = commentRef.orderBy("timestamp", Query.Direction.DESCENDING)
            .whereEqualTo("id_forum_post", idForum).limit(3)
        val pager = Pager(
            config = PagingConfig(
                pageSize = 3
            ),
            pagingSourceFactory = {
                CommentForumPagingStore(query, idBestComment)
            }
        )
        return pager.flow
    }

//    fun getComments(comments:List<String>,idBestComment:CommentForumPost?): Flow<PagingData<CommentForumPost>> {
//        val query:Query=commentRef.orderBy("timestamp",Query.Direction.DESCENDING).whereIn("id_comment",comments).limit(3)
//        val pager = Pager(
//            config = PagingConfig(
//                pageSize = 3
//            ),
//            pagingSourceFactory = {
//                CommentForumPagingStore(query,idBestComment)
//            }
//        )
//        return pager.flow
//    }

    private val topicRef = firebaseFirestore.collection("topics")
    private fun getQueryTopicByCategory(kategori: KategoriTopik): Query {
        return if (kategori != KategoriTopik.SEMUA) {
            topicRef.orderBy("topic_name", Query.Direction.ASCENDING)
                .whereEqualTo("topic_category", kategori.printable)
        } else {
            topicRef.orderBy("topic_name", Query.Direction.ASCENDING)
        }
    }

//    suspend fun getListTopikForum(kategory: KategoriTopik): Flow<Resource<List<Topic>>> {
//        var list = mutableListOf<Topic>()
//        var msg: String? = null
//        return flow {
//            emit(Resource.Loading())
//            val query = getQueryTopicByCategory(kategory)
//            query.get().addOnCompleteListener {
//                if (it.isSuccessful) {
//                    try {
//                        for (i in it.result) {
//                            val x = i.toObject<Topic>()
//                            list.add(x)
//                        }
//                    } catch (e: Exception) {
//                        msg = e.message
//                        list = mutableListOf()
//                    }
//                } else {
//                    msg = "Error"
//                    list = mutableListOf()
//                }
//            }.await()
//
//            if (msg != null) {
//                emit(Resource.Error(msg!!))
//            } else {
//                emit(Resource.Success(list))
//            }
//        }
//    }

    suspend fun getListTopikForum(kategory: KategoriTopik): Flow<Resource<List<Topic>>> {
        return flow {
            emit(Resource.Loading())
            val query = getQueryTopicByCategory(kategory)

            try {
                val result = suspendCoroutine<Task<QuerySnapshot>> { continuation ->
                    query.get().addOnCompleteListener { task ->
                        continuation.resume(task)
                    }
                }

                if (result.isSuccessful) {
                    val list =
                        result.result?.documents?.mapNotNull { it.toObject<Topic>() } ?: emptyList()
                    emit(Resource.Success(list))
                } else {
                    val errorMessage = result.exception?.message ?: "Error"
                    emit(Resource.Error(errorMessage))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Unknown error"))
            }
        }
    }


//    suspend fun getTopics(topics:List<String>): Flow<Resource<List<Topic>>>{
//        var list= mutableListOf<Topic>()
//        var msg:String?=context.getString(R.string.gagal_mendapatkan_data)
//        return flow{
//            topicRef.whereIn("topic_id",topics).get().addOnCompleteListener {
//                if(it.isSuccessful){
//                    msg=null
//                    try{
//                        for(i in it.result){
//                            val x=i.toObject<Topic>()
//                            list.add(x)
//                        }
//                    }catch (e:Exception){
//                        Log.d("TAG","unsuccessful")
//                    }
//                }
//            }.await()
//            if (msg != null) {
//                emit(Resource.Error(msg!!))
//            } else {
//                emit(Resource.Success(list))
//            }
//        }
//    }

    suspend fun getTopics(topics: List<String>): Flow<Resource<List<Topic>>> {
        return flow {
            val list = mutableListOf<Topic>()
            try {
                emit(Resource.Loading())
                val result = suspendCoroutine<Task<QuerySnapshot>> { continuation ->
                    topicRef.whereIn("topic_id", topics).get().addOnCompleteListener { task ->
                        continuation.resume(task)
                    }
                }
                if (result.isSuccessful) {
                    for (i in result.result) {
                        val x = i.toObject<Topic>()
                        list.add(x)
                    }
                    emit(Resource.Success(list))
                } else {
                    emit(Resource.Error(context.getString(R.string.gagal_mendapatkan_data)))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message.toString()))
            }
        }
    }


    private fun isContainUid(list: MutableList<String>): Boolean {
        return list.contains(currentUser?.uid)
    }

//    fun likeForumPost(forumPost: ForumPost): Flow<Resource<Pair<Boolean, String?>>> {
//        return flow {
//            emit(Resource.Loading())
//            val favorite: Boolean
//            val list = forumPost.likes ?: mutableListOf()
//            val s: FieldValue = if (isContainUid(list)) {
//                favorite = false
//                FieldValue.arrayRemove(currentUser?.uid)
//            } else {
//                favorite = true
//                FieldValue.arrayUnion(currentUser?.uid)
//            }
//            var successMsg: Pair<Boolean, String?>? = null
//            var errorMsg = ""
//            forumRef.document(forumPost.id_forum_post).update("likes", s)
//                .addOnCompleteListener {
//                    if (it.isSuccessful) {
//                        Log.d("like","like successful")
//                        forumRef.document(forumPost.id_forum_post)
//                            .update("like_count", FieldValue.increment(if (favorite) 1 else -1))
//                        successMsg = Pair(
//                            favorite,
//                            currentUser?.uid
//                        )
//                    } else {
//                        errorMsg =
//                            if (favorite) "Gagal menyukai postingan" else "Gagal menghapus suka paka postingan"
//                    }
//                }.await()
//            if (successMsg != null) {
//                emit(Resource.Success(successMsg!!))
//            } else emit(Resource.Error(errorMsg))
//        }
//    }


    fun likeForumPost(forumPost: ForumPost): Flow<Resource<Pair<Boolean, String?>>> {
        return flow {
//            emit(Resource.Loading())

            val favorite: Boolean
            val list = forumPost.likes ?: mutableListOf()
            val s: FieldValue = if (isContainUid(list)) {
                favorite = false
                FieldValue.arrayRemove(currentUser?.uid)
            } else {
                favorite = true
                FieldValue.arrayUnion(currentUser?.uid)
            }

            try {
                val result = suspendCancellableCoroutine<Boolean> { continuation ->
                    forumRef.document(forumPost.id_forum_post).update("likes", s)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Log.d("like", "like successful")
//                                forumRef.document(forumPost.id_forum_post)
//                                    .update("like_count", FieldValue.increment(if (favorite) 1 else -1))
                                continuation.resume(true)
                            } else {
                                Log.d("like", "like failed")
                                continuation.resume(false)
                            }
                        }
                }

                val successMsg = Pair(
                    favorite,
                    currentUser?.uid
                )

                emit(Resource.Success(successMsg))
            } catch (e: Exception) {
                val errorMsg =
                    if (favorite) "Gagal menyukai postingan" else "Gagal menghapus suka paka postingan"
                emit(Resource.Error(errorMsg))
            }
        }
    }


//    fun getDetailForum(idForum: String): Flow<Resource<ForumPost>> {
//        var x: ForumPost? = null
//        var msg = context.getString(R.string.gagal_mendapatkan_data)
//        return flow {
//            emit(Resource.Loading())
//            forumRef.document(idForum).get().addOnCompleteListener {
//                if (it.isSuccessful) {
//                    try {
//                        x = it.result.toObject<ForumPost>()
//                    } catch (e: Exception) {
//                        msg = e.message.toString()
//                    }
//                } else {
//                    Log.d("TAG", "unsuccessful")
//                }
//            }.await()
//            if (x != null) {
//                emit(Resource.Success(x!!))
//            } else {
//                emit(Resource.Error(msg))
//            }
//        }
//    }

    fun getDetailForum(idForum: String): Flow<Resource<ForumPost>> {
        return flow {
            emit(Resource.Loading())
            var x: ForumPost? = null
            try {
                val result = suspendCoroutine<Task<DocumentSnapshot>> { continuation ->
                    forumRef.document(idForum).get().addOnCompleteListener { task ->
                        continuation.resume(task)
                    }
                }
                if (result.isSuccessful) {
                    x = result.result?.toObject<ForumPost>()
                    if (x != null) {
                        emit(Resource.Success(x))
                    } else {
                        emit(Resource.Error(context.getString(R.string.gagal_mendapatkan_data)))
                    }
                } else {
                    emit(Resource.Error(context.getString(R.string.gagal_mendapatkan_data)))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message.toString()))
            }
        }
    }


    suspend fun getBestComment(idComment: String): Flow<Resource<CommentForumPost>> {
        return flow {
            var x: CommentForumPost? = null
            emit(Resource.Loading())
            try {
                val result = suspendCoroutine<Task<DocumentSnapshot>> { continuation ->
                    commentRef.document(idComment).get().addOnCompleteListener { task ->
                        continuation.resume(task)
                    }
                }
                if (result.isSuccessful) {
                    x = result.result?.toObject<CommentForumPost>()
                    if (x != null) {
                        emit(Resource.Success(x))
                    } else {
                        emit(Resource.Error(context.getString(R.string.gagal_mendapatkan_data)))
                    }
                } else {
                    emit(Resource.Error(context.getString(R.string.gagal_mendapatkan_data)))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message.toString()))
            }
        }
    }

//    suspend fun sendComment(comment: CommentForumPost): Flow<Resource<String>> {
//        val key = commentRef.document().id
//        var msg: String? = null
//        comment.id_comment = key
//        return flow {
//            emit(Resource.Loading())
//            val x = FieldValue.arrayUnion(key)
//            forumRef.document(comment.id_forum_post).update("comments", FieldValue.arrayUnion(key))
//                .addOnCompleteListener {
//                    if (it.isSuccessful) {
//                        commentRef.document(key).set(comment).addOnCompleteListener {
//                            try {
//                                if (!it.isSuccessful) {
//                                    val exception = it.exception
//                                    msg = exception.toString()
//                                }
//                            } catch (e: Exception) {
//                                msg = e.message
//                            }
//                        }
//                    } else {
//                        msg = "Error adding new comment"
//                    }
//                }.await()
//
//            if (msg != null) {
//                emit(Resource.Error(msg!!))
//            } else {
//                emit(Resource.Success("Comment added successfully"))
//            }
//        }
//    }

    suspend fun sendComment(comment: CommentForumPost): Flow<Resource<String>> {
        val key = commentRef.document().id
        var msg: String? = null
        comment.id_comment = key
        return flow {
            emit(Resource.Loading())
            try {
                val updateResult = suspendCoroutine<Task<Void>> { continuation ->
                    val x = FieldValue.arrayUnion(key)
                    forumRef.document(comment.id_forum_post).update("comments", x)
                        .addOnCompleteListener { task ->
                            continuation.resume(task)
                        }
                }
                if (updateResult.isSuccessful) {
                    val commentResult = suspendCoroutine<Task<Void>> { continuation ->
                        commentRef.document(key).set(comment)
                            .addOnCompleteListener { task ->
                                continuation.resume(task)
                            }
                    }
                    if (commentResult.isSuccessful) {
                        emit(Resource.Success("Comment added successfully"))
                    }
                } else {
                    emit(Resource.Error(context.getString(R.string.gagal_mendapatkan_data)))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message.toString()))
            }
        }
    }


//    suspend fun uploadThread(data: ForumPost, file: Uri?): Flow<Resource<String>> {
//        val key = forumRef.document().id
//        var msg: String? = null
//        data.id_forum_post = key
//        return flow {
//            emit(Resource.Loading())
//            if (file != null) {
//                val r = storageUserRef.child(key).putFile(file).await()
//                if (r !== null) {
//                    if (r.task.isSuccessful) {
//                        storageUserRef.child(key).downloadUrl.addOnCompleteListener { uri ->
//                            data.img_header = uri.result.toString()
//                            forumRef.document(key).set(data).addOnCompleteListener {
//                                try {
//                                    if (!it.isSuccessful) {
//                                        val exception = it.exception
//                                        msg = exception.toString()
//                                    }
//                                } catch (e: Exception) {
//                                    msg = e.message
//                                }
//                            }
//                        }
//                    } else {
//                        msg = "Failed to upload image"
//                    }
//                } else {
//                    msg = "Failed to upload image"
//                }
//            } else {
//                forumRef.document(key).set(data).addOnCompleteListener {
//                    try {
//                        if (!it.isSuccessful) {
//                            val exception = it.exception
//                            msg = exception.toString()
//                        }
//                    } catch (e: Exception) {
//                        msg = e.message
//                    }
//                }
//            }
//
//            if (msg != null) {
//                emit(Resource.Error(msg!!))
//            } else {
//                emit(Resource.Success("Thread uploaded successfully"))
//            }
//        }
//    }

    suspend fun uploadThread(data: ForumPost, file: Uri?): Flow<Resource<String>> {
        val key = forumRef.document().id
        data.id_forum_post = key
        return flow {
            emit(Resource.Loading())

            if (file != null) {
                try {
                    val r = storageUserRef.child(key).putFile(file).await()

                    if (r != null && r.task.isSuccessful) {
                        val uri = storageUserRef.child(key).downloadUrl.await()

                        if (uri != null) {
                            data.img_header = uri.toString()
                            val setResult = suspendCoroutine<Task<Void>> { continuation ->
                                forumRef.document(key).set(data).addOnCompleteListener { task ->
                                    continuation.resume(task)
                                }
                            }
                            if (setResult.isSuccessful) {
                                emit(Resource.Success("Comment added successfully"))
                            }
                        } else {
                            emit(Resource.Error(context.getString(R.string.gagal_mendapatkan_data)))
                        }
                    } else {
                        emit(Resource.Error(context.getString(R.string.gagal_mendapatkan_data)))
                    }
                } catch (e: Exception) {
                    emit(Resource.Error(e.message.toString()))
                }
            } else {
                val setResult = suspendCoroutine<Task<Void>> { continuation ->
                    forumRef.document(key).set(data).addOnCompleteListener { task ->
                        continuation.resume(task)
                    }
                }

                if (setResult.isSuccessful) {
                    emit(Resource.Success("Comment added successfully"))
                }
            }
        }
    }
}