package com.example.myapplication.adapt

import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.CardPostBinding
import com.example.myapplication.post.Post
import com.example.myapplication.post.UpdateNumber

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root)
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
                onInteractionListener.onLike(post)
            }
            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }
            videoContent?.isVisible = !post.video.isNullOrBlank()
            menu?.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.remove_item)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove_menu -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
            play?.setOnClickListener {
                onInteractionListener.onOpenVideo(post)
            }
            binding.root.setOnClickListener {
                onInteractionListener.onDetailsClicked(post)
            }
        }
    }
}
