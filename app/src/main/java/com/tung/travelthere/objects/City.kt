package com.tung.travelthere.objects
import okhttp3.OkHttpClient
import okhttp3.Request
import android.app.DownloadManager
import android.media.Image
import android.os.Debug
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject

class City(val name: String) {
    fun getImageUrl(): Flow<String> = flow {
        val imageInfo = withContext(Dispatchers.IO){
            val client = OkHttpClient()
            val url = "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=pageimages&titles=$name&pithumbsize=500"
            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("$response")
                val jsonResponse = response.body?.string() ?: ""
                val jsonObject = JSONObject(jsonResponse)
                val pages = jsonObject.getJSONObject("query").getJSONObject("pages")
                val pageId = pages.keys().next()
                pages.getJSONObject(pageId).optJSONObject("thumbnail")

            }
        }
        emit(imageInfo?.getString("source")?:"")
    }

    fun getDescription(): Flow<String> = flow { //lấy mô tả của thành phố từ wikipedia
        val description = withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val url =
                "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&exintro=true&titles=$name"
            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("$response")
                val jsonResponse = response.body?.string() ?: ""
                val jsonObject = JSONObject(jsonResponse)
                val pages = jsonObject.getJSONObject("query").getJSONObject("pages")
                val pageId = pages.keys().next()
                pages.getJSONObject(pageId).getString("extract")

            }
        }
        Log.d("description",description)
        emit(description)
    }
}