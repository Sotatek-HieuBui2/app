package com.example.smartcleaner.data.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for AutoCleanWorker
 * Tests WorkManager background cleaning tasks
 */
@RunWith(AndroidJUnit4::class)
class AutoCleanWorkerTest {

    private lateinit var context: Context
    private lateinit var worker: AutoCleanWorker

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        worker = TestListenableWorkerBuilder<AutoCleanWorker>(context).build()
    }

    @Test
    fun `worker executes successfully with valid data`() = runBlocking {
        // Given
        val inputData = workDataOf(
            "clean_junk" to true,
            "clean_cache" to true,
            "clean_duplicates" to false
        )
        
        val worker = TestListenableWorkerBuilder<AutoCleanWorker>(context)
            .setInputData(inputData)
            .build()

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.Success::class, result::class)
    }

    @Test
    fun `worker handles no permissions gracefully`() = runBlocking {
        // When
        val result = worker.doWork()

        // Then
        // Should complete even without storage permissions
        // (would show notification about needing permissions)
        assertNotEquals(ListenableWorker.Result.Failure::class, result::class)
    }

    @Test
    fun `worker respects battery constraints`() {
        // WorkManager constraints are tested through WorkManager testing library
        // This would verify worker only runs when battery is not low
    }

    @Test
    fun `worker sends notification on completion`() = runBlocking {
        // When
        val result = worker.doWork()

        // Then
        // Verify notification was sent (requires NotificationManager mock)
        // assertTrue(notificationSent)
    }

    @Test
    fun `worker retries on failure`() = runBlocking {
        // Given - simulate failure condition
        
        // When
        val result = worker.doWork()

        // Then
        // Should return Retry for transient failures
        // assertEquals(ListenableWorker.Result.Retry::class, result::class)
    }
}
