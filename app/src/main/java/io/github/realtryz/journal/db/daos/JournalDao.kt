package io.github.realtryz.journal.db.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.github.realtryz.journal.domain.Journal
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal")
    fun getAll(): Flow<List<Journal>>

    @Query("SELECT * FROM journal WHERE id = :id")
    fun getById(id: String): Journal

    @Insert
    suspend fun insert(journal: Journal)

    @Update
    suspend fun update(journal: Journal)

    @Delete
    suspend fun delete(journal: Journal)
}