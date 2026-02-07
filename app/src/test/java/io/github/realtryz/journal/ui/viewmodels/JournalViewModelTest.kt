package io.github.realtryz.journal.ui.viewmodels

import android.app.Application
import io.github.realtryz.journal.db.JournalDatabase
import io.github.realtryz.journal.db.daos.JournalDao
import io.github.realtryz.journal.db.daos.JournalEntryDao
import io.github.realtryz.journal.domain.Journal
import io.github.realtryz.journal.domain.JournalEntry
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import app.cash.turbine.test

@OptIn(ExperimentalCoroutinesApi::class)
class JournalViewModelTest {

    private val application = mockk<Application>(relaxed = true)
    private val journalDao = mockk<JournalDao>(relaxed = true)
    private val journalEntryDao = mockk<JournalEntryDao>(relaxed = true)
    private val database = mockk<JournalDatabase>()

    private lateinit var viewModel: JournalViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockkObject(JournalDatabase)
        every { JournalDatabase.getDatabase(any()) } returns database
        every { database.journalDao() } returns journalDao
        every { database.journalEntryDao() } returns journalEntryDao

        // Initial flows
        every { journalDao.getAll() } returns flowOf(emptyList())

        viewModel = JournalViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `journals flow emits data from dao`() = runTest {
        val journals = listOf(Journal(id = "1", title = "Test Journal", color = 0))
        every { journalDao.getAll() } returns flowOf(journals)

        // Re-init to pick up the new flow
        viewModel = JournalViewModel(application)

        viewModel.journals.test {
            assertEquals(emptyList<Journal>(), awaitItem())
            assertEquals(journals, awaitItem())
        }
    }

    @Test
    fun `selectJournal updates selectedDate and currentEntry`() = runTest {
        val journalId = "journal_1"
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val entry = JournalEntry(id = "entry_1", title = "", content = "Hello", date = todayStr, journalId = journalId)

        every { journalEntryDao.getByDate(journalId, todayStr) } returns flowOf(entry)

        viewModel.currentEntry.test {
            assertEquals(null, awaitItem()) // Initial value

            viewModel.selectJournal(journalId)
            
            assertEquals(entry, awaitItem())
        }
    }

    @Test
    fun `saveEntry inserts new entry if currentEntry is null`() = runTest {
        val journalId = "journal_1"
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

        every { journalEntryDao.getByDate(journalId, todayStr) } returns flowOf(null)

        viewModel.currentEntry.test {
            assertEquals(null, awaitItem())

            viewModel.selectJournal(journalId)
            // Settle flows
            advanceUntilIdle()

            viewModel.saveEntry("New Content")
            advanceUntilIdle()

            coVerify {
                journalEntryDao.insert(match {
                    it.content == "New Content" && it.journalId == journalId && it.date == todayStr
                })
            }
        }
    }

    @Test
    fun `saveEntry updates existing entry if currentEntry is not null`() = runTest {
        val journalId = "journal_1"
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val existingEntry = JournalEntry(id = "entry_1", title = "", content = "Old", date = todayStr, journalId = journalId)

        every { journalEntryDao.getByDate(journalId, todayStr) } returns flowOf(existingEntry)

        viewModel.currentEntry.test {
            assertEquals(null, awaitItem())

            viewModel.selectJournal(journalId)
            assertEquals(existingEntry, awaitItem())

            viewModel.saveEntry("Updated Content")
            advanceUntilIdle()

            coVerify {
                journalEntryDao.update(match {
                    it.id == "entry_1" && it.content == "Updated Content"
                })
            }
        }
    }

    @Test
    fun `nextDay saves current content and updates date`() = runTest {
        val journalId = "journal_1"
        val today = LocalDate.now()
        val todayStr = today.format(DateTimeFormatter.ISO_DATE)
        
        every { journalEntryDao.getByDate(journalId, any()) } returns flowOf(null)

        viewModel.currentEntry.test {
            awaitItem() // Initial null

            viewModel.selectJournal(journalId)
            advanceUntilIdle()

            viewModel.nextDay("Today's Content")
            advanceUntilIdle()

            assertEquals(today.plusDays(1), viewModel.selectedDate.value)
            coVerify { 
                journalEntryDao.insert(match { it.content == "Today's Content" && it.date == todayStr }) 
            }
        }
    }

    @Test
    fun `addJournal calls dao insert`() = runTest {
        viewModel.addJournal("New", 123)
        advanceUntilIdle()

        coVerify { journalDao.insert(match { it.title == "New" && it.color == 123 }) }
    }

    @Test
    fun `deleteEntry calls dao delete if entry exists`() = runTest {
        val journalId = "journal_1"
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val entry = JournalEntry(id = "e1", title = "", content = "X", date = todayStr, journalId = journalId)

        every { journalEntryDao.getByDate(journalId, todayStr) } returns flowOf(entry)

        viewModel.currentEntry.test {
            awaitItem() // null initial

            viewModel.selectJournal(journalId)
            assertEquals(entry, awaitItem())

            viewModel.deleteEntry()
            advanceUntilIdle()

            coVerify { journalEntryDao.delete(entry) }
        }
    }
}
