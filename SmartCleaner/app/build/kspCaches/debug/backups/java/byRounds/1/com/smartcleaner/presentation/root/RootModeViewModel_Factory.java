package com.smartcleaner.presentation.root;

import com.smartcleaner.domain.usecase.root.CheckRootAccessUseCase;
import com.smartcleaner.domain.usecase.root.CleanSystemCacheUseCase;
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
public final class RootModeViewModel_Factory implements Factory<RootModeViewModel> {
  private final Provider<CheckRootAccessUseCase> checkRootAccessUseCaseProvider;

  private final Provider<CleanSystemCacheUseCase> cleanSystemCacheUseCaseProvider;

  public RootModeViewModel_Factory(Provider<CheckRootAccessUseCase> checkRootAccessUseCaseProvider,
      Provider<CleanSystemCacheUseCase> cleanSystemCacheUseCaseProvider) {
    this.checkRootAccessUseCaseProvider = checkRootAccessUseCaseProvider;
    this.cleanSystemCacheUseCaseProvider = cleanSystemCacheUseCaseProvider;
  }

  @Override
  public RootModeViewModel get() {
    return newInstance(checkRootAccessUseCaseProvider.get(), cleanSystemCacheUseCaseProvider.get());
  }

  public static RootModeViewModel_Factory create(
      Provider<CheckRootAccessUseCase> checkRootAccessUseCaseProvider,
      Provider<CleanSystemCacheUseCase> cleanSystemCacheUseCaseProvider) {
    return new RootModeViewModel_Factory(checkRootAccessUseCaseProvider, cleanSystemCacheUseCaseProvider);
  }

  public static RootModeViewModel newInstance(CheckRootAccessUseCase checkRootAccessUseCase,
      CleanSystemCacheUseCase cleanSystemCacheUseCase) {
    return new RootModeViewModel(checkRootAccessUseCase, cleanSystemCacheUseCase);
  }
}
