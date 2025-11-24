package com.smartcleaner.di;

import android.content.Context;
import com.smartcleaner.domain.repository.PreferencesRepository;
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
public final class RepositoryModule_ProvidePreferencesRepositoryFactory implements Factory<PreferencesRepository> {
  private final Provider<Context> contextProvider;

  public RepositoryModule_ProvidePreferencesRepositoryFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PreferencesRepository get() {
    return providePreferencesRepository(contextProvider.get());
  }

  public static RepositoryModule_ProvidePreferencesRepositoryFactory create(
      Provider<Context> contextProvider) {
    return new RepositoryModule_ProvidePreferencesRepositoryFactory(contextProvider);
  }

  public static PreferencesRepository providePreferencesRepository(Context context) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.providePreferencesRepository(context));
  }
}
