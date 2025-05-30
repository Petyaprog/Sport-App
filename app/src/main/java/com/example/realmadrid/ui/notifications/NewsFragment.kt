package com.example.realmadrid.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.realmadrid.R
import com.example.realmadrid.database.AppDatabase
import com.example.realmadrid.database.NewsRepository
import com.example.realmadrid.databinding.FragmentNewsBinding
import com.example.realmadrid.database.UserRepository
import kotlinx.coroutines.launch

class NewsFragment : Fragment() {
    private lateinit var binding: FragmentNewsBinding
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var userRepository: UserRepository
    private lateinit var newsRepository: NewsRepository
    private var isAdmin = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userRepository = AppDatabase.DatabaseProvider.getUserRepository()
        newsRepository = AppDatabase.DatabaseProvider.getNewsRepository()

        lifecycleScope.launch {
            val currentUser = userRepository.getCurrentUser()
            isAdmin = currentUser?.role == "admin"

            setupNewsList()

            if (isAdmin) {
                binding.fabAddNews.visibility = View.VISIBLE
                binding.fabAddNews.setOnClickListener {
                    findNavController().navigate(R.id.action_newsFragment_to_addEditNewsFragment)
                }
            } else {
                binding.fabAddNews.visibility = View.GONE
            }
        }
    }

    private fun setupNewsList() {
        lifecycleScope.launch {
            val newsList = newsRepository.getAllNews().map { news ->
                NewsAdapter.NewsItem(
                    id = news.id.toString(),
                    title = news.title,
                    date = news.date,
                    previewText = news.previewText,
                    fullText = news.fullText,
                    imageUrl = news.imageUrl
                )
            }

            newsAdapter = NewsAdapter(newsList, isAdmin) { newsItem, action ->
                when (action) {
                    "view" -> {
                        val bundle = Bundle().apply {
                            putString("id", newsItem.id)
                            putString("title", newsItem.title)
                            putString("date", newsItem.date)
                            putString("fullText", newsItem.fullText)
                            putString("imageUrl", newsItem.imageUrl)
                            putBoolean("isAdmin", isAdmin)
                        }
                        findNavController().navigate(
                            R.id.action_newsFragment_to_newsDetailFragment,
                            bundle
                        )
                    }
                }
            }
            binding.newsRecyclerView.adapter = newsAdapter
        }
    }
}