package com.smartcleaner.domain.usecase.root;

import com.smartcleaner.domain.repository.RootRepository;
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
public final class CleanSystemCacheUseCase_Factory implements Factory<CleanSystemCacheUseCase> {
  private final Provider<RootRepository> repositoryProvider;

  public CleanSystemCacheUseCase_Factory(Provider<RootRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public CleanSystemCacheUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static CleanSystemCacheUseCase_Factory create(
      Provider<RootRepository> repositoryProvider) {
    return new CleanSystemCacheUseCase_Factory(repositoryProvider);
  }

  public static CleanSystemCacheUseCase newInstance(RootRepository repository) {
    return new CleanSystemCacheUseCase(repository);
  }
}
