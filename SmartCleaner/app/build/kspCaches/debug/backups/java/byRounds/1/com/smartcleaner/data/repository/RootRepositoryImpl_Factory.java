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
public final class RootRepositoryImpl_Factory implements Factory<RootRepositoryImpl> {
  private final Provider<Context> contextProvider;

  public RootRepositoryImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public RootRepositoryImpl get() {
    return newInstance(contextProvider.get());
  }

  public static RootRepositoryImpl_Factory create(Provider<Context> contextProvider) {
    return new RootRepositoryImpl_Factory(contextProvider);
  }

  public static RootRepositoryImpl newInstance(Context context) {
    return new RootRepositoryImpl(context);
  }
}
