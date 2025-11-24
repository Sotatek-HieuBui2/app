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
public final class DeleteMessagingMediaUseCase_Factory implements Factory<DeleteMessagingMediaUseCase> {
  private final Provider<MessagingCleanerRepository> repositoryProvider;

  public DeleteMessagingMediaUseCase_Factory(
      Provider<MessagingCleanerRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public DeleteMessagingMediaUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static DeleteMessagingMediaUseCase_Factory create(
      Provider<MessagingCleanerRepository> repositoryProvider) {
    return new DeleteMessagingMediaUseCase_Factory(repositoryProvider);
  }

  public static DeleteMessagingMediaUseCase newInstance(MessagingCleanerRepository repository) {
    return new DeleteMessagingMediaUseCase(repository);
  }
}
