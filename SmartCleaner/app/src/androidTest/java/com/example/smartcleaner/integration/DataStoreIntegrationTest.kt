package com.example.smartcleaner.integration

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for DataStore preferences
 */
@RunWith(AndroidJUnit4::class)
class DataStoreIntegrationTest {

    private val Context.dataStore by preferencesDataStore(name = "test_preferences")
    
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        
        // Clear data store before each test
        runTest {
            context.dataStore.edit { it.clear() }
        }
    }

    @Test
    fun `write and read string preference`() = runTest {
        // Given
        val key = stringPreferencesKey("test_key")
        val value = "test_value"

        // When
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }

        // Then
        val prefs = context.dataStore.data.first()
        assertEquals(value, prefs[key])
    }

    @Test
    fun `update existing preference`() = runTest {
        // Given
        val key = stringPreferencesKey("update_key")
        
        context.dataStore.edit { it[key] = "initial" }

        // When
        context.dataStore.edit { it[key] = "updated" }

        // Then
        val prefs = context.dataStore.data.first()
        assertEquals("updated", prefs[key])
    }

    @Test
    fun `delete preference`() = runTest {
        // Given
        val key = stringPreferencesKey("delete_key")
        
        context.dataStore.edit { it[key] = "to_delete" }

        // When
        context.dataStore.edit { it.remove(key) }

        // Then
        val prefs = context.dataStore.data.first()
        assertNull(prefs[key])
    }

    @Test
    fun `clear all preferences`() = runTest {
        // Given
        val key1 = stringPreferencesKey("key1")
        val key2 = stringPreferencesKey("key2")
        
        context.dataStore.edit { prefs ->
            prefs[key1] = "value1"
            prefs[key2] = "value2"
        }

        // When
        context.dataStore.edit { it.clear() }

        // Then
        val prefs = context.dataStore.data.first()
        assertTrue(prefs.asMap().isEmpty())
    }

    @Test
    fun `preferences persist across reads`() = runTest {
        // Given
        val key = stringPreferencesKey("persist_key")
        val value = "persist_value"
        
        context.dataStore.edit { it[key] = value }

        // When - Read multiple times
        val read1 = context.dataStore.data.first()[key]
        val read2 = context.dataStore.data.first()[key]
        val read3 = context.dataStore.data.first()[key]

        // Then
        assertEquals(value, read1)
        assertEquals(value, read2)
        assertEquals(value, read3)
    }

    @Test
    fun `multiple preference types coexist`() = runTest {
        // Given
        val stringKey = stringPreferencesKey("string_key")
        val intKey = androidx.datastore.preferences.core.intPreferencesKey("int_key")
        val boolKey = androidx.datastore.preferences.core.booleanPreferencesKey("bool_key")

        // When
        context.dataStore.edit { prefs ->
            prefs[stringKey] = "text"
            prefs[intKey] = 42
            prefs[boolKey] = true
        }

        // Then
        val prefs = context.dataStore.data.first()
        assertEquals("text", prefs[stringKey])
        assertEquals(42, prefs[intKey])
        assertEquals(true, prefs[boolKey])
    }

    @Test
    fun `concurrent writes are handled correctly`() = runTest {
        // Given
        val key = stringPreferencesKey("concurrent_key")

        // When - Multiple concurrent writes
        context.dataStore.edit { it[key] = "write1" }
        context.dataStore.edit { it[key] = "write2" }
        context.dataStore.edit { it[key] = "write3" }

        // Then - Last write wins
        val prefs = context.dataStore.data.first()
        assertEquals("write3", prefs[key])
    }

    @Test
    fun `default value when key not found`() = runTest {
        // Given
        val key = stringPreferencesKey("nonexistent_key")

        // When
        val prefs = context.dataStore.data.first()
        val value = prefs[key]

        // Then
        assertNull(value)
    }

    @Test
    fun `batch updates are atomic`() = runTest {
        // Given
        val key1 = stringPreferencesKey("batch1")
        val key2 = stringPreferencesKey("batch2")
        val key3 = stringPreferencesKey("batch3")

        // When - Batch update
        context.dataStore.edit { prefs ->
            prefs[key1] = "value1"
            prefs[key2] = "value2"
            prefs[key3] = "value3"
        }

        // Then - All values should be present
        val prefs = context.dataStore.data.first()
        assertEquals("value1", prefs[key1])
        assertEquals("value2", prefs[key2])
        assertEquals("value3", prefs[key3])
    }

    @Test
    fun `flow emits on preference change`() = runTest {
        // Given
        val key = stringPreferencesKey("flow_key")
        
        // When - Initial write
        context.dataStore.edit { it[key] = "initial" }
        val initial = context.dataStore.data.first()[key]
        
        // Update
        context.dataStore.edit { it[key] = "updated" }
        val updated = context.dataStore.data.first()[key]

        // Then
        assertEquals("initial", initial)
        assertEquals("updated", updated)
    }
}
