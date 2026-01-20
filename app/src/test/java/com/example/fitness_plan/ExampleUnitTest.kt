package com.example.fitness_plan

import org.junit.Test
import org.junit.Assert.*

/**
 * Basic unit tests for Fitness Plan application
 */
class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun basic_math_operations() {
        assertEquals(8, 4 * 2)
        assertEquals(2, 4 / 2)
        assertEquals(6, 4 + 2)
        assertEquals(2, 4 - 2)
    }

    @Test
    fun string_operations() {
        val str = "Hello World"
        assertEquals(11, str.length)
        assertTrue(str.contains("Hello"))
        assertTrue(str.startsWith("H"))
        assertTrue(str.endsWith("d"))
    }

    @Test
    fun collection_operations() {
        val list = listOf(1, 2, 3, 4, 5)
        assertEquals(5, list.size)
        assertEquals(1, list.first())
        assertEquals(5, list.last())
        assertTrue(list.contains(3))
        assertEquals(15, list.sum())
    }

    @Test
    fun boolean_operations() {
        assertTrue(true && true)
        assertFalse(true && false)
        assertTrue(true || false)
        assertFalse(false || false)
        assertFalse(!true)
        assertTrue(!false)
    }
}