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
public final class AnalyzeUnusedAppsUseCase_Factory implements Factory<AnalyzeUnusedAppsUseCase> {
  private final Provider<UnusedAppRepository> repositoryProvider;

  public AnalyzeUnusedAppsUseCase_Factory(Provider<UnusedAppRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public AnalyzeUnusedAppsUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static AnalyzeUnusedAppsUseCase_Factory create(
      Provider<UnusedAppRepository> repositoryProvider) {
    return new AnalyzeUnusedAppsUseCase_Factory(repositoryProvider);
  }

  public static AnalyzeUnusedAppsUseCase newInstance(UnusedAppRepository repository) {
    return new AnalyzeUnusedAppsUseCase(repository);
  }
}
