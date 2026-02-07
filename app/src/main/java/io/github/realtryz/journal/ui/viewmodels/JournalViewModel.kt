package io.github.realtryz.journal.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.realtryz.journal.db.JournalDatabase
import io.github.realtryz.journal.domain.Journal
import io.github.realtryz.journal.domain.JournalEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class JournalViewModel(application: Application) : AndroidViewModel(application) {
    private val journalDao = JournalDatabase.getDatabase(application).journalDao()
    private val journalEntryDao = JournalDatabase.getDatabase(application).journalEntryDao()

    val journals: StateFlow<List<Journal>> = journalDao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedJournalId = MutableStateFlow<String?>(null)
    val selectedJournalId: StateFlow<String?> = _selectedJournalId
    
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentEntry: StateFlow<JournalEntry?> = _selectedJournalId
        .flatMapLatest { journalId ->
            _selectedDate.flatMapLatest { date ->
                if (journalId != null && date != null) {
                    journalEntryDao.getByDate(journalId, date.format(DateTimeFormatter.ISO_DATE))
                } else {
                    kotlinx.coroutines.flow.flowOf(null)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun getEntriesForJournal(journalId: String): Flow<List<JournalEntry>> {
        return journalEntryDao.getEntriesForJournal(journalId)
    }

    fun selectJournal(journalId: String) {
        _selectedJournalId.value = journalId
        _selectedDate.value = LocalDate.now()
    }

    fun selectJournalAndDate(journalId: String, date: LocalDate) {
        _selectedJournalId.value = journalId
        _selectedDate.value = date
    }

    fun setDate(date: LocalDate, currentContent: String) {
        saveEntry(currentContent)
        _selectedDate.value = date
    }

    fun nextDay(currentContent: String) {
        saveEntry(currentContent)
        _selectedDate.value = _selectedDate.value?.plusDays(1)
    }

    fun previousDay(currentContent: String) {
        saveEntry(currentContent)
        _selectedDate.value = _selectedDate.value?.minusDays(1)
    }

    fun addJournal(title: String, color: Int) {
        viewModelScope.launch {
            val newJournal = Journal(title = title, color = color)
            journalDao.insert(newJournal)
        }
    }

    fun updateJournal(journal: Journal) {
        viewModelScope.launch {
            journalDao.update(journal)
        }
    }

    fun deleteJournal(journal: Journal) {
        viewModelScope.launch {
            journalDao.delete(journal)
        }
    }

    fun saveEntry(content: String) {
        val journalId = _selectedJournalId.value ?: return
        val date = _selectedDate.value ?: return
        val dateString = date.format(DateTimeFormatter.ISO_DATE)
        
        if (content.isBlank()) return

        viewModelScope.launch {
            val existing = currentEntry.value
            if (existing != null) {
                journalEntryDao.update(existing.copy(content = content))
            } else {
                val newEntry = JournalEntry(
                    title = "",
                    content = content,
                    date = dateString,
                    journalId = journalId
                )
                journalEntryDao.insert(newEntry)
            }
        }
    }

    fun deleteEntry() {
        val entry = currentEntry.value ?: return
        viewModelScope.launch {
            journalEntryDao.delete(entry)
        }
    }
}
