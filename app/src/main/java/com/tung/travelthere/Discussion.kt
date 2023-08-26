package com.tung.travelthere

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
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
            .whereEqualTo(cityNameField, location.city.getName()).limit(1).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentSnapshot = querySnapshot.documents[0]
                    documentSnapshot.reference.collection(
                        collectionLocations
                    ).whereEqualTo(locationNameField,location.getName()).limit(1).get()
                        .addOnSuccessListener {
                            querySnapshot2 ->
                            if (!querySnapshot2.isEmpty){
                                val documentSnapshot2 = querySnapshot2.documents[0]
                                documentSnapshot2.reference.collection(collectionDiscussions).document(id).get()
                                    .addOnSuccessListener {

                                    }
                            }
                        }
                }
            }

    }

    fun fetchUpdateDiscussion(){

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

