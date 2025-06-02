package com.example.realmadrid.ui.notifications

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.realmadrid.database.AppDatabase
import com.example.realmadrid.database.News
import com.example.realmadrid.databinding.FragmentAddEditNewsBinding
import com.bumptech.glide.Glide
import com.example.realmadrid.database.NewsRepository
import kotlinx.coroutines.launch

class AddEditNewsFragment : Fragment() {
    private var _binding: FragmentAddEditNewsBinding? = null
    private val binding get() = _binding!!
    private var imageUri: Uri? = null
    private lateinit var newsRepository: NewsRepository
    private var currentNewsId: Int? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                imageUri = uri
                Glide.with(this)
                    .load(uri)
                    .into(binding.newsImage)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newsRepository = AppDatabase.DatabaseProvider.getNewsRepository()

         arguments?.let { bundle ->
            currentNewsId = bundle.getString("id")?.toIntOrNull()
            binding.newsTitle.setText(bundle.getString("title", ""))
            binding.newsDate.setText(bundle.getString("date", ""))
            binding.newsFullText.setText(bundle.getString("fullText", ""))
            val imageUrl = bundle.getString("imageUrl", "")
            if (imageUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(imageUrl)
                    .into(binding.newsImage)
            }
        }

        binding.btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImage.launch(intent)
        }

        binding.newsFullText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.scrollView.post {
                    binding.scrollView.smoothScrollTo(0, binding.newsFullText.bottom)
                }
            }
        }

        binding.btnSave.setOnClickListener {
            saveNews()
        }
    }

    private fun saveNews() {
        val title = binding.newsTitle.text.toString()
        val date = binding.newsDate.text.toString()
        val fullText = binding.newsFullText.text.toString()
        val previewText = if (fullText.length > 100) fullText.substring(0, 100) + "..." else fullText
        val imageUrl = imageUri?.toString() ?: arguments?.getString("imageUrl") ?: ""

        lifecycleScope.launch {
            if (currentNewsId != null) {
                // Редактирование существующей новости
                val news = News(
                    id = currentNewsId!!,
                    title = title,
                    date = date,
                    previewText = previewText,
                    fullText = fullText,
                    imageUrl = imageUrl
                )
                newsRepository.updateNews(news)
            } else {
                newsRepository.addNews(title, date, previewText, fullText, imageUrl)
            }
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}