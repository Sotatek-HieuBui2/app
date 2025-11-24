package com.smartcleaner.domain.usecase.leftover;

import com.smartcleaner.domain.repository.LeftoverRepository;
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
public final class ScanLeftoverFilesUseCase_Factory implements Factory<ScanLeftoverFilesUseCase> {
  private final Provider<LeftoverRepository> repositoryProvider;

  public ScanLeftoverFilesUseCase_Factory(Provider<LeftoverRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ScanLeftoverFilesUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static ScanLeftoverFilesUseCase_Factory create(
      Provider<LeftoverRepository> repositoryProvider) {
    return new ScanLeftoverFilesUseCase_Factory(repositoryProvider);
  }

  public static ScanLeftoverFilesUseCase newInstance(LeftoverRepository repository) {
    return new ScanLeftoverFilesUseCase(repository);
  }
}
