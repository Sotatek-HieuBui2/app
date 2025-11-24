package com.smartcleaner.domain.usecase.junk;

import com.smartcleaner.domain.repository.JunkRepository;
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
public final class ScanJunkFilesUseCase_Factory implements Factory<ScanJunkFilesUseCase> {
  private final Provider<JunkRepository> repositoryProvider;

  public ScanJunkFilesUseCase_Factory(Provider<JunkRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ScanJunkFilesUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static ScanJunkFilesUseCase_Factory create(Provider<JunkRepository> repositoryProvider) {
    return new ScanJunkFilesUseCase_Factory(repositoryProvider);
  }

  public static ScanJunkFilesUseCase newInstance(JunkRepository repository) {
    return new ScanJunkFilesUseCase(repository);
  }
}
