package com.smartcleaner.presentation.unusedapp;

import com.smartcleaner.domain.repository.UnusedAppRepository;
import com.smartcleaner.domain.usecase.unusedapp.AnalyzeUnusedAppsUseCase;
import com.smartcleaner.domain.usecase.unusedapp.UninstallAppUseCase;
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
public final class UnusedAppViewModel_Factory implements Factory<UnusedAppViewModel> {
  private final Provider<AnalyzeUnusedAppsUseCase> analyzeUnusedAppsUseCaseProvider;

  private final Provider<UninstallAppUseCase> uninstallAppUseCaseProvider;

  private final Provider<UnusedAppRepository> repositoryProvider;

  public UnusedAppViewModel_Factory(
      Provider<AnalyzeUnusedAppsUseCase> analyzeUnusedAppsUseCaseProvider,
      Provider<UninstallAppUseCase> uninstallAppUseCaseProvider,
      Provider<UnusedAppRepository> repositoryProvider) {
    this.analyzeUnusedAppsUseCaseProvider = analyzeUnusedAppsUseCaseProvider;
    this.uninstallAppUseCaseProvider = uninstallAppUseCaseProvider;
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public UnusedAppViewModel get() {
    return newInstance(analyzeUnusedAppsUseCaseProvider.get(), uninstallAppUseCaseProvider.get(), repositoryProvider.get());
  }

  public static UnusedAppViewModel_Factory create(
      Provider<AnalyzeUnusedAppsUseCase> analyzeUnusedAppsUseCaseProvider,
      Provider<UninstallAppUseCase> uninstallAppUseCaseProvider,
      Provider<UnusedAppRepository> repositoryProvider) {
    return new UnusedAppViewModel_Factory(analyzeUnusedAppsUseCaseProvider, uninstallAppUseCaseProvider, repositoryProvider);
  }

  public static UnusedAppViewModel newInstance(AnalyzeUnusedAppsUseCase analyzeUnusedAppsUseCase,
      UninstallAppUseCase uninstallAppUseCase, UnusedAppRepository repository) {
    return new UnusedAppViewModel(analyzeUnusedAppsUseCase, uninstallAppUseCase, repository);
  }
}
