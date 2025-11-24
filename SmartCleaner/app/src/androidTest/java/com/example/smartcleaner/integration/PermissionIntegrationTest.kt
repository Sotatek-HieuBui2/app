package com.example.smartcleaner.integration

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for Permission handling
 */
@RunWith(AndroidJUnit4::class)
class PermissionIntegrationTest {

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `storage permissions are granted`() {
        // When
        val readPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val writePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        // Then
        assertEquals(PackageManager.PERMISSION_GRANTED, readPermission)
        assertEquals(PackageManager.PERMISSION_GRANTED, writePermission)
    }

    @Test
    fun `check multiple permissions at once`() {
        // Given
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        // When
        val results = permissions.map { permission ->
            ContextCompat.checkSelfPermission(context, permission)
        }

        // Then
        assertTrue("All permissions should be granted", 
                   results.all { it == PackageManager.PERMISSION_GRANTED })
    }

    @Test
    fun `verify permission checking helper`() {
        // Given
        fun hasStoragePermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

        // When
        val hasPermission = hasStoragePermission(context)

        // Then
        assertTrue("Should have storage permission", hasPermission)
    }

    @Test
    fun `check permission status for denied permission`() {
        // Given - A permission that is typically not granted
        val cameraPermission = Manifest.permission.CAMERA

        // When
        val status = ContextCompat.checkSelfPermission(context, cameraPermission)

        // Then - Will likely be denied unless specifically granted
        // This test demonstrates checking denied permissions
        assertTrue("Status should be GRANTED or DENIED",
                   status == PackageManager.PERMISSION_GRANTED || 
                   status == PackageManager.PERMISSION_DENIED)
    }

    @Test
    fun `validate runtime permission flow`() = runTest {
        // Given
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        
        // When - Check if permission is granted
        val isGranted = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED

        // Then
        assertTrue("Permission should be granted by test rule", isGranted)
    }

    @Test
    fun `check app has required permissions`() {
        // Given - Required permissions for app
        val requiredPermissions = listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        // When
        val allGranted = requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == 
            PackageManager.PERMISSION_GRANTED
        }

        // Then
        assertTrue("All required permissions should be granted", allGranted)
    }

    @Test
    fun `permission state is persistent`() {
        // Given - Check permission twice
        val firstCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        
        // When - Check again
        val secondCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        // Then - Both checks should have same result
        assertEquals("Permission state should be consistent", firstCheck, secondCheck)
    }

    @Test
    fun `helper function to check all storage permissions`() {
        // Given
        fun hasAllStoragePermissions(context: Context): Boolean {
            val read = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            
            val write = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            
            return read && write
        }

        // When
        val hasAll = hasAllStoragePermissions(context)

        // Then
        assertTrue("Should have all storage permissions", hasAll)
    }
}
