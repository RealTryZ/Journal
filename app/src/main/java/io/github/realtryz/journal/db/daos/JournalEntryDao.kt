package io.github.realtryz.journal.db.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.github.realtryz.journal.domain.JournalEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {
    @Query("SELECT * FROM journalEntry")
    suspend fun getAll(): List<JournalEntry>

    @Query("SELECT * FROM journalEntry WHERE id = :id")
    fun getById(id: String): JournalEntry

    @Query("SELECT * FROM journalEntry WHERE journalId = :journalId AND date = :date LIMIT 1")
    fun getByDate(journalId: String, date: String): Flow<JournalEntry?>

    @Query("SELECT date FROM journalEntry WHERE journalId = :journalId")
    fun getDatesWithEntries(journalId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(journalEntry: JournalEntry)

    @Update
    suspend fun update(journalEntry: JournalEntry)

    @Delete
    suspend fun delete(journalEntry: JournalEntry)
}
