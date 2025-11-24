package com.smartcleaner.domain.usecase.unusedapp;

import com.smartcleaner.domain.repository.UnusedAppRepository;
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
public final class UninstallAppUseCase_Factory implements Factory<UninstallAppUseCase> {
  private final Provider<UnusedAppRepository> repositoryProvider;

  public UninstallAppUseCase_Factory(Provider<UnusedAppRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public UninstallAppUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static UninstallAppUseCase_Factory create(
      Provider<UnusedAppRepository> repositoryProvider) {
    return new UninstallAppUseCase_Factory(repositoryProvider);
  }

  public static UninstallAppUseCase newInstance(UnusedAppRepository repository) {
    return new UninstallAppUseCase(repository);
  }
}
