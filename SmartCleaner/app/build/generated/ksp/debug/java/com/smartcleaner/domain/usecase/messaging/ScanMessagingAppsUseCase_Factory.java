package com.smartcleaner.domain.usecase.messaging;

import com.smartcleaner.domain.repository.MessagingCleanerRepository;
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
public final class ScanMessagingAppsUseCase_Factory implements Factory<ScanMessagingAppsUseCase> {
  private final Provider<MessagingCleanerRepository> repositoryProvider;

  public ScanMessagingAppsUseCase_Factory(Provider<MessagingCleanerRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ScanMessagingAppsUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static ScanMessagingAppsUseCase_Factory create(
      Provider<MessagingCleanerRepository> repositoryProvider) {
    return new ScanMessagingAppsUseCase_Factory(repositoryProvider);
  }

  public static ScanMessagingAppsUseCase newInstance(MessagingCleanerRepository repository) {
    return new ScanMessagingAppsUseCase(repository);
  }
}
