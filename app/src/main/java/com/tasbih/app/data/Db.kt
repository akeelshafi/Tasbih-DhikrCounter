package com.tasbih.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.RoomDatabase.JournalMode
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/** How many times one dhikr was said on one day. Stored on the phone only. */
@Entity(tableName = "daily_dhikr", primaryKeys = ["date", "dhikrId"])
data class DailyDhikr(
    val date: String, // ISO yyyy-MM-dd
    val dhikrId: String,
    val count: Int,
)

@Dao
abstract class DhikrDao {
    @Query("SELECT * FROM daily_dhikr")
    abstract fun observeAll(): Flow<List<DailyDhikr>>

    @Query("UPDATE daily_dhikr SET count = count + :delta WHERE date = :date AND dhikrId = :dhikrId")
    abstract suspend fun bump(date: String, dhikrId: String, delta: Int): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(row: DailyDhikr)

    @Transaction
    open suspend fun add(date: String, dhikrId: String, delta: Int) {
        if (bump(date, dhikrId, delta) == 0) insert(DailyDhikr(date, dhikrId, delta))
    }

    @Query("DELETE FROM daily_dhikr")
    abstract suspend fun clear()
}

@Database(entities = [DailyDhikr::class], version = 1, exportSchema = true)
abstract class TasbihDb : RoomDatabase() {
    abstract fun dao(): DhikrDao

    companion object {
        @Volatile private var instance: TasbihDb? = null

        fun get(context: Context): TasbihDb = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, TasbihDb::class.java, "tasbih.db")
                // one plain file, no -wal/-shm side files: safe and consistent for Android Auto Backup
                .setJournalMode(JournalMode.TRUNCATE)
                .build()
                .also { instance = it }
        }
    }
}
