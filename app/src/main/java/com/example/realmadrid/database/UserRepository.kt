package com.example.realmadrid.database

import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    suspend fun addUser(email: String, username: String, password: String): Long {
        val user = User(email = email, username = username, password = password)
        return userDao.insertUser(user)
    }

    suspend fun checkUser(username: String, password: String): Boolean {
        return userDao.getUser(username, password) != null
    }

    suspend fun isUsernameExists(username: String): Boolean {
        return userDao.getUserByUsername(username) != null
    }

    suspend fun isEmailExists(email: String): Boolean {
        return userDao.getUserByEmail(email) != null
    }

    suspend fun deleteUser(username: String): Int {
        return userDao.deleteUser(username)
    }
}