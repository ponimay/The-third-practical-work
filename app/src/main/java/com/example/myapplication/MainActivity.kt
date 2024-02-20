package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var textView: TextView
    private lateinit var textView6: TextView
    private var likes = 150
    private var share = 999

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textView6)
        textView6 = findViewById(R.id.textView)
        var imageButton2: ImageButton = (findViewById(R.id.imageButton3))
        var imageButton: ImageButton = (findViewById(R.id.imageButton))
        var imageButton3: ImageButton = (findViewById(R.id.imageButton2))
        imageButton.setOnClickListener {
            likes++
            updatelikes()
            imageButton2.visibility = View.VISIBLE
            imageButton.visibility = View.INVISIBLE
        }
        imageButton2.setOnClickListener {
            likes--
            updatelikes()

            imageButton.visibility = View.VISIBLE
            imageButton2.visibility = View.INVISIBLE
        }
        imageButton3.setOnClickListener {
            share += 10
            updateshare()

        }
    }
    private fun updateshare() {
        textView6.text = if (share > 999) {
            String.format("%.1fK", share / 1000.0)
        } else {
            share.toString()
        }
    }
    private fun updatelikes() {
        textView.text = if (likes > 999) {
            String.format("%.1fK", likes / 1000.0)
        } else {
            likes.toString()
        }
    }
}
