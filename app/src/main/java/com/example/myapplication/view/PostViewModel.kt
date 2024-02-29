package com.example.myapplication.view

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.database.AppDb
import com.example.myapplication.database.PostRepositorySQLiteImpl
import com.example.myapplication.post.Post
import com.example.myapplication.repos.PostRepository

val empty = Post(
    id = 0,
    content = "",
    author = "Я",
    likedByMe = false,
    published = "Сейчас",
    liketxt = 0,
    sharetxt = 0,
    shareByMe = false,
    video = ""
)
class PostViewModel(application: Application): AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositorySQLiteImpl(
                AppDb.getInstance(application).postDao
    )
    val data: LiveData<List<Post>> = repository.getData()
    //val data: LiveData<List<Post>> = repository.getAll() - работает Json файл!!!


    fun likeById(id: Long) = repository.likeById(id)

    fun shareById(id: Long) = repository.shareById(id)

    fun removeById(id: Long) = repository.removeById(id)


    val edited = MutableLiveData(empty)

    fun save() {
        edited.value?.let {
            repository.save(it)
        }

        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun clearEditing() {
        edited.value = empty
    }

    fun changeContent(content: String) {
        edited.value?.let {
            val text = content.trim()
            if (edited.value?.content == text) {
                return
            }
            edited.value = edited.value?.copy(content = text)
        }
    }
}