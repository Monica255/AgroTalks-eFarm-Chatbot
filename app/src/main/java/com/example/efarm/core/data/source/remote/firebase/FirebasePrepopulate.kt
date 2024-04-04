package com.example.efarm.core.data.source.local

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.example.efarm.core.data.source.remote.model.ForumPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FirebasePrepopulate @Inject constructor(
    firebaseFirestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) {
    private val forumRef = firebaseFirestore.collection("forum_posts")
    suspend fun prepopulate(){
        val list = mutableListOf<ForumPost>()
        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString() ?: ""
        try {
            val myInput: InputStream
            // initialize asset manager
            val assetManager: AssetManager = context.assets
            // open excel file name as dataset.xlsx
            myInput = assetManager.open("dataset.xlsx")
            // Create a workbook using XSSFWorkbook (for XLSX files)
            val workBook = XSSFWorkbook(myInput)
            // Get the first sheet from workbook
            val sheet = workBook.getSheetAt(0)
            for (rowIndex in 1 until sheet.physicalNumberOfRows) {
                val row = sheet.getRow(rowIndex)
                // Get values from cells
                val id_forum_post=row.getCell(1).stringCellValue
                val title=row.getCell(3).stringCellValue
                val content = row.getCell(4).stringCellValue
                val img = row.getCell(5).stringCellValue
                val time = row.getCell(6).numericCellValue.toLong()
                val topic = row.getCell(7).stringCellValue
                val verified = row.getCell(8).stringCellValue
                val link = row.getCell(9).stringCellValue
                val topics = topic.split(";").map { it.trim().toString() }
                if(topics.isEmpty()){
                    return
                }
                // Create an object and add it to the list
                val post = ForumPost(id_forum_post,uid,title, content,img,time,null,null,topics,verified,link)
                list.add(post)

                try {
                    // Add data to Firestore
                    forumRef.document(id_forum_post).set(post).await()
                } catch (e: Exception) {
                    Log.e("prepopulate", "Failed to add data", e)
                }
            }
            workBook.close()
        } catch (e: Exception) {
            Log.d("prepopulate","error "+e.stackTrace.toString())
            e.printStackTrace()
        }
    }
}
