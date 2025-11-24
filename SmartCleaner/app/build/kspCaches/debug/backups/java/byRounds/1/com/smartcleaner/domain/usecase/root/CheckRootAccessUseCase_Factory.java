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
public final class CheckRootAccessUseCase_Factory implements Factory<CheckRootAccessUseCase> {
  private final Provider<RootRepository> repositoryProvider;

  public CheckRootAccessUseCase_Factory(Provider<RootRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public CheckRootAccessUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static CheckRootAccessUseCase_Factory create(Provider<RootRepository> repositoryProvider) {
    return new CheckRootAccessUseCase_Factory(repositoryProvider);
  }

  public static CheckRootAccessUseCase newInstance(RootRepository repository) {
    return new CheckRootAccessUseCase(repository);
  }
}
