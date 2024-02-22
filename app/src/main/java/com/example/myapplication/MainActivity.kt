package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val viewModel: PostViewModel by viewModels()
        viewModel.data.observe(this) { post ->
            with(binding) {
                textView2.text = post.author
                textView3.text = post.published
                textView4.text = post.content
                imageButton.setImageResource(
                    if (post.likedByMe)
                        R.drawable.img_4
                    else
                        R.drawable.img
                )
                textView6.text = updateNumber(post.liketxt)
                textView.text = updateNumber(post.sharetxt)
            }

        }
        binding.imageButton.setOnClickListener {
            Toast.makeText(this, "Вы лайкнули новый пост", Toast.LENGTH_SHORT).show()
            viewModel.like()
        }
        binding.imageButton2.setOnClickListener {//для репоста
            Toast.makeText(this, "Вы репостнули новый пост", Toast.LENGTH_SHORT).show()
            viewModel.share()

        }

    }

    private fun updateNumber(count: Int): String{
        return when {
            count < 1000 -> count.toString()
            count < 1000000 -> String.format("%.1fK", count / 1000.0)
            else -> String.format("%.1fM", count / 1000000.0)
        }
    }
}
data class Post(
    val id:Long,
    val author:String,
    val content:String,
    val published:String,
    val likedByMe:Boolean,
    var liketxt:Int,
    var sharetxt:Int,
    val shareByMe: Boolean,
)

interface PostRepository{
    fun get(): LiveData<Post>
    fun like()
    fun share()
}

class PostRepositoryInMemoryImpl:PostRepository{
    private var post = Post(
        id = 1,
        author = "ГБПОУ ВО 'БТПИТ'",
        content = "Студенты 1-4 курсов специальности: \"Дошкольное образование\" Борисоглебского колледжа промышленных и информационных технологий, совместно с преподавателями Ларисой Владимировной Гребенниковой и Маргаритой Михайловной Луговиной, 13 февраля, на базе Борисоглебского филиала ВИРО им. Н.Ф. Бунакова, в рамках мероприятий Наставник клуба молодых педагогов, принял участие в семинаре на тему: \"Методы и методички формирования исследовательского поведения ребенка как условие его саморазвития\".\"В ходе этого образовательного интенсива Ольга Николаевна, региональный методист, педагог-психолог МБДОУ \"Новохоперский центр развития ребенка \"Пристань детства\", поделилась своим опытом работы. Подробнее читайте на нашем сайте -> https://btpit36.ru",
        published = "20 февраля в 09:00",
        likedByMe = false,
        liketxt = 999,
        sharetxt = 999,
        shareByMe = false
    )
    private val data = MutableLiveData(post)


    override fun get(): LiveData<Post> = data

    override fun like() {
        if (!post.likedByMe) {
            post.liketxt++
        } else {
            post.liketxt--
        }
        post = post.copy(likedByMe = !post.likedByMe)
        data.value = post
    }
    override fun share() {
        if (!post.shareByMe) {
            post.sharetxt++
        } else {
            post.sharetxt--
        }
        post = post.copy(shareByMe = !post.shareByMe)
        data.value = post
    }
}
class PostViewModel: ViewModel() {
    private val repository:PostRepository = PostRepositoryInMemoryImpl()
    val data = repository.get()
    fun like() = repository.like()
    fun share() = repository.share()
}

