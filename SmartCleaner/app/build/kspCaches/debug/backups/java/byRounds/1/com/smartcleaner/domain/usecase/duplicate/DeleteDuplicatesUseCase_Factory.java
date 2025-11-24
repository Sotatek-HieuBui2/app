package com.smartcleaner.domain.usecase.duplicate;

import com.smartcleaner.domain.repository.DuplicateFinderRepository;
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
public final class DeleteDuplicatesUseCase_Factory implements Factory<DeleteDuplicatesUseCase> {
  private final Provider<DuplicateFinderRepository> repositoryProvider;

  public DeleteDuplicatesUseCase_Factory(Provider<DuplicateFinderRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public DeleteDuplicatesUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static DeleteDuplicatesUseCase_Factory create(
      Provider<DuplicateFinderRepository> repositoryProvider) {
    return new DeleteDuplicatesUseCase_Factory(repositoryProvider);
  }

  public static DeleteDuplicatesUseCase newInstance(DuplicateFinderRepository repository) {
    return new DeleteDuplicatesUseCase(repository);
  }
}
