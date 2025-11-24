package com.smartcleaner.data.repository

import android.content.Context
import com.smartcleaner.domain.model.RootOperationResult
import com.smartcleaner.domain.model.RootStatus
import com.smartcleaner.domain.model.SystemApp
import com.smartcleaner.domain.model.SystemPartitionInfo
import com.smartcleaner.domain.repository.RootRepository
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RootRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : RootRepository {

    init {
        // Initialize Shell with builder
        Shell.enableVerboseLogging = true
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        )
    }

    override suspend fun checkRootAccess(): RootStatus {
        return withContext(Dispatchers.IO) {
            try {
                when {
                    Shell.isAppGrantedRoot() == true -> RootStatus.ROOTED_GRANTED
                    Shell.isAppGrantedRoot() == false -> RootStatus.ROOTED_DENIED
                    else -> RootStatus.NOT_ROOTED
                }
            } catch (e: Exception) {
                RootStatus.NOT_ROOTED
            }
        }
    }

    override suspend fun requestRootPermission(): RootStatus {
        return withContext(Dispatchers.IO) {
            try {
                // Request root access
                val shell = Shell.getShell()
                when {
                    shell.isRoot -> RootStatus.ROOTED_GRANTED
                    else -> RootStatus.ROOTED_DENIED
                }
            } catch (e: Exception) {
                RootStatus.NOT_ROOTED
            }
        }
    }

    override suspend fun cleanSystemCache(): RootOperationResult {
        return executeCommand("rm -rf /data/cache/*")
    }

    override suspend fun cleanDalvikCache(): RootOperationResult {
        return executeCommand("rm -rf /data/dalvik-cache/*")
    }

    override suspend fun getSystemPartitionInfo(): List<SystemPartitionInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val result = Shell.cmd("df -h").exec()
                if (result.isSuccess) {
                    parsePartitionInfo(result.out)
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun getSystemApps(): List<SystemApp> {
        return withContext(Dispatchers.IO) {
            try {
                val result = Shell.cmd("pm list packages -s").exec()
                if (result.isSuccess) {
                    result.out.mapNotNull { line ->
                        val packageName = line.removePrefix("package:")
                        SystemApp(
                            packageName = packageName,
                            appName = packageName, // Would need PackageManager for real name
                            size = 0, // Would need du command
                            canBeDisabled = true,
                            isBloatware = checkIfBloatware(packageName)
                        )
                    }
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun disableSystemApp(packageName: String): RootOperationResult {
        return executeCommand("pm disable-user --user 0 $packageName")
    }

    override suspend fun executeCommand(command: String): RootOperationResult {
        return withContext(Dispatchers.IO) {
            try {
                val result = Shell.cmd(command).exec()
                RootOperationResult(
                    success = result.isSuccess,
                    message = if (result.isSuccess) "Command executed successfully" else "Command failed",
                    output = result.out.joinToString("\n")
                )
            } catch (e: Exception) {
                RootOperationResult(
                    success = false,
                    message = "Error: ${e.message}",
                    output = null
                )
            }
        }
    }

    private fun parsePartitionInfo(output: List<String>): List<SystemPartitionInfo> {
        // Parse df output: Filesystem Size Used Available Use% Mounted on
        return output.drop(1).mapNotNull { line ->
            try {
                val parts = line.trim().split(Regex("\\s+"))
                if (parts.size >= 6) {
                    SystemPartitionInfo(
                        path = parts[5],
                        totalSize = parseSize(parts[1]),
                        usedSize = parseSize(parts[2]),
                        freeSize = parseSize(parts[3]),
                        percentage = parts[4].removeSuffix("%").toFloatOrNull() ?: 0f
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun parseSize(sizeStr: String): Long {
        val multiplier = when {
            sizeStr.endsWith("G") -> 1024 * 1024 * 1024L
            sizeStr.endsWith("M") -> 1024 * 1024L
            sizeStr.endsWith("K") -> 1024L
            else -> 1L
        }
        val value = sizeStr.dropLast(1).toDoubleOrNull() ?: 0.0
        return (value * multiplier).toLong()
    }

    private fun checkIfBloatware(packageName: String): Boolean {
        val bloatwarePatterns = listOf(
            "com.facebook",
            "com.netflix",
            "com.spotify",
            "com.amazon",
            "com.google.android.apps.docs", // Google Drive
            "com.google.android.apps.plus", // Google+
            "com.android.chrome"
        )
        return bloatwarePatterns.any { packageName.contains(it) }
    }
}
