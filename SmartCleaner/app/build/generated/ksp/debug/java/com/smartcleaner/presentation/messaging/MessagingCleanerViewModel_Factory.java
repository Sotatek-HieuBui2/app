package com.smartcleaner.presentation.messaging;

import com.smartcleaner.domain.usecase.messaging.DeleteMessagingMediaUseCase;
import com.smartcleaner.domain.usecase.messaging.ScanMessagingAppsUseCase;
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
public final class MessagingCleanerViewModel_Factory implements Factory<MessagingCleanerViewModel> {
  private final Provider<ScanMessagingAppsUseCase> scanMessagingAppsUseCaseProvider;

  private final Provider<DeleteMessagingMediaUseCase> deleteMessagingMediaUseCaseProvider;

  public MessagingCleanerViewModel_Factory(
      Provider<ScanMessagingAppsUseCase> scanMessagingAppsUseCaseProvider,
      Provider<DeleteMessagingMediaUseCase> deleteMessagingMediaUseCaseProvider) {
    this.scanMessagingAppsUseCaseProvider = scanMessagingAppsUseCaseProvider;
    this.deleteMessagingMediaUseCaseProvider = deleteMessagingMediaUseCaseProvider;
  }

  @Override
  public MessagingCleanerViewModel get() {
    return newInstance(scanMessagingAppsUseCaseProvider.get(), deleteMessagingMediaUseCaseProvider.get());
  }

  public static MessagingCleanerViewModel_Factory create(
      Provider<ScanMessagingAppsUseCase> scanMessagingAppsUseCaseProvider,
      Provider<DeleteMessagingMediaUseCase> deleteMessagingMediaUseCaseProvider) {
    return new MessagingCleanerViewModel_Factory(scanMessagingAppsUseCaseProvider, deleteMessagingMediaUseCaseProvider);
  }

  public static MessagingCleanerViewModel newInstance(
      ScanMessagingAppsUseCase scanMessagingAppsUseCase,
      DeleteMessagingMediaUseCase deleteMessagingMediaUseCase) {
    return new MessagingCleanerViewModel(scanMessagingAppsUseCase, deleteMessagingMediaUseCase);
  }
}
