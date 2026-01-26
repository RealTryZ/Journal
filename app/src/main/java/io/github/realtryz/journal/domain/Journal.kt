package io.github.realtryz.journal.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "journal")
data class Journal (
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val color: Int
)
