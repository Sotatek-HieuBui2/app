package com.smartcleaner.di;

import android.content.Context;
import com.smartcleaner.domain.repository.UnusedAppRepository;
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
public final class RepositoryModule_ProvideUnusedAppRepositoryFactory implements Factory<UnusedAppRepository> {
  private final Provider<Context> contextProvider;

  public RepositoryModule_ProvideUnusedAppRepositoryFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public UnusedAppRepository get() {
    return provideUnusedAppRepository(contextProvider.get());
  }

  public static RepositoryModule_ProvideUnusedAppRepositoryFactory create(
      Provider<Context> contextProvider) {
    return new RepositoryModule_ProvideUnusedAppRepositoryFactory(contextProvider);
  }

  public static UnusedAppRepository provideUnusedAppRepository(Context context) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideUnusedAppRepository(context));
  }
}
