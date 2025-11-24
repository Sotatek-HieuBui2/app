package com.smartcleaner.presentation.emptyfolder;

import com.smartcleaner.domain.usecase.emptyfolder.DeleteEmptyFoldersUseCase;
import com.smartcleaner.domain.usecase.emptyfolder.ScanEmptyFoldersUseCase;
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
public final class EmptyFolderViewModel_Factory implements Factory<EmptyFolderViewModel> {
  private final Provider<ScanEmptyFoldersUseCase> scanEmptyFoldersUseCaseProvider;

  private final Provider<DeleteEmptyFoldersUseCase> deleteEmptyFoldersUseCaseProvider;

  public EmptyFolderViewModel_Factory(
      Provider<ScanEmptyFoldersUseCase> scanEmptyFoldersUseCaseProvider,
      Provider<DeleteEmptyFoldersUseCase> deleteEmptyFoldersUseCaseProvider) {
    this.scanEmptyFoldersUseCaseProvider = scanEmptyFoldersUseCaseProvider;
    this.deleteEmptyFoldersUseCaseProvider = deleteEmptyFoldersUseCaseProvider;
  }

  @Override
  public EmptyFolderViewModel get() {
    return newInstance(scanEmptyFoldersUseCaseProvider.get(), deleteEmptyFoldersUseCaseProvider.get());
  }

  public static EmptyFolderViewModel_Factory create(
      Provider<ScanEmptyFoldersUseCase> scanEmptyFoldersUseCaseProvider,
      Provider<DeleteEmptyFoldersUseCase> deleteEmptyFoldersUseCaseProvider) {
    return new EmptyFolderViewModel_Factory(scanEmptyFoldersUseCaseProvider, deleteEmptyFoldersUseCaseProvider);
  }

  public static EmptyFolderViewModel newInstance(ScanEmptyFoldersUseCase scanEmptyFoldersUseCase,
      DeleteEmptyFoldersUseCase deleteEmptyFoldersUseCase) {
    return new EmptyFolderViewModel(scanEmptyFoldersUseCase, deleteEmptyFoldersUseCase);
  }
}
