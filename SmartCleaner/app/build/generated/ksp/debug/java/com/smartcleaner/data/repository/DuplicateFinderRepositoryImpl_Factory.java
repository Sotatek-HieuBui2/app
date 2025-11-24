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
public final class DuplicateFinderRepositoryImpl_Factory implements Factory<DuplicateFinderRepositoryImpl> {
  private final Provider<Context> contextProvider;

  public DuplicateFinderRepositoryImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DuplicateFinderRepositoryImpl get() {
    return newInstance(contextProvider.get());
  }

  public static DuplicateFinderRepositoryImpl_Factory create(Provider<Context> contextProvider) {
    return new DuplicateFinderRepositoryImpl_Factory(contextProvider);
  }

  public static DuplicateFinderRepositoryImpl newInstance(Context context) {
    return new DuplicateFinderRepositoryImpl(context);
  }
}
