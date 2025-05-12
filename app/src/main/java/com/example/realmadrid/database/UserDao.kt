package com.example.realmadrid.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE username = :username AND password = :password AND role = :role LIMIT 1")
    suspend fun getUser(username: String, password: String, role: String): User?

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("DELETE FROM users WHERE username = :username")
    suspend fun deleteUser(username: String): Int

    @Query("UPDATE users SET is_current = 0")
    suspend fun clearCurrentUser()

    @Query("UPDATE users SET is_current = 1 WHERE id = :userId")
    suspend fun setCurrentUser(userId: Int)

    @Query("SELECT * FROM users WHERE is_current = 1 LIMIT 1")
    suspend fun getCurrentUser(): User?

    @Query("UPDATE users SET role = 'admin' WHERE id = :userId")
    suspend fun promoteToAdmin(userId: Int)

    @Query("SELECT * FROM users WHERE role = 'user'")
    suspend fun getAllRegularUsers(): List<User>

    @Query("UPDATE users SET profile_image_uri = :imageUri WHERE id = :userId")
    suspend fun updateProfileImageUri(userId: Int, imageUri: String)
}