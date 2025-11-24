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
public final class FindDuplicatesUseCase_Factory implements Factory<FindDuplicatesUseCase> {
  private final Provider<DuplicateFinderRepository> repositoryProvider;

  public FindDuplicatesUseCase_Factory(Provider<DuplicateFinderRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public FindDuplicatesUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static FindDuplicatesUseCase_Factory create(
      Provider<DuplicateFinderRepository> repositoryProvider) {
    return new FindDuplicatesUseCase_Factory(repositoryProvider);
  }

  public static FindDuplicatesUseCase newInstance(DuplicateFinderRepository repository) {
    return new FindDuplicatesUseCase(repository);
  }
}
