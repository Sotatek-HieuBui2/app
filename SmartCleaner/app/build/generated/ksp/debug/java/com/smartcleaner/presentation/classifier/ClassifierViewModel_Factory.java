package com.smartcleaner.presentation.classifier;

import com.smartcleaner.domain.usecase.classifier.ClassifyJunkFilesUseCase;
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
public final class ClassifierViewModel_Factory implements Factory<ClassifierViewModel> {
  private final Provider<ClassifyJunkFilesUseCase> classifyJunkFilesUseCaseProvider;

  public ClassifierViewModel_Factory(
      Provider<ClassifyJunkFilesUseCase> classifyJunkFilesUseCaseProvider) {
    this.classifyJunkFilesUseCaseProvider = classifyJunkFilesUseCaseProvider;
  }

  @Override
  public ClassifierViewModel get() {
    return newInstance(classifyJunkFilesUseCaseProvider.get());
  }

  public static ClassifierViewModel_Factory create(
      Provider<ClassifyJunkFilesUseCase> classifyJunkFilesUseCaseProvider) {
    return new ClassifierViewModel_Factory(classifyJunkFilesUseCaseProvider);
  }

  public static ClassifierViewModel newInstance(ClassifyJunkFilesUseCase classifyJunkFilesUseCase) {
    return new ClassifierViewModel(classifyJunkFilesUseCase);
  }
}
