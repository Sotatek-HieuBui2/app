package com.smartcleaner.domain.usecase.storage;

import com.smartcleaner.domain.repository.StorageAnalyzerRepository;
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
public final class AnalyzeStorageUseCase_Factory implements Factory<AnalyzeStorageUseCase> {
  private final Provider<StorageAnalyzerRepository> repositoryProvider;

  public AnalyzeStorageUseCase_Factory(Provider<StorageAnalyzerRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public AnalyzeStorageUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static AnalyzeStorageUseCase_Factory create(
      Provider<StorageAnalyzerRepository> repositoryProvider) {
    return new AnalyzeStorageUseCase_Factory(repositoryProvider);
  }

  public static AnalyzeStorageUseCase newInstance(StorageAnalyzerRepository repository) {
    return new AnalyzeStorageUseCase(repository);
  }
}
