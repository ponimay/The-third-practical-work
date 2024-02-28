package com.example.myapplication.repos

import androidx.lifecycle.LiveData
import com.example.myapplication.post.Post

interface PostRepository {
    fun getAll(): LiveData<List<Post>>
    fun getData(): LiveData<List<Post>>
    fun likeById(id: Long)
    fun shareById(id: Long)
    fun removeById(id: Long)
    fun save(post: Post)
}