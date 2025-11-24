package com.smartcleaner.di;

import android.content.Context;
import com.smartcleaner.domain.repository.StorageAnalyzerRepository;
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
public final class RepositoryModule_ProvideStorageAnalyzerRepositoryFactory implements Factory<StorageAnalyzerRepository> {
  private final Provider<Context> contextProvider;

  public RepositoryModule_ProvideStorageAnalyzerRepositoryFactory(
      Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public StorageAnalyzerRepository get() {
    return provideStorageAnalyzerRepository(contextProvider.get());
  }

  public static RepositoryModule_ProvideStorageAnalyzerRepositoryFactory create(
      Provider<Context> contextProvider) {
    return new RepositoryModule_ProvideStorageAnalyzerRepositoryFactory(contextProvider);
  }

  public static StorageAnalyzerRepository provideStorageAnalyzerRepository(Context context) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideStorageAnalyzerRepository(context));
  }
}
