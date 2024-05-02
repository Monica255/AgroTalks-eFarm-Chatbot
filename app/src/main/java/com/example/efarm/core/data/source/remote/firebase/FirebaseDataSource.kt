package com.example.efarm.core.data.source.remote.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.eFarm.R
import com.example.efarm.core.data.Resource
import com.example.efarm.core.data.source.remote.model.Chat
import com.example.efarm.core.data.source.remote.model.CommentForumPost
import com.example.efarm.core.data.source.remote.model.ForumPost
import com.example.efarm.core.data.source.remote.model.Images
import com.example.efarm.core.data.source.remote.model.Topic
import com.example.efarm.core.data.source.remote.model.UserData
import com.example.efarm.core.util.KategoriTopik
import com.example.efarm.core.util.VoteType
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
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
import java.io.File
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
    private val storageUserRef = firebaseStorage.reference.child("thread_headers")
    private val userDataRef = firebaseDatabase.reference.child("user_data/")
    private val chatsRef = firebaseDatabase.reference.child("chats/")
    private val commentRef = firebaseFirestore.collection("forum_comments")

    fun voteComment(
        comment: CommentForumPost,
        voteType: VoteType
    ): Flow<Resource<Pair<Boolean, String?>>> {
        return flow {
            val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
            val vote: Boolean
            val list = (if (voteType == VoteType.UP) comment.upvotes else comment.downvotes)
                ?: mutableListOf()
            val list2 = (if (voteType == VoteType.UP) comment.downvotes else comment.upvotes)
                ?: mutableListOf()
            var z = false
            val s: FieldValue = if (isContainUid(list)) {
                vote = false
                FieldValue.arrayRemove(uid)
            } else {
                vote = true
                z = (isContainUid(list2))
                FieldValue.arrayUnion(uid)
            }

            try {
                val result = suspendCancellableCoroutine<Boolean> { continuation ->
                    commentRef.document(comment.id_comment).update(voteType.printable, s)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                if (vote && z) {
                                    val d: FieldValue = FieldValue.arrayRemove(uid)
                                    commentRef.document(comment.id_comment).update(
                                        if (voteType == VoteType.UP) VoteType.DOWN.printable else VoteType.UP.printable,
                                        d
                                    ).addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            continuation.resume(true)
                                        } else {
                                            continuation.resume(false)
                                        }
                                    }
                                } else {
                                    continuation.resume(true)
                                }
                            } else {
                                continuation.resume(false)
                            }
                        }
                }

                val successMsg = Pair(
                    vote, uid
                )

                emit(Resource.Success(successMsg))
            } catch (e: Exception) {
                val errorMsg =
                    if (vote) "Gagal voting up" else "Gagal voting down"
                emit(Resource.Error(errorMsg))
            }
        }
    }

    fun verifyForumPost(
        forumPost: ForumPost,
        verify: String?
    ): Flow<Resource<Pair<String?, String?>>> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        return flow {
            try {
                val result = suspendCancellableCoroutine<Boolean> { continuation ->
                    forumRef.document(forumPost.id_forum_post).update("verified", verify)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                continuation.resume(true)
                            } else {
                                continuation.resume(false)
                            }
                        }
                }

                val successMsg = Pair(
                    verify,
                    uid
                )

                emit(Resource.Success(successMsg))
            } catch (e: Exception) {
                val errorMsg = "Gagal melakukan verifikasi"
                emit(Resource.Error(errorMsg))
            }
        }
    }


    fun getChats(): MutableLiveData<List<Chat>?> {
        val chats = MutableLiveData<List<Chat>?>()
        val x = FirebaseAuth.getInstance().currentUser?.uid.toString()
        x?.let {
            chatsRef.child(it)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
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
                        } else {
                            chats.value = listOf()
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
        val x = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val data2 = Chat(key, data.actor, data.message, data.timestamp)

        x?.let { id ->
            chatsRef.child(id).child(key!!).setValue(data2).addOnCompleteListener {
                if (it.isSuccessful) {
                    if (!data.thread.isNullOrEmpty()) {
                        chatsRef.child(id).child(key).child("thread").setValue(data.thread)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    chat.value = Resource.Success("Berhasil mengirim data")
                                } else {
                                    chat.value = Resource.Error("gagal mengirim data")
                                }
                            }
                    } else {
                        chat.value = Resource.Success("Berhasil mengirim data")
                    }
                } else {
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
        val currentUser = FirebaseAuth.getInstance().currentUser
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
                    } else {
                        emit(Resource.Error(context.getString(R.string.email_atau_password_mungkin_salah)))
                    }
                }
            } catch (e: Exception) {
                emit(Resource.Error(context.getString(R.string.email_atau_password_mungkin_salah)))
                Log.e("TAG", e.toString())
            }
        }
    }

    fun getUserData(uid: String?): MutableLiveData<UserData?> {
        val userData = MutableLiveData<UserData?>()
        val x = uid ?: FirebaseAuth.getInstance().currentUser?.uid.toString()
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
        if (FirebaseAuth.getInstance().currentUser != null) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.resources.getString(R.string.default_web_client_id_2))
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            googleSignInClient.signOut()
            firebaseAuth.signOut()
        }
    }

    private val forumRef = firebaseFirestore.collection("forum_posts")
    fun getPagingForum(
        topic: Topic? = null,
        self: Boolean = false
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
        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        if (self) query = query.whereEqualTo("user_id", uid)

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

    private val topicRef = firebaseFirestore.collection("topics")
    private fun getQueryTopicByCategory(kategori: KategoriTopik): Query {
        return if (kategori != KategoriTopik.SEMUA) {
            topicRef.orderBy("topic_name", Query.Direction.ASCENDING)
                .whereEqualTo("topic_category", kategori.printable)
        } else {
            topicRef.orderBy("topic_name", Query.Direction.ASCENDING)
        }
    }

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
        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        return list.contains(uid)
    }

    fun likeForumPost(forumPost: ForumPost): Flow<Resource<Pair<Boolean, String?>>> {
        return flow {
            val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
            val favorite: Boolean
            val list = forumPost.likes ?: mutableListOf()
            val s: FieldValue = if (isContainUid(list)) {
                favorite = false
                FieldValue.arrayRemove(uid)
            } else {
                favorite = true
                FieldValue.arrayUnion(uid)
            }

            try {
                val result = suspendCancellableCoroutine<Boolean> { continuation ->
                    forumRef.document(forumPost.id_forum_post).update("likes", s)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                continuation.resume(true)
                            } else {
                                continuation.resume(false)
                            }
                        }
                }

                val successMsg = Pair(
                    favorite, uid
                )

                emit(Resource.Success(successMsg))
            } catch (e: Exception) {
                val errorMsg =
                    if (favorite) "Gagal menyukai postingan" else "Gagal menghapus suka paka postingan"
                emit(Resource.Error(errorMsg))
            }
        }
    }

    fun deleteForumPost(forumPost: ForumPost): Flow<Resource<String?>> {
        val errorMsg = "Gagal menghapus postingan"
        return flow {
            emit(Resource.Loading())
            try {
                if ((forumPost.img_header != null && forumPost.img_header != "")||(forumPost.thread?.images!=null&&forumPost.thread?.images?.isNotEmpty()==true)) {
                    suspendCancellableCoroutine<Boolean> { con ->
                        storageUserRef.child(forumPost.id_forum_post).listAll()
                            .addOnSuccessListener { listResult ->
                                val item = listResult.items
                                if (item.isNotEmpty()) {
                                    listResult.items.forEach { item ->
                                        item.delete()
                                    }
                                }
                                con.resume(true)
                            }.addOnFailureListener { exception ->
                                con.resume(false)
                        }
                    }

                }
                suspendCancellableCoroutine<Boolean> { continuation ->
                    forumRef.document(forumPost.id_forum_post).delete()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                continuation.resume(true)
                            } else {
                                continuation.resume(false)
                            }
                        }
                }
                emit(Resource.Success("Berhasil menghapus postingan"))
            } catch (e: Exception) {
                emit(Resource.Error(errorMsg))
            }
        }
    }

    fun getDetailForum(idForum: String): Flow<Resource<ForumPost>> {
        return flow {
            emit(Resource.Loading())
            var x: ForumPost? = null
            try {
                val result = suspendCoroutine<Task<DocumentSnapshot>> { continuation ->
                    forumRef.document(idForum).get().addOnCompleteListener { task ->
                        Log.d("detail","tak running")
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
                        emit(Resource.Success("Komentar berhasil ditambahkan"))
                    }
                } else {
                    emit(Resource.Error(context.getString(R.string.gagal_mendapatkan_data)))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message.toString()))
            }
        }
    }
    suspend fun firebaseAuthWithGoogle(idToken: String): Flow<Resource<String>> {
        return flow {
            try {
                emit(Resource.Loading())
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = firebaseAuth.signInWithCredential(credential).await()
                result?.let {
                    if (result.user != null) {
                        Log.d("signin","success sign in")
                        val x = setUserData()
                        emit(Resource.Success(x.second))
                    } else {
                        Log.d("signin","error sign in")
                        emit(Resource.Error<String>("Error"))
                    }
                } ?: emit(Resource.Error<String>("Error"))

            } catch (e: Exception) {

                emit(Resource.Error(e.toString()))
                Log.e("signin", "cc "+e.toString())
            }

        }.flowOn(Dispatchers.IO)
    }
    suspend fun uploadThread(data: ForumPost, file: Uri?): Flow<Resource<String>> {
        val key = forumRef.document().id
        if(data.id_forum_post=="") data.id_forum_post = key

        val listImages = mutableListOf<Images>()
        return flow {
            try {
                emit(Resource.Loading())
                if (file != null && file.path != null) {
                    val mFile = File(file.path)
                    Log.d("edit",mFile.name)
                    try {
                        val r = storageUserRef.child(data.id_forum_post).child(data.id_forum_post).putFile(file).await()
                        Log.d("edit","aaa")
                        if (r != null && r.task.isSuccessful) {
                            val uri = storageUserRef.child(data.id_forum_post).child(data.id_forum_post).downloadUrl.await()
                            if (uri != null) data.img_header = uri.toString()
                        } else {
                            emit(Resource.Error(context.getString(R.string.gagal_mendapatkan_data)))
                        }
                    }catch (e:Exception){
                        data.img_header = file.toString()
                        Log.d("edit","xxx")
                        //image already exist
                    }

                }
                data.thread?.images?.forEachIndexed { index, it ->
                    it.image?.let {img->
                        val mFile = File(img.toUri().path)
                        Log.d("edit",mFile.name)
                        try {
                            val r = storageUserRef.child(data.id_forum_post).child(mFile.name).putFile(img.toUri()).await()
                            if (r != null && r.task.isSuccessful) {
                                val uri = storageUserRef.child(data.id_forum_post).child(mFile.name).downloadUrl.await()
                                if (uri != null) listImages.add(index,Images(it.position,uri.toString()))
                            } else {
                                emit(Resource.Error(context.getString(R.string.gagal_mendapatkan_data)))
                            }
                        }catch (e:Exception){
                            //image already exist
                            listImages.add(index,Images(it.position,img))
                        }
                    }
                }
                data.thread?.images=listImages
                val setResult = suspendCoroutine<Task<Void>> { continuation ->
                    forumRef.document(data.id_forum_post).set(data).addOnCompleteListener { task ->
                        continuation.resume(task)
                    }
                }
                if (setResult.isSuccessful) {
                    emit(Resource.Success("Berhasil membagikan"))
                }
            } catch (e: Exception) {
                Log.d("edit","edit "+e.message.toString())
                emit(Resource.Error("Photo: " + e.message.toString()))
            }
        }
    }
}