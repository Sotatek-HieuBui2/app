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
public final class DashboardRepositoryImpl_Factory implements Factory<DashboardRepositoryImpl> {
  private final Provider<Context> contextProvider;

  public DashboardRepositoryImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DashboardRepositoryImpl get() {
    return newInstance(contextProvider.get());
  }

  public static DashboardRepositoryImpl_Factory create(Provider<Context> contextProvider) {
    return new DashboardRepositoryImpl_Factory(contextProvider);
  }

  public static DashboardRepositoryImpl newInstance(Context context) {
    return new DashboardRepositoryImpl(context);
  }
}
