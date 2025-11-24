package com.smartcleaner.presentation.storage;

import com.smartcleaner.domain.usecase.storage.AnalyzeStorageUseCase;
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
public final class StorageAnalyzerViewModel_Factory implements Factory<StorageAnalyzerViewModel> {
  private final Provider<AnalyzeStorageUseCase> analyzeStorageUseCaseProvider;

  public StorageAnalyzerViewModel_Factory(
      Provider<AnalyzeStorageUseCase> analyzeStorageUseCaseProvider) {
    this.analyzeStorageUseCaseProvider = analyzeStorageUseCaseProvider;
  }

  @Override
  public StorageAnalyzerViewModel get() {
    return newInstance(analyzeStorageUseCaseProvider.get());
  }

  public static StorageAnalyzerViewModel_Factory create(
      Provider<AnalyzeStorageUseCase> analyzeStorageUseCaseProvider) {
    return new StorageAnalyzerViewModel_Factory(analyzeStorageUseCaseProvider);
  }

  public static StorageAnalyzerViewModel newInstance(AnalyzeStorageUseCase analyzeStorageUseCase) {
    return new StorageAnalyzerViewModel(analyzeStorageUseCase);
  }
}
