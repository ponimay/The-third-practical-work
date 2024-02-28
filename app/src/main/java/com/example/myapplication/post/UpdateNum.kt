package com.example.myapplication.post

object UpdateNumber {
    fun countAmountFormat(num: Int): String {
        return when {
            num < 1000 -> num.toString()
            num < 1000000 -> String.format("%.1fK", num / 1000.0)
            else -> String.format("%.1fM", num / 1000000.0)
        }
    }
}