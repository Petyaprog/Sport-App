package com.example.realmadrid.ui.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.realmadrid.R
import com.example.realmadrid.database.AppDatabase
import com.example.realmadrid.database.UserRepository
import com.example.realmadrid.databinding.FragmentProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var userRepository: UserRepository
    private var currentUserId: Int? = null

    // Современный способ запроса разрешений
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            showToast("Для загрузки фото необходимо разрешение")
        }
    }

    // Современный способ выбора изображения
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { handleSelectedImage(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        userRepository = AppDatabase.DatabaseProvider.getUserRepository()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.profileImage.setOnClickListener {
            checkPermissionsAndPickImage()
        }

        loadUserData()
    }

    private fun checkPermissionsAndPickImage() {
        when {
            // Для Android 13+ (API 33+) - используем Photo Picker без разрешений
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                openImagePicker()
            }

            // Для Android 10-12 (API 29-32) - проверяем разрешение
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                openImagePicker()
            }

            // Запрашиваем разрешение
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun openImagePicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Используем системный Photo Picker для API 33+
            val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
            intent.type = "image/*"
            pickImage.launch("image/*")
        } else {
            // Стандартный способ для более старых версий
            pickImage.launch("image/*")
        }
    }

    private fun handleSelectedImage(uri: Uri) {
        lifecycleScope.launch {
            try {
                // Копируем изображение в приватное хранилище
                val internalUri = copyImageToPrivateStorage(uri)

                // Сохраняем URI в БД
                currentUserId?.let { userId ->
                    userRepository.updateProfileImageUri(userId, internalUri.toString())
                }

                // Показываем изображение
                withContext(Dispatchers.Main) {
                    binding.profileImage.setImageURI(internalUri)
                    showToast("Фото профиля обновлено")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Ошибка при сохранении фото")
                }
            }
        }
    }

    private suspend fun copyImageToPrivateStorage(uri: Uri): Uri = withContext(Dispatchers.IO) {
        val filename = "profile_${currentUserId}_${System.currentTimeMillis()}.jpg"
        val outputFile = File(requireContext().filesDir, filename)

        requireContext().contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }

        Uri.fromFile(outputFile)
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                val user = userRepository.getCurrentUser()
                currentUserId = user?.id

                withContext(Dispatchers.Main) {
                    if (user != null) {
                        binding.usernameProfile.text = user.username
                        binding.telephoneProfile.text = user.telephone
                        binding.birthDateProfile.text = user.birthdate
                        binding.emailProfile.text = user.email

                        user.profileImageUri?.let { uriString ->
                            try {
                                binding.profileImage.setImageURI(Uri.parse(uriString))
                            } catch (e: Exception) {
                                // В случае ошибки показываем изображение по умолчанию
                                binding.profileImage.setImageResource(R.drawable.user_img)
                            }
                        } ?: run {
                            binding.profileImage.setImageResource(R.drawable.user_img)
                        }
                    } else {
                        binding.profileImage.setImageResource(R.drawable.user_img)
                        showToast("Пользователь не авторизован")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.profileImage.setImageResource(R.drawable.user_img)
                    showToast("Ошибка загрузки данных")
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}