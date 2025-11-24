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
public final class DeleteLeftoverFilesUseCase_Factory implements Factory<DeleteLeftoverFilesUseCase> {
  private final Provider<LeftoverRepository> repositoryProvider;

  public DeleteLeftoverFilesUseCase_Factory(Provider<LeftoverRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public DeleteLeftoverFilesUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static DeleteLeftoverFilesUseCase_Factory create(
      Provider<LeftoverRepository> repositoryProvider) {
    return new DeleteLeftoverFilesUseCase_Factory(repositoryProvider);
  }

  public static DeleteLeftoverFilesUseCase newInstance(LeftoverRepository repository) {
    return new DeleteLeftoverFilesUseCase(repository);
  }
}
