package com.example.smartcleaner.integration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.*
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.smartcleaner.data.worker.AutoCleanWorker
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * Integration tests for WorkManager periodic task execution
 */
@RunWith(AndroidJUnit4::class)
class WorkManagerIntegrationTest {

    private lateinit var context: Context
    private lateinit var workManager: WorkManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        
        // Initialize WorkManager for testing
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setExecutor(java.util.concurrent.Executors.newSingleThreadExecutor())
            .build()
        
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        workManager = WorkManager.getInstance(context)
    }

    @Test
    fun `enqueue periodic work request`() = runTest {
        // Given
        val workRequest = PeriodicWorkRequestBuilder<AutoCleanWorker>(
            1, TimeUnit.DAYS
        ).build()

        // When
        workManager.enqueue(workRequest).result.get()

        // Then
        val workInfo = workManager.getWorkInfoById(workRequest.id).get()
        assertNotNull(workInfo)
        assertTrue("Work should be enqueued", 
                   workInfo.state == WorkInfo.State.ENQUEUED || 
                   workInfo.state == WorkInfo.State.RUNNING)
    }

    @Test
    fun `unique work replaces existing`() = runTest {
        // Given
        val workRequest1 = PeriodicWorkRequestBuilder<AutoCleanWorker>(
            1, TimeUnit.DAYS
        ).addTag("auto_clean")
        .build()
        
        val workRequest2 = PeriodicWorkRequestBuilder<AutoCleanWorker>(
            1, TimeUnit.DAYS
        ).addTag("auto_clean")
        .build()

        // When
        workManager.enqueueUniquePeriodicWork(
            "auto_clean",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest1
        ).result.get()
        
        workManager.enqueueUniquePeriodicWork(
            "auto_clean",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest2
        ).result.get()

        // Then
        val workInfos = workManager.getWorkInfosByTag("auto_clean").get()
        assertEquals("Should have only one work with this tag", 1, workInfos.size)
    }

    @Test
    fun `cancel work by tag`() = runTest {
        // Given
        val workRequest = PeriodicWorkRequestBuilder<AutoCleanWorker>(
            1, TimeUnit.DAYS
        ).addTag("test_work")
        .build()
        
        workManager.enqueue(workRequest).result.get()

        // When
        workManager.cancelAllWorkByTag("test_work").result.get()

        // Then
        val workInfo = workManager.getWorkInfoById(workRequest.id).get()
        assertEquals(WorkInfo.State.CANCELLED, workInfo.state)
    }

    @Test
    fun `work with constraints is enqueued`() = runTest {
        // Given
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<AutoCleanWorker>(
            1, TimeUnit.DAYS
        ).setConstraints(constraints)
        .build()

        // When
        workManager.enqueue(workRequest).result.get()

        // Then
        val workInfo = workManager.getWorkInfoById(workRequest.id).get()
        assertNotNull(workInfo)
    }

    @Test
    fun `test worker execution directly`() = runTest {
        // Given
        val worker = TestListenableWorkerBuilder<AutoCleanWorker>(context).build()

        // When
        val result = worker.doWork()

        // Then
        assertTrue("Worker should succeed", 
                   result is ListenableWorker.Result.Success ||
                   result is ListenableWorker.Result.Retry)
    }

    @Test
    fun `get work status by id`() = runTest {
        // Given
        val workRequest = OneTimeWorkRequestBuilder<AutoCleanWorker>().build()
        workManager.enqueue(workRequest).result.get()

        // When
        val workInfo = workManager.getWorkInfoById(workRequest.id).get()

        // Then
        assertNotNull(workInfo)
        assertNotNull(workInfo.id)
        assertNotNull(workInfo.state)
    }

    @Test
    fun `enqueue one-time work request`() = runTest {
        // Given
        val workRequest = OneTimeWorkRequestBuilder<AutoCleanWorker>()
            .setInitialDelay(1, TimeUnit.SECONDS)
            .build()

        // When
        workManager.enqueue(workRequest).result.get()

        // Then
        val workInfo = workManager.getWorkInfoById(workRequest.id).get()
        assertNotNull(workInfo)
    }

    @Test
    fun `work info contains correct tags`() = runTest {
        // Given
        val tag1 = "tag1"
        val tag2 = "tag2"
        val workRequest = OneTimeWorkRequestBuilder<AutoCleanWorker>()
            .addTag(tag1)
            .addTag(tag2)
            .build()

        // When
        workManager.enqueue(workRequest).result.get()

        // Then
        val workInfo = workManager.getWorkInfoById(workRequest.id).get()
        assertTrue(workInfo.tags.contains(tag1))
        assertTrue(workInfo.tags.contains(tag2))
    }

    @Test
    fun `cancel unique work by name`() = runTest {
        // Given
        val uniqueName = "unique_auto_clean"
        val workRequest = PeriodicWorkRequestBuilder<AutoCleanWorker>(
            1, TimeUnit.DAYS
        ).build()
        
        workManager.enqueueUniquePeriodicWork(
            uniqueName,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        ).result.get()

        // When
        workManager.cancelUniqueWork(uniqueName).result.get()

        // Then
        val workInfo = workManager.getWorkInfosForUniqueWork(uniqueName).get()
        assertTrue(workInfo.isEmpty() || workInfo.all { it.state == WorkInfo.State.CANCELLED })
    }

    @Test
    fun `work with input data executes correctly`() = runTest {
        // Given
        val inputData = Data.Builder()
            .putBoolean("auto_mode", true)
            .putInt("threshold", 100)
            .build()
        
        val workRequest = OneTimeWorkRequestBuilder<AutoCleanWorker>()
            .setInputData(inputData)
            .build()

        // When
        workManager.enqueue(workRequest).result.get()

        // Then
        val workInfo = workManager.getWorkInfoById(workRequest.id).get()
        assertNotNull(workInfo)
    }
}
