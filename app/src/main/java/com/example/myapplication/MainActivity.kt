package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.databinding.ActivityMainBinding
import kotlin.properties.ReadOnlyProperty

class MainActivity : AppCompatActivity() {
    private lateinit var textView: TextView
    private lateinit var textView6: TextView
    private var likes = 150
    private var share = 999

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel:PostViewModel by viewModels()
        viewModel.data.observe(this){post->
            with(binding){
                textView2.text = post.author
                textView3.text = post.published
                textView4.text = post.content


            }

        }



        textView = findViewById(R.id.textView6)
        textView6 = findViewById(R.id.textView)
        var imageButton2: ImageButton = (findViewById(R.id.imageButton3))
        var imageButton: ImageButton = (findViewById(R.id.imageButton))
        var imageButton3: ImageButton = (findViewById(R.id.imageButton2))
        var imageView: ImageView = (findViewById(R.id.imageView))
        var root: ConstraintLayout = (findViewById(R.id.root))

        root.setOnClickListener {
           Toast.makeText(this, "Вы нажали на ConstraintLayout", Toast.LENGTH_SHORT).show()
        }

        imageButton.setOnClickListener {
            likes++
            Toast.makeText(this, "Вы лайкнули новый пост", Toast.LENGTH_SHORT).show()

            updatelikes()
            imageButton2.visibility = View.VISIBLE
            imageButton.visibility = View.INVISIBLE
        }
        imageButton2.setOnClickListener {
            likes--
           Toast.makeText(this, "Вы дизлайкнули новый пост", Toast.LENGTH_SHORT).show()

            updatelikes()

            imageButton.visibility = View.VISIBLE
            imageButton2.visibility = View.INVISIBLE
        }

        imageButton3.setOnClickListener {
            share += 10
           Toast.makeText(this, "Вы поделились новым постом", Toast.LENGTH_SHORT).show()

            updateshare()
        }

        imageView.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            Toast.makeText(this, "Вы обновили страницу", Toast.LENGTH_SHORT).show()
            startActivity(intent)
        }
    }
    private fun updateshare() {
        textView6.text = if (share > 999) {
            String.format("%.1fK", share / 1000.0)
        } else if(share > 999999) {
            String.format("%.1fM", share / 1000_000.0)
        }
        else{
            share.toString()


        }
    }
    private fun updatelikes() {
        textView.text = if (likes > 999) {
            String.format("%.1fK", likes / 1000.0)
        } else if(likes > 999999) {
            String.format("%.1fM", likes / 1000_000.0)
        }
        else{
            likes.toString()


        }
    }
}

data class Post(
    val id:Long,
    val author:String,
    val content:String,
    val published:String,
    val likedByMe:Boolean,
)
interface PostRepository{
    fun get(): LiveData<Post>
    fun like()
}
class PostRepositoryInMemoryImpl:PostRepository{
    private var post = Post(
        id = 1,
        author = "ГБПОУ ВО 'БТПИТ'",
        content = "Студенты 1-4 курсов специальности: \"Дошкольное образование\" Борисоглебского колледжа промышленных и информационных технологий, совместно с преподавателями Ларисой Владимировной Гребенниковой и Маргаритой Михайловной Луговиной, 13 февраля, на базе Борисоглебского филиала ВИРО им. Н.Ф. Бунакова, в рамках мероприятий Наставник клуба молодых педагогов, принял участие в семинаре на тему: \"Методы и методички формирования исследовательского поведения ребенка как условие его саморазвития\".\"В ходе этого образовательного интенсива Ольга Николаевна, региональный методист, педагог-психолог МБДОУ \"Новохоперский центр развития ребенка \"Пристань детства\", поделилась своим опытом работы. Подробнее читайте на нашем сайте -> https://btpit36.ru",
        published = "20 февраля в 09:00",
        likedByMe = false
    )
    private val data = MutableLiveData(post)


    override fun get(): LiveData<Post> = data

    override fun like() {
        post = post.copy(likedByMe = !post.likedByMe)
        data.value = post
    }
}
class PostViewModel: ViewModel() {
    private val repository:PostRepository = PostRepositoryInMemoryImpl()
    val data = repository.get()
    fun like() = repository.like()
}

