package com.smartcleaner.domain.usecase.classifier;

import com.smartcleaner.domain.repository.JunkClassifierRepository;
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
public final class ClassifyJunkFilesUseCase_Factory implements Factory<ClassifyJunkFilesUseCase> {
  private final Provider<JunkClassifierRepository> repositoryProvider;

  public ClassifyJunkFilesUseCase_Factory(Provider<JunkClassifierRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public ClassifyJunkFilesUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static ClassifyJunkFilesUseCase_Factory create(
      Provider<JunkClassifierRepository> repositoryProvider) {
    return new ClassifyJunkFilesUseCase_Factory(repositoryProvider);
  }

  public static ClassifyJunkFilesUseCase newInstance(JunkClassifierRepository repository) {
    return new ClassifyJunkFilesUseCase(repository);
  }
}
