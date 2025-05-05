package com.example.realmadrid.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [User::class], version = 4, exportSchema = true )
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    object DatabaseProvider {
        private var database: AppDatabase? = null

        fun init(context: Context) {
            database = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "UserDatabase.db"
            ).fallbackToDestructiveMigration().build()
        }

        fun getUserRepository(): UserRepository {
            return UserRepository(database!!.userDao())
        }
    }
}