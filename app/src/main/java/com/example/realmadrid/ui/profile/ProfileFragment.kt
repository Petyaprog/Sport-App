package com.example.realmadrid.ui.profile

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.realmadrid.Authentication
import com.example.realmadrid.R
import com.example.realmadrid.database.AppDatabase
import com.example.realmadrid.database.UserRepository
import com.example.realmadrid.databinding.FragmentProfileBinding
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var userRepository: UserRepository
    private var currentUserId: Int? = null
    private var isAdmin: Boolean = false

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

        // Настройка бокового меню
        binding.navView.setNavigationItemSelectedListener(this)
        binding.menuButton.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.END)
        }

        binding.profileImage.setOnClickListener {
            checkPermissionsAndPickImage()
        }
        loadUserData()
    }


    private fun checkPermissionsAndPickImage() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                openImagePicker()
            }
            // Запрашиваем разрешение
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    override fun onNavigationItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_edit_profile -> {
                binding.personalInfoContainer.visibility =
                    if (binding.personalInfoContainer.isVisible) View.GONE else View.VISIBLE
                binding.usersContainer.visibility = View.GONE
            }
            R.id.nav_delete_account -> deleteAccount()
            R.id.nav_view_users -> if (isAdmin) loadUsersList()
        }
        binding.drawerLayout.closeDrawer(GravityCompat.END)
        return true
    }

    private fun deleteAccount() {
        lifecycleScope.launch {
            userRepository.getCurrentUser()?.let { user ->
                if (userRepository.deleteUser(user.username)) {
                    showToast("Аккаунт удален")
                    startActivity(Intent(requireActivity(), Authentication::class.java))
                } else {
                    showToast("Ошибка удаления аккаунта")
                }
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
                isAdmin = user?.role == "admin"

                withContext(Dispatchers.Main) {
                    if (user != null) {
                        binding.usernameProfile.text = user.username
                        binding.telephoneProfile.text = user.telephone
                        binding.birthDateProfile.text = user.birthdate
                        binding.emailProfile.text = user.email

                        try {
                            user.profileImageUri?.let {
                                binding.profileImage.setImageURI(Uri.parse(it))
                            } ?: binding.profileImage.setImageResource(R.drawable.user_img)
                        } catch (e: Exception) {
                            binding.profileImage.setImageResource(R.drawable.user_img)
                        }

                        if (isAdmin) {
                            binding.navView.menu.findItem(R.id.nav_view_users).isVisible = isAdmin
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

    private fun loadUsersList() {
        lifecycleScope.launch {
            binding.personalInfoContainer.visibility = View.GONE
            binding.usersContainer.visibility = View.VISIBLE
            binding.usersContainer.removeAllViews()

            userRepository.getAllRegularUsers().forEach { user ->
                val userView = layoutInflater.inflate(
                    R.layout.item_user,
                    binding.usersContainer,
                    false
                ).apply {
                    findViewById<TextView>(R.id.userUsername).text = user.username
                    findViewById<TextView>(R.id.userEmail).text = user.email
                    findViewById<Button>(R.id.promoteButton).setOnClickListener {
                        promoteUser(user.id)
                    }
                }
                binding.usersContainer.addView(userView)
            }
        }
    }

    private fun promoteUser(userId: Int) {
        lifecycleScope.launch {
            userRepository.promoteToAdmin(userId)
            showToast("Пользователь назначен администратором")
            loadUsersList()
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