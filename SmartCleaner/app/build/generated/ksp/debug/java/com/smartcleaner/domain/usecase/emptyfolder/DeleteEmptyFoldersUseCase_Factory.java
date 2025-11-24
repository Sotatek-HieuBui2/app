package com.smartcleaner.domain.usecase.emptyfolder;

import com.smartcleaner.domain.repository.EmptyFolderRepository;
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
public final class DeleteEmptyFoldersUseCase_Factory implements Factory<DeleteEmptyFoldersUseCase> {
  private final Provider<EmptyFolderRepository> repositoryProvider;

  public DeleteEmptyFoldersUseCase_Factory(Provider<EmptyFolderRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public DeleteEmptyFoldersUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static DeleteEmptyFoldersUseCase_Factory create(
      Provider<EmptyFolderRepository> repositoryProvider) {
    return new DeleteEmptyFoldersUseCase_Factory(repositoryProvider);
  }

  public static DeleteEmptyFoldersUseCase newInstance(EmptyFolderRepository repository) {
    return new DeleteEmptyFoldersUseCase(repository);
  }
}
