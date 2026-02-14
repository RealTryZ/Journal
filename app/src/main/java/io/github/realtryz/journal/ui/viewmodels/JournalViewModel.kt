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

/**
 * ViewModel that manages journals and journal entries.
 *
 * Exposes flows for the list of journals, the currently selected journal/date and the
 * currently selected entry. Provides methods to add/update/delete journals and entries
 * and to navigate/select dates.
 *
 * @param application Application instance used to obtain the database.
 */
class JournalViewModel(application: Application) : AndroidViewModel(application) {
    private val journalDao = JournalDatabase.getDatabase(application).journalDao()
    private val journalEntryDao = JournalDatabase.getDatabase(application).journalEntryDao()

    /** Flow of all journals in the database. */
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

    /**
     * Current entry for the selected journal/date as a StateFlow.
     * Emits null when no journal or date is selected or when no entry exists for the date.
     */
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

    /**
     * Returns a Flow of entries for the given journal ID.
     *
     * @param journalId ID of the journal.
     */
    fun getEntriesForJournal(journalId: String): Flow<List<JournalEntry>> {
        return journalEntryDao.getEntriesForJournal(journalId)
    }

    /**
     * Selects a journal and resets the selected date to today.
     *
     * @param journalId ID of the journal to select.
     */
    fun selectJournal(journalId: String) {
        _selectedJournalId.value = journalId
        _selectedDate.value = LocalDate.now()
    }

    /**
     * Selects a journal and a specific date.
     *
     * @param journalId ID of the journal.
     * @param date Date to select.
     */
    fun selectJournalAndDate(journalId: String, date: LocalDate) {
        _selectedJournalId.value = journalId
        _selectedDate.value = date
    }

    /**
     * Change the selected date and persist the current content as an entry before switching.
     *
     * @param date New date to select.
     * @param currentContent Current content to save for the previous date.
     * @param currentImageUris Current image URIs to save for the previous date.
     */
    fun setDate(date: LocalDate, currentContent: String, currentImageUris: List<String>) {
        saveEntry(currentContent, currentImageUris)
        _selectedDate.value = date
    }

    /**
     * Move to the next day. Saves current content before incrementing the date.
     */
    fun nextDay(currentContent: String, currentImageUris: List<String>) {
        saveEntry(currentContent, currentImageUris)
        _selectedDate.value = _selectedDate.value?.plusDays(1)
    }

    /**
     * Move to the previous day. Saves current content before decrementing the date.
     */
    fun previousDay(currentContent: String, currentImageUris: List<String>) {
        saveEntry(currentContent, currentImageUris)
        _selectedDate.value = _selectedDate.value?.minusDays(1)
    }

    /**
     * Adds a new journal to the database.
     *
     * @param title Journal title.
     * @param color ARGB color int for the journal.
     */
    fun addJournal(title: String, color: Int) {
        viewModelScope.launch {
            val newJournal = Journal(title = title, color = color)
            journalDao.insert(newJournal)
        }
    }

    /**
     * Updates an existing journal.
     *
     * @param journal Journal object with updated fields.
     */
    fun updateJournal(journal: Journal) {
        viewModelScope.launch {
            journalDao.update(journal)
        }
    }

    /**
     * Deletes a journal from the database.
     *
     * @param journal Journal to delete.
     */
    fun deleteJournal(journal: Journal) {
        viewModelScope.launch {
            journalDao.delete(journal)
        }
    }

    /**
     * Saves the current entry for the selected journal and date. If the entry exists, it is
     * updated; otherwise a new entry is inserted. Empty content with no images will not be saved.
     *
     * @param content Text content to save.
     * @param imageUris List of image URIs to save (defaults to current entry's images if not provided).
     */
    fun saveEntry(content: String, imageUris: List<String> = currentEntry.value?.imageUris ?: emptyList()) {
        val journalId = _selectedJournalId.value ?: return
        val date = _selectedDate.value ?: return
        val dateString = date.format(DateTimeFormatter.ISO_DATE)
        
        if (content.isBlank() && imageUris.isEmpty()) return

        viewModelScope.launch {
            val existing = currentEntry.value
            if (existing != null) {
                journalEntryDao.update(existing.copy(content = content, imageUris = imageUris))
            } else {
                val newEntry = JournalEntry(
                    title = "",
                    content = content,
                    date = dateString,
                    journalId = journalId,
                    imageUris = imageUris
                )
                journalEntryDao.insert(newEntry)
            }
        }
    }

    /**
     * Deletes the currently selected entry if it exists.
     */
    fun deleteEntry() {
        val entry = currentEntry.value ?: return
        viewModelScope.launch {
            journalEntryDao.delete(entry)
        }
    }
}
