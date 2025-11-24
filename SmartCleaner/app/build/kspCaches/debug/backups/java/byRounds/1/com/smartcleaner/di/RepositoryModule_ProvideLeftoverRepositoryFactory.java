package com.smartcleaner.di;

import android.content.Context;
import com.smartcleaner.domain.repository.LeftoverRepository;
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
public final class RepositoryModule_ProvideLeftoverRepositoryFactory implements Factory<LeftoverRepository> {
  private final Provider<Context> contextProvider;

  public RepositoryModule_ProvideLeftoverRepositoryFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public LeftoverRepository get() {
    return provideLeftoverRepository(contextProvider.get());
  }

  public static RepositoryModule_ProvideLeftoverRepositoryFactory create(
      Provider<Context> contextProvider) {
    return new RepositoryModule_ProvideLeftoverRepositoryFactory(contextProvider);
  }

  public static LeftoverRepository provideLeftoverRepository(Context context) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideLeftoverRepository(context));
  }
}
