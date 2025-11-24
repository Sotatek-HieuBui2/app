package com.smartcleaner.domain.usecase.root

import com.smartcleaner.domain.model.RootStatus
import com.smartcleaner.domain.repository.RootRepository
import javax.inject.Inject

/**
 * Use case: Check root access
 */
class CheckRootAccessUseCase @Inject constructor(
    private val repository: RootRepository
) {
    suspend operator fun invoke(): RootStatus {
        return repository.checkRootAccess()
    }
}
