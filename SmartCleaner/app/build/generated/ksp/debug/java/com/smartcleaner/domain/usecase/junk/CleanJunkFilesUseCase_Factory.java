package com.smartcleaner.domain.usecase.junk;

import com.smartcleaner.domain.repository.JunkRepository;
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
public final class CleanJunkFilesUseCase_Factory implements Factory<CleanJunkFilesUseCase> {
  private final Provider<JunkRepository> repositoryProvider;

  public CleanJunkFilesUseCase_Factory(Provider<JunkRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public CleanJunkFilesUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static CleanJunkFilesUseCase_Factory create(Provider<JunkRepository> repositoryProvider) {
    return new CleanJunkFilesUseCase_Factory(repositoryProvider);
  }

  public static CleanJunkFilesUseCase newInstance(JunkRepository repository) {
    return new CleanJunkFilesUseCase(repository);
  }
}
