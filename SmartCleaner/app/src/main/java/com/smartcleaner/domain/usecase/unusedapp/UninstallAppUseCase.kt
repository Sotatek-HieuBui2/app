package com.smartcleaner.domain.usecase.unusedapp

import com.smartcleaner.domain.repository.UnusedAppRepository
import javax.inject.Inject

/**
 * Use case: Uninstall an unused app
 * 
 * Input: packageName
 * Output: Result<Boolean>
 * 
 * Process:
 * 1. Open Android system uninstall dialog
 * 2. User confirms/cancels
 * 3. Return result
 */
class UninstallAppUseCase @Inject constructor(
    private val repository: UnusedAppRepository
) {
    suspend operator fun invoke(packageName: String): Result<Boolean> {
        return repository.uninstallApp(packageName)
    }
}
