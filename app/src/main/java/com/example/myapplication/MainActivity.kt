package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.databinding.CardPostBinding



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val viewModel: PostViewModel by viewModels()
        val adapter = PostAdapter(
            onLikeListener = { post ->
                viewModel.likeById(post.id)
            },
            onShareListener = { post ->
                viewModel.shareById(post.id)
            }
        )
        binding.recycleView.adapter = adapter
        viewModel.data.observe(this) { post ->
            adapter.submitList(post)
        }
    }
}

typealias OnLikeListener = (post: Post) -> Unit
typealias onShareListener = (post: Post) -> Unit


class PostAdapter(
    private val onLikeListener: OnLikeListener,
    private val onShareListener: onShareListener
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding =
            CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onLikeListener, onShareListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)

    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}



class PostViewHolder(
    private val binding: CardPostBinding,
    private val onLikeListener: OnLikeListener,
    private val onShareListener: onShareListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
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

            imageButton.setOnClickListener {
                onLikeListener(post)
                if (post.likedByMe) {
                    post.liketxt++
                } else {
                    post.liketxt--
                }
            }
            imageButton2.setOnClickListener {
                onShareListener(post)
                if (post.shareByMe) {
                    post.sharetxt++
                } else {
                    post.sharetxt--
                }
            }

        }
    }
}





fun updateNumber(count: Int): String {
    return when {
        count < 1000 -> count.toString()
        count < 1000000 -> String.format("%.1fK", count / 1000.0)
        else -> String.format("%.1fM", count / 1000000.0)
    }
}
data class Post(
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    var liketxt:Int,
    var sharetxt:Int,
    val shareByMe: Boolean,
)



interface PostRepository {
    fun getAll(): LiveData<List<Post>>
    fun likeById(id: Long)
    fun shareById(id: Long)
}

class PostViewModel : ViewModel() {
    private val repository: PostRepository = PostRepositoryInMemoryImpl()
    val data = repository.getAll()

    fun likeById(id: Long) = repository.likeById(id)

    fun shareById(id: Long) = repository.shareById(id)
}

class PostRepositoryInMemoryImpl : PostRepository {
    private var post = listOf(
        Post(
            id = 1,
            author = "ГБПОУ ВО 'БТПИТ'",
            content = "Студенты 1-4 курсов специальности: \"Дошкольное образование\" Борисоглебского колледжа промышленных и информационных технологий, совместно с преподавателями Ларисой Владимировной Гребенниковой и Маргаритой Михайловной Луговиной, 13 февраля, на базе Борисоглебского филиала ВИРО им. Н.Ф. Бунакова, в рамках мероприятий Наставник клуба молодых педагогов, принял участие в семинаре на тему: \"Методы и методички формирования исследовательского поведения ребенка как условие его саморазвития\".\"В ходе этого образовательного интенсива Ольга Николаевна, региональный методист, педагог-психолог МБДОУ \"Новохоперский центр развития ребенка \"Пристань детства\", поделилась своим опытом работы. Подробнее читайте на нашем сайте -> https://btpit36.ru",
            published = "20 февраля в 09:00",
            likedByMe = false,
            liketxt = 0,
            sharetxt = 999,
            shareByMe = false
        ),
        Post(
            id = 2,
            author = "ГБПОУ ВО 'БТПИТ'",
            content = "Студенты 1-4 курсов специальности: \"Дошкольное образование\" Борисоглебского колледжа промышленных и информационных технологий, совместно с преподавателями Ларисой Владимировной Гребенниковой и Маргаритой Михайловной Луговиной, 13 февраля, на базе Борисоглебского филиала ВИРО им. Н.Ф. Бунакова, в рамках мероприятий Наставник клуба молодых педагогов, принял участие в семинаре на тему: \"Методы и методички формирования исследовательского поведения ребенка как условие его саморазвития\".\"В ходе этого образовательного интенсива Ольга Николаевна, региональный методист, педагог-психолог МБДОУ \"Новохоперский центр развития ребенка \"Пристань детства\", поделилась своим опытом работы. Подробнее читайте на нашем сайте -> https://btpit36.ru",
            published = "20 февраля в 09:00",
            likedByMe = false,
            liketxt = 0,
            sharetxt = 999,
            shareByMe = false
        )
    )
    private val data = MutableLiveData(post)

    override fun getAll(): LiveData<List<Post>> = data


    override fun likeById(id: Long) {
        post = post.map {
            if (it.id == id) {
                if (!it.likedByMe) {
                    it.liketxt++
                } else {
                    it.liketxt--
                }
                it.copy(likedByMe = !it.likedByMe)
            } else {
                it
            }
        }
        data.value = post
    }

    override fun shareById(id: Long) {
        post = post.map {
            if (it.id == id) {
                if (!it.shareByMe) {
                    it.sharetxt++
                } else {
                    it.sharetxt--
                }
                it.copy(shareByMe = !it.shareByMe)
            } else {
                it
            }
        }
        data.value = post
    }

}

