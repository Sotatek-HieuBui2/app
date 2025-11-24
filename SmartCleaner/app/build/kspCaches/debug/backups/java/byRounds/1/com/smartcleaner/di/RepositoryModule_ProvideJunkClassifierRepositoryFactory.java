package com.smartcleaner.di;

import android.content.Context;
import com.smartcleaner.data.ml.JunkClassifier;
import com.smartcleaner.domain.repository.JunkClassifierRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class RepositoryModule_ProvideJunkClassifierRepositoryFactory implements Factory<JunkClassifierRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<JunkClassifier> classifierProvider;

  public RepositoryModule_ProvideJunkClassifierRepositoryFactory(Provider<Context> contextProvider,
      Provider<JunkClassifier> classifierProvider) {
    this.contextProvider = contextProvider;
    this.classifierProvider = classifierProvider;
  }

  @Override
  public JunkClassifierRepository get() {
    return provideJunkClassifierRepository(contextProvider.get(), classifierProvider.get());
  }

  public static RepositoryModule_ProvideJunkClassifierRepositoryFactory create(
      Provider<Context> contextProvider, Provider<JunkClassifier> classifierProvider) {
    return new RepositoryModule_ProvideJunkClassifierRepositoryFactory(contextProvider, classifierProvider);
  }

  public static JunkClassifierRepository provideJunkClassifierRepository(Context context,
      JunkClassifier classifier) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideJunkClassifierRepository(context, classifier));
  }
}
