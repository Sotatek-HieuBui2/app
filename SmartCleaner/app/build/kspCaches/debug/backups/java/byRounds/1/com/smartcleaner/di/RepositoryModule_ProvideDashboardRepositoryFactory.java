package com.smartcleaner.di;

import android.content.Context;
import com.smartcleaner.domain.repository.DashboardRepository;
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
public final class RepositoryModule_ProvideDashboardRepositoryFactory implements Factory<DashboardRepository> {
  private final Provider<Context> contextProvider;

  public RepositoryModule_ProvideDashboardRepositoryFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DashboardRepository get() {
    return provideDashboardRepository(contextProvider.get());
  }

  public static RepositoryModule_ProvideDashboardRepositoryFactory create(
      Provider<Context> contextProvider) {
    return new RepositoryModule_ProvideDashboardRepositoryFactory(contextProvider);
  }

  public static DashboardRepository provideDashboardRepository(Context context) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideDashboardRepository(context));
  }
}
