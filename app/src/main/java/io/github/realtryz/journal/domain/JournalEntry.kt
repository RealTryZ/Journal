package io.github.realtryz.journal.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "journalEntry")
data class JournalEntry (
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val date: String,
    val journalId: String,
    val imageUris: List<String> = emptyList()
)
