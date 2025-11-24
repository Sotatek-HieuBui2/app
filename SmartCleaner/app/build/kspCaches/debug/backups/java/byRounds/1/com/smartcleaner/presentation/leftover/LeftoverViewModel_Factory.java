package com.smartcleaner.presentation.leftover;

import com.smartcleaner.domain.usecase.leftover.DeleteLeftoverFilesUseCase;
import com.smartcleaner.domain.usecase.leftover.ScanLeftoverFilesUseCase;
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
public final class LeftoverViewModel_Factory implements Factory<LeftoverViewModel> {
  private final Provider<ScanLeftoverFilesUseCase> scanLeftoverFilesUseCaseProvider;

  private final Provider<DeleteLeftoverFilesUseCase> deleteLeftoverFilesUseCaseProvider;

  public LeftoverViewModel_Factory(
      Provider<ScanLeftoverFilesUseCase> scanLeftoverFilesUseCaseProvider,
      Provider<DeleteLeftoverFilesUseCase> deleteLeftoverFilesUseCaseProvider) {
    this.scanLeftoverFilesUseCaseProvider = scanLeftoverFilesUseCaseProvider;
    this.deleteLeftoverFilesUseCaseProvider = deleteLeftoverFilesUseCaseProvider;
  }

  @Override
  public LeftoverViewModel get() {
    return newInstance(scanLeftoverFilesUseCaseProvider.get(), deleteLeftoverFilesUseCaseProvider.get());
  }

  public static LeftoverViewModel_Factory create(
      Provider<ScanLeftoverFilesUseCase> scanLeftoverFilesUseCaseProvider,
      Provider<DeleteLeftoverFilesUseCase> deleteLeftoverFilesUseCaseProvider) {
    return new LeftoverViewModel_Factory(scanLeftoverFilesUseCaseProvider, deleteLeftoverFilesUseCaseProvider);
  }

  public static LeftoverViewModel newInstance(ScanLeftoverFilesUseCase scanLeftoverFilesUseCase,
      DeleteLeftoverFilesUseCase deleteLeftoverFilesUseCase) {
    return new LeftoverViewModel(scanLeftoverFilesUseCase, deleteLeftoverFilesUseCase);
  }
}
