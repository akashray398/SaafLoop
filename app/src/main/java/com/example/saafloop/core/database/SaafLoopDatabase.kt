package com.example.saafloop.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.saafloop.core.database.dao.ReportDraftDao
import com.example.saafloop.core.database.entity.ReportDraftEntity

@Database(
    entities = [ReportDraftEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SaafLoopDatabase : RoomDatabase() {

    abstract fun reportDraftDao(): ReportDraftDao

    companion object {
        @Volatile
        private var INSTANCE: SaafLoopDatabase? = null

        fun getInstance(context: Context): SaafLoopDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SaafLoopDatabase::class.java,
                    "saafloop_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
