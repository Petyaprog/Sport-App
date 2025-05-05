package com.example.realmadrid.database

class UserRepository(private val userDao: UserDao) {
    suspend fun addUser(email: String, telephone: String, birthdate: String, username: String, password: String, role: String): Long {
        val user = User(email = email, telephone = telephone, birthdate = birthdate, username = username, password = password, role = role)
        return userDao.insertUser(user)
    }

    suspend fun checkUser(username: String, password: String, role: String): Boolean {
        return userDao.getUser(username, password, role) != null
    }

    suspend fun isUsernameExists(username: String): Boolean {
        return userDao.getUserByUsername(username) != null
    }

    suspend fun isEmailExists(email: String): Boolean {
        return userDao.getUserByEmail(email) != null
    }

    suspend fun deleteUser(username: String): Boolean {
        return userDao.deleteUser(username) > 0
    }

    suspend fun loginUser(username: String, password: String, role: String): Boolean {
        return try {
            userDao.clearCurrentUser()
            val user = userDao.getUser(username, password, role)
            user?.let {
                userDao.setCurrentUser(it.id)
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun logout() {
        userDao.clearCurrentUser()
    }

    suspend fun getCurrentUser(): User? {
        return userDao.getCurrentUser()
    }
}