package com.example.myapplication.repos

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.post.Post
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.io.IOException

class PostRepositoryInMemoryImpl(
    private val context: Context
) : PostRepository {
    override fun getAll(): LiveData<List<Post>> {
        val jsonString = loadJsonFromAsset("publication.json")
        val jsonObject = JSONObject(jsonString)
        val postData = jsonObject.getJSONObject("data")
        val post = Gson().fromJson(postData.toString(), Post::class.java)
        return MutableLiveData(listOf(post))
    }

    private fun loadJsonFromAsset(fileName: String): String {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            ""
        }
    }

    private val FILE_NAME = "publication.json"
    private val gson = Gson()
    private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type
    private var posts: List<Post> = readPosts()
        set(value) {
            field = value
            sync()
        }

    private val data = MutableLiveData(posts)
    override fun getData(): LiveData<List<Post>> = data


    private fun sync() {
        context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).bufferedWriter().use {
            it.write(gson.toJson(data.value))
        }
    }

    private fun readPosts(): List<Post> {
        val file = context.filesDir.resolve(FILE_NAME)

        return if (file.exists()) {
            context.openFileInput(FILE_NAME).bufferedReader().use {
                gson.fromJson(it, type)
            }
        } else {
            emptyList()
        }
    }


    override fun save(post: Post) {
        val existingPosts = data.value.orEmpty().toMutableList()
        if (post.id == 0L) {
            val newPost = post.copy(id = existingPosts.firstOrNull()?.id ?: 0L+1)
            existingPosts.add(0, newPost)
        } else {
            val index = existingPosts.indexOfFirst { it.id == post.id }
            if (index != -1) {
                existingPosts[index] = post
            }
        }
        data.value = existingPosts
    }


    override fun likeById(id: Long) {
        val existingPosts = data.value.orEmpty().toMutableList()
        val index = existingPosts.indexOfFirst { it.id == id }
        if (index != -1) {
            val post = existingPosts[index]
            existingPosts[index] = post.copy(
                likedByMe = !post.likedByMe,
                liketxt = if (post.likedByMe) post.liketxt - 1 else post.liketxt + 1
            )
            save(existingPosts[index])
        }
        sync()
    }


    override fun shareById(id: Long) {
        val existingPosts = data.value.orEmpty().toMutableList()
        val index = existingPosts.indexOfFirst { it.id == id }
        if (index != -1) {
            val post = existingPosts[index]
            existingPosts[index] = post.copy(
                shareByMe = !post.shareByMe,
                sharetxt = if (post.shareByMe) post.sharetxt - 1 else post.sharetxt + 1
            )
            save(existingPosts[index])
        }
        sync()
    }
    override fun removeById(id: Long) {
        val existingPosts = data.value.orEmpty().toMutableList()
        val postToRemove = existingPosts.firstOrNull { it.id == id }
        if (postToRemove != null) {
            existingPosts.remove(postToRemove)
            for (post in existingPosts) {
                if (post.likedByMe) {
                    post.likedByMe = false
                    post.liketxt = post.liketxt - 1
                }
            }
            data.value = existingPosts
        }
    }

}