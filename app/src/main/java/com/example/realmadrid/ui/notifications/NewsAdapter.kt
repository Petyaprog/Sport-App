package com.example.realmadrid.ui.notifications

import com.example.realmadrid.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class NewsAdapter(
    private val newsList: List<NewsItem>,
    private val isAdmin: Boolean,
    private val onItemAction: (NewsItem, String) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val newsImage: ImageView = itemView.findViewById(R.id.newsImage)
        val newsTitle: TextView = itemView.findViewById(R.id.newsTitle)
        val newsDate: TextView = itemView.findViewById(R.id.newsDate)
        val newsPreview: TextView = itemView.findViewById(R.id.newsPreview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val newsItem = newsList[position]

        holder.newsTitle.text = newsItem.title
        holder.newsDate.text = newsItem.date
        holder.newsPreview.text = newsItem.previewText

        // Загрузка изображения
        Glide.with(holder.itemView.context)
            .load(newsItem.imageUrl)
            .placeholder(R.drawable.user_img)
            .into(holder.newsImage)

        holder.itemView.setOnClickListener {
            onItemAction(newsItem, "view")
        }
    }

    override fun getItemCount(): Int = newsList.size

    data class NewsItem(
        val id: String,
        val title: String,
        val date: String,
        val previewText: String,
        val fullText: String,
        val imageUrl: String
    )
}