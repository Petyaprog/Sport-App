package com.example.realmadrid.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.realmadrid.R
import com.example.realmadrid.database.AppDatabase
import com.example.realmadrid.database.NewsRepository
import com.example.realmadrid.databinding.FragmentNewsDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NewsDetailFragment : Fragment() {
    private var _binding: FragmentNewsDetailBinding? = null
    private val binding get() = _binding!!
    private var isAdmin = false
    private lateinit var newsRepository: NewsRepository
    private var currentNewsId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация репозитория
        newsRepository = AppDatabase.DatabaseProvider.getNewsRepository()

        // Получаем данные из Bundle
        currentNewsId = arguments?.getString("id")?.toIntOrNull() ?: -1
        val title = arguments?.getString("title") ?: ""
        val date = arguments?.getString("date") ?: ""
        val fullText = arguments?.getString("fullText") ?: ""
        val imageUrl = arguments?.getString("imageUrl") ?: ""
        isAdmin = arguments?.getBoolean("isAdmin") ?: false

        // Устанавливаем данные в UI
        binding.detailNewsTitle.text = title
        binding.detailNewsDate.text = date
        binding.detailNewsText.text = fullText

        // Загружаем изображение
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.user_img)
            .into(binding.detailNewsImage)

        // Настройка видимости кнопок для админа
        if (isAdmin) {
            binding.panelButton.visibility = View.VISIBLE

            binding.btnEditNews.setOnClickListener {
                navigateToEditNews(currentNewsId, title, date, fullText, imageUrl)
            }

            binding.btnDeleteNews.setOnClickListener {
                deleteNews(currentNewsId)
            }
        } else {
            binding.panelButton.visibility = View.GONE
        }
    }

    private fun navigateToEditNews(id: Int, title: String, date: String, fullText: String, imageUrl: String) {
        val bundle = Bundle().apply {
            putString("id", id.toString())
            putString("title", title)
            putString("date", date)
            putString("fullText", fullText)
            putString("imageUrl", imageUrl)
        }
        findNavController().navigate(
            R.id.action_newsDetailFragment_to_addEditNewsFragment,
            bundle
        )
    }

    private fun deleteNews(newsId: Int) {
        if (newsId == -1) return

        CoroutineScope(Dispatchers.IO).launch {
            // Получаем полный объект новости для удаления
            val newsToDelete = newsRepository.getNewsById(newsId)
            newsToDelete?.let {
                newsRepository.deleteNews(it)

                // Возвращаемся назад после удаления
                requireActivity().runOnUiThread {
                    findNavController().popBackStack()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}