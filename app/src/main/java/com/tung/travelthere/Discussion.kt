package com.tung.travelthere

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentChange
import com.tung.travelthere.controller.*
import com.tung.travelthere.objects.Location

class Discussion(val name: String, private val location: Location) : ViewModel() {
    lateinit var id: String
    inner class DiscussionContent(private val sender: String, private val content: String) {
        fun getSender(): String {
            return sender
        }

        fun getContent(): String {
            return content
        }
    }

    var contents =
        mutableStateListOf<DiscussionContent>() //những nội dung bên trong một đoạn discussion

    fun fetchCurrentDiscussion() { //fetch mới
        if (id==null){
            return //id null nghĩa là chưa up lên firebase
        }

        AppController.db.collection(collectionCities)
            .whereEqualTo(cityNameField, location.city.getName()).limit(1).get() //lấy document city tương ứng
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentSnapshot = querySnapshot.documents[0]
                    documentSnapshot.reference.collection(
                        collectionLocations
                    ).whereEqualTo(locationNameField,location.getName()).limit(1).get() //lấy document location tương ứng
                        .addOnSuccessListener {
                            querySnapshot2 ->
                            if (!querySnapshot2.isEmpty){
                                val documentSnapshot2 = querySnapshot2.documents[0]
                                documentSnapshot2.reference.collection("discussions").document(id).get() //lấy discussion
                                    .addOnSuccessListener {
                                        documentSnapshot3 ->
                                        if (documentSnapshot3!=null&&documentSnapshot3.exists()){
                                            documentSnapshot3.reference.collection("contents").get().addOnSuccessListener { //lấy nội dung discussion
                                                documents ->
                                                for (document in documents){ //đọc từng reply trong discussion
                                                    val sender = document.get("sender") as String
                                                    val content = document.get("content") as String

                                                    contents.add(DiscussionContent(sender,content))
                                                }
                                            }
                                        }
                                    }

                            }
                        }
                }
            }

    }

    fun fetchUpdateDiscussion(){
        if (id==null){
            return //id null nghĩa là chưa up lên firebase
        }

        AppController.db.collection(collectionCities)
            .whereEqualTo(cityNameField, location.city.getName()).limit(1).get() //lấy document city tương ứng
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentSnapshot = querySnapshot.documents[0]
                    documentSnapshot.reference.collection(
                        collectionLocations
                    ).whereEqualTo(locationNameField,location.getName()).limit(1).get() //lấy document location tương ứng
                        .addOnSuccessListener {
                                querySnapshot2 ->
                            if (!querySnapshot2.isEmpty){
                                val documentSnapshot2 = querySnapshot2.documents[0]
                                documentSnapshot2.reference.collection("discussions").document(id).get() //lấy discussion
                                    .addOnSuccessListener {
                                            documentSnapshot3 ->
                                        if (documentSnapshot3!=null&&documentSnapshot3.exists()){
                                            documentSnapshot3.reference.collection("contents").addSnapshotListener {
                                                    value, error ->
                                                if (error!=null){
                                                    return@addSnapshotListener
                                                }

                                                for (docChange in value!!.documentChanges){
                                                    if (docChange.type == DocumentChange.Type.ADDED){ //có tin nhắn mới
                                                        val newMessageData = docChange.document.data

                                                        val sender = newMessageData.get("sender") as String
                                                        val content =  newMessageData.get("content") as String

                                                        contents.add(DiscussionContent(sender,content))
                                                    }
                                                }
                                            } //cập nhật những reply mới
                                        }
                                    }

                            }
                        }
                }
            }
    }

    fun updateDiscussion() {
    }
}

class DiscussionsViewModel : ViewModel() {

}

@Composable
fun discussionUI() {

}

@Composable
fun discussionList() {
}

@Composable
fun discussionView() {

}

