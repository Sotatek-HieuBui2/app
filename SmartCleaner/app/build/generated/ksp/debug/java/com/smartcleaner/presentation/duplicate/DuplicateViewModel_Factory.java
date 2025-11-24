package com.smartcleaner.presentation.duplicate;

import com.smartcleaner.domain.usecase.duplicate.DeleteDuplicatesUseCase;
import com.smartcleaner.domain.usecase.duplicate.FindDuplicatesUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class DuplicateViewModel_Factory implements Factory<DuplicateViewModel> {
  private final Provider<FindDuplicatesUseCase> findDuplicatesUseCaseProvider;

  private final Provider<DeleteDuplicatesUseCase> deleteDuplicatesUseCaseProvider;

  public DuplicateViewModel_Factory(Provider<FindDuplicatesUseCase> findDuplicatesUseCaseProvider,
      Provider<DeleteDuplicatesUseCase> deleteDuplicatesUseCaseProvider) {
    this.findDuplicatesUseCaseProvider = findDuplicatesUseCaseProvider;
    this.deleteDuplicatesUseCaseProvider = deleteDuplicatesUseCaseProvider;
  }

  @Override
  public DuplicateViewModel get() {
    return newInstance(findDuplicatesUseCaseProvider.get(), deleteDuplicatesUseCaseProvider.get());
  }

  public static DuplicateViewModel_Factory create(
      Provider<FindDuplicatesUseCase> findDuplicatesUseCaseProvider,
      Provider<DeleteDuplicatesUseCase> deleteDuplicatesUseCaseProvider) {
    return new DuplicateViewModel_Factory(findDuplicatesUseCaseProvider, deleteDuplicatesUseCaseProvider);
  }

  public static DuplicateViewModel newInstance(FindDuplicatesUseCase findDuplicatesUseCase,
      DeleteDuplicatesUseCase deleteDuplicatesUseCase) {
    return new DuplicateViewModel(findDuplicatesUseCase, deleteDuplicatesUseCase);
  }
}
