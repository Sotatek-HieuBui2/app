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
public final class LeftoverRepositoryImpl_Factory implements Factory<LeftoverRepositoryImpl> {
  private final Provider<Context> contextProvider;

  public LeftoverRepositoryImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public LeftoverRepositoryImpl get() {
    return newInstance(contextProvider.get());
  }

  public static LeftoverRepositoryImpl_Factory create(Provider<Context> contextProvider) {
    return new LeftoverRepositoryImpl_Factory(contextProvider);
  }

  public static LeftoverRepositoryImpl newInstance(Context context) {
    return new LeftoverRepositoryImpl(context);
  }
}
