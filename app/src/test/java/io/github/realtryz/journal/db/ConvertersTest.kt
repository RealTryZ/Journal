package io.github.realtryz.journal.db

import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `test fromStringList with valid input`() {
        val input = listOf("apple", "banana", "cherry")
        val expected = "[\"apple\",\"banana\",\"cherry\"]"
        val result = converters.fromStringList(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test toStringList with valid input`() {
        val input = "[\"apple\",\"banana\",\"cherry\"]"
        val expected = listOf("apple", "banana", "cherry")
        val result = converters.toStringList(input)
        assertEquals(expected, result)
    }

    @Test
    fun `test toStringList with invalid input`() {
        val input = "invalid_json"
        val expected = emptyList<String>()
        val result = converters.toStringList(input)
        assertEquals(expected, result)
    }
}
