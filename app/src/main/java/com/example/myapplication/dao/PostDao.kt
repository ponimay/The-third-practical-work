package com.example.myapplication.dao

import com.example.myapplication.post.Post

interface PostDao {
    fun getAll(): List<Post>
    fun save(post: Post): Post
    fun likeById(id: Long)
    fun shareById(id: Long)
    fun removeById(id: Long)
    fun update(post: Post)

}
