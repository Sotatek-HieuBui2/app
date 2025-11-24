package com.smartcleaner.di;

import android.content.Context;
import com.smartcleaner.domain.repository.DuplicateFinderRepository;
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
public final class RepositoryModule_ProvideDuplicateFinderRepositoryFactory implements Factory<DuplicateFinderRepository> {
  private final Provider<Context> contextProvider;

  public RepositoryModule_ProvideDuplicateFinderRepositoryFactory(
      Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DuplicateFinderRepository get() {
    return provideDuplicateFinderRepository(contextProvider.get());
  }

  public static RepositoryModule_ProvideDuplicateFinderRepositoryFactory create(
      Provider<Context> contextProvider) {
    return new RepositoryModule_ProvideDuplicateFinderRepositoryFactory(contextProvider);
  }

  public static DuplicateFinderRepository provideDuplicateFinderRepository(Context context) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideDuplicateFinderRepository(context));
  }
}
