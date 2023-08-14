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
    private var imageUrl: String?=null
    private var description: String?=null
    suspend fun getImageUrl(): String = withContext(Dispatchers.IO) {
        if (imageUrl!=null){
            return@withContext imageUrl!!
        }
        Log.d("url has","false")
        val client = OkHttpClient()
        val url =
            "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=pageimages&titles=$name&pithumbsize=500"
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("$response")
            val jsonResponse = response.body?.string() ?: ""
            val jsonObject = JSONObject(jsonResponse)
            val pages = jsonObject.getJSONObject("query").getJSONObject("pages")
            val pageId = pages.keys().next()
            val json = pages.getJSONObject(pageId).optJSONObject("thumbnail")
            val url = json?.getString("source") ?: ""
            imageUrl=url
            Log.d("url",url)
            return@use url
        }

    }


    suspend fun getDescription(): String = withContext(Dispatchers.IO) {
        if (description!=null){
            return@withContext description!!
        }
        Log.d("desc has","false")
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
            val desc = pages.getJSONObject(pageId).getString("extract")
            description=desc
            Log.d("desc",desc)
            return@use desc
        }
    }

}