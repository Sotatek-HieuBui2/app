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
public final class ScanEmptyFoldersUseCase_Factory implements Factory<ScanEmptyFoldersUseCase> {
  private final Provider<EmptyFolderRepository> repositoryProvider;

  public ScanEmptyFoldersUseCase_Factory(Provider<EmptyFolderRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ScanEmptyFoldersUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static ScanEmptyFoldersUseCase_Factory create(
      Provider<EmptyFolderRepository> repositoryProvider) {
    return new ScanEmptyFoldersUseCase_Factory(repositoryProvider);
  }

  public static ScanEmptyFoldersUseCase newInstance(EmptyFolderRepository repository) {
    return new ScanEmptyFoldersUseCase(repository);
  }
}
