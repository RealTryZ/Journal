package io.github.realtryz.journal.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.realtryz.journal.db.daos.JournalDao
import io.github.realtryz.journal.db.daos.JournalEntryDao
import io.github.realtryz.journal.domain.Journal
import io.github.realtryz.journal.domain.JournalEntry

@Database(
    entities = [Journal::class, JournalEntry::class],
    version = 3
)
@TypeConverters(Converters::class)
abstract class JournalDatabase : RoomDatabase() {

    abstract fun journalDao(): JournalDao

    abstract fun journalEntryDao(): JournalEntryDao

    companion object {
        @Volatile
        private var Instance: JournalDatabase? = null

        fun getDatabase(context: Context): JournalDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    JournalDatabase::class.java,
                    "journal_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}