package com.smartcleaner.data.repository;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class StorageAnalyzerRepositoryImpl_Factory implements Factory<StorageAnalyzerRepositoryImpl> {
  private final Provider<Context> contextProvider;

  public StorageAnalyzerRepositoryImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public StorageAnalyzerRepositoryImpl get() {
    return newInstance(contextProvider.get());
  }

  public static StorageAnalyzerRepositoryImpl_Factory create(Provider<Context> contextProvider) {
    return new StorageAnalyzerRepositoryImpl_Factory(contextProvider);
  }

  public static StorageAnalyzerRepositoryImpl newInstance(Context context) {
    return new StorageAnalyzerRepositoryImpl(context);
  }
}
