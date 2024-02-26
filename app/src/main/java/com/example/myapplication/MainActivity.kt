package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
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
        val newPostContract = registerForActivityResult(NewPostActivity.Contract) { result ->
            result ?: return@registerForActivityResult
            viewModel.changeContent((result))
            viewModel.save()
        }


        val adapter = PostAdapter(
            object : OnInteractionListener  {
                override fun onRemove(post: Post) {
                    viewModel.removeById(post.id)
                }

                override fun onEdit(post: Post) {
                    viewModel.edit(post)
                    newPostContract.launch(post.content)
                }

                override fun onShare(post: Post) {
                    viewModel.shareById(post.id)
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, post.content)
                        type = "text/plain"
                    }

                    val startIntent = Intent.createChooser(intent, getString(R.string.app_name))

                    startActivity(startIntent)
                }

                override fun onLike(post: Post) {
                    viewModel.likeById(post.id)
                }

                override fun onClearEditing(post: Post) {
                    viewModel.clearEditing()
                }
            }
        )


//        viewModel.edited.observe(this) {
//            if (it.id == 0L) {
//                binding.cancelGroup?.visibility = View.GONE
//                return@observe
//            }
//
//            binding.cancelGroup?.visibility = View.VISIBLE
//            binding.content?.requestFocus()
//            binding.content?.setText(it.content)
//        }
//
//
//        binding.cancel?.setOnClickListener {
//            binding.cancelGroup?.visibility = View.GONE
//            viewModel.clearEditing()
//            binding.content?.clearFocus()
//            binding.content?.setText("")
//        }
//
//        binding.save?.setOnClickListener {
//            with(binding.content) {
//                if (this?.text.isNullOrBlank()) {
//                    Toast.makeText(
//                        this@MainActivity,
//                        "Содержимое не может быть пустым",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    return@setOnClickListener
//                }
//
//                viewModel.changeContent(this?.text.toString())
//                viewModel.save()
//
//                this?.setText("")
//                this?.clearFocus()
//                this?.let { it1 -> AndroidUtils.hideKeyboard(it1) }
//            }
//        }

        viewModel.data.observe(this) { posts ->
            adapter.submitList(posts)
        }

        binding.list?.adapter = adapter

        binding.add?.setOnClickListener {
            newPostContract.launch("")
        }

    }
}


interface OnInteractionListener  {
    fun onRemove(post: Post)
    fun onEdit(post: Post)
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onClearEditing(post: Post)
}

class PostAdapter(private val listener: OnInteractionListener ) : ListAdapter<Post, PostViewHolder>( PostDiffCallback())
{


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(
            binding = binding,
            listener = listener
        )
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}

class PostDiffCallback  : DiffUtil.ItemCallback<Post>() {

    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
}
class PostViewHolder(
    private val binding: CardPostBinding,
    private val listener: OnInteractionListener ) : RecyclerView.ViewHolder(binding.root)
{

    fun bind(post: Post) {
        with(binding) {
            textView2.text = post.author
            textView3.text = post.published
            textView4.text = post.content

            likeBtn?.isChecked= post.likedByMe

            textView.text = UpdateNumber.countAmountFormat(post.liketxt)
            textView6.text = UpdateNumber.countAmountFormat(post.sharetxt)

            likeBtn?.setOnClickListener {
                listener.onLike(post)
            }

            share.setOnClickListener {
                listener.onShare(post)
            }

            menu?.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.remove_item)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove_menu -> {
                                listener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                listener.onEdit(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
        }
    }
}

object AndroidUtils {
    fun hideKeyboard(view: View) {
        val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}


object UpdateNumber {
    fun countAmountFormat(num: Int): String {
        return when {
            num < 1000 -> num.toString()
            num < 1000000 -> String.format("%.1fK", num / 1000.0)
            else -> String.format("%.1fM", num / 1000000.0)
        }
    }
}

data class Post (
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    var likedByMe: Boolean = false,
    var sharedByMe: Boolean = false,
    var liketxt: Int = 999,
    val sharetxt: Int = 999,
    val views: Int = 999
)

interface PostRepository {
    fun getAll(): LiveData<List<Post>>
    fun likeById(id: Long)
    fun shareById(id: Long)
    fun removeById(id: Long)
    fun save(post: Post)
}

private val empty = Post(
    id = 0,
    author = "",
    content = "",
    published = "",
    likedByMe = false,
    sharedByMe = false
)
class PostViewModel: ViewModel() {
    private val repository: PostRepository = PostRepositoryInMemoryImpl ()

    val data: LiveData<List<Post>> = repository.getAll()

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
        edited.value?.let { post ->
            if(content != post.content) {
                edited.value = post.copy(
                    published = "Сейчас",
                    author = "Я",
                    content = content
                )
            }
        }
    }
}

private var nextId = 0L

class PostRepositoryInMemoryImpl  : PostRepository {
    private var post = listOf(
        Post(
            id = ++nextId,
            author = "ГБПОУ ВО 'БТПИТ'",
            content = "Студенты 1-4 курсов специальности: \"Дошкольное образование\" Борисоглебского колледжа промышленных и информационных технологий, совместно с преподавателями Ларисой Владимировной Гребенниковой и Маргаритой Михайловной Луговиной, 13 февраля, на базе Борисоглебского филиала ВИРО им. Н.Ф. Бунакова, в рамках мероприятий Наставник клуба молодых педагогов, принял участие в семинаре на тему: \"Методы и методички формирования исследовательского поведения ребенка как условие его саморазвития\".\"В ходе этого образовательного интенсива Ольга Николаевна, региональный методист, педагог-психолог МБДОУ \"Новохоперский центр развития ребенка \"Пристань детства\", поделилась своим опытом работы. Подробнее читайте на нашем сайте -> https://btpit36.ru",
            published = "20 февраля в 09:00",
            likedByMe = false
        ),
        Post(
            id = ++nextId,
            author = "ГБПОУ ВО 'БТПИТ'",
            content = "Преподаватель Борисоглебского техникума промышленных и информационных технологий Гребенникова Лариса Владимировна одно из занятий по дисциплине «Краеведение» со студентами 1 курсов специальностей «Дошкольное образование» и «Коррекционная педагогика в начальном образовании» провела в МБУК БГО Борисоглебском историко-художественном музее.\n" +
                    "Старший научный сотрудник музея Загребина Ольга Васильевна рассказала студентам, что Территория Воронежского Прихоперья, где мы живем, в историческом отношении весьма интересна. Основой экспозиции послужили материалы археологических раскопок.\n" +
                    "Студенты увидели экспонаты, которые помнит не одно поколение борисоглебцев (кость мамонта, например, значится под номером 1 в книге поступлений основного фонда музея), и совершенно новую коллекцию предметов археологии, которой пополнился Борисоглебский музей.",
            published = "21 февраля в 09:00",
            likedByMe = false
        ),
        Post(
            id = ++nextId,
            author = "ГБПОУ ВО 'БТПИТ'",
            content = "Студенты 4 курса специальности «Дошкольное образование» совместно с преподавателем Чихачевой Ириной Юрьевной Борисоглебского техникума промышленных и информационных технологий на занятиях по дисциплине Теоретические основы дошкольного образования в рамках освоения темы: «Современные технологии», погрузились в виртуальную реальность.\n" +
                    "Виртуальная реальность представляет среду моделирования или симуляции, которая погружает пользователя в виртуальный мир, обеспечивает человеку ощущение присутствия в нем, путем визуальных, звуковых и тактильных воздействий.",
            published = "22 февраля в 09:00",
            likedByMe = false
        ),

    ).reversed()
    private val data = MutableLiveData(post)

    override fun getAll(): LiveData<List<Post>> = data
    override fun save(post: Post) {
        val existingPosts = data.value.orEmpty().toMutableList()

        if (post.id == 0L) {
            val newPost = post.copy(id = ++nextId)
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
    }

    override fun shareById(id: Long) {
        val existingPosts = data.value.orEmpty().toMutableList()
        val index = existingPosts.indexOfFirst { it.id == id }
        if (index != -1) {
            val post = existingPosts[index]
            existingPosts[index] = post.copy(
                sharedByMe = !post.sharedByMe,
                sharetxt = if (post.sharedByMe) post.sharetxt - 1 else post.sharetxt + 1
            )
            save(existingPosts[index])
        }
    }


    override fun removeById(id: Long) {
        val existingPosts = data.value.orEmpty().toMutableList()
        val postToRemove = existingPosts.firstOrNull { it.id == id }
        if (postToRemove != null) {
            existingPosts.remove(postToRemove)
            // Проходим по оставшимся постам и сбрасываем likedByMe
            for (post in existingPosts) {
                if (post.likedByMe) {
                    post.likedByMe = false
                    // Обновляем также счетчик лайков
                    post.liketxt = post.liketxt - 1
                }
            }
            data.value = existingPosts
        }
    }


}


