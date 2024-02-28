package com.example.myapplication.post

data class Post(
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    var likedByMe: Boolean,
    var liketxt:Int,
    var sharetxt:Int,
    val shareByMe: Boolean,
    val video:String?
)