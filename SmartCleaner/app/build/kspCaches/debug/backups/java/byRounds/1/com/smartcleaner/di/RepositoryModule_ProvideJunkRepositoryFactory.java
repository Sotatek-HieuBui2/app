package com.smartcleaner.di;

import android.content.Context;
import com.smartcleaner.domain.repository.JunkRepository;
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
public final class RepositoryModule_ProvideJunkRepositoryFactory implements Factory<JunkRepository> {
  private final Provider<Context> contextProvider;

  public RepositoryModule_ProvideJunkRepositoryFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public JunkRepository get() {
    return provideJunkRepository(contextProvider.get());
  }

  public static RepositoryModule_ProvideJunkRepositoryFactory create(
      Provider<Context> contextProvider) {
    return new RepositoryModule_ProvideJunkRepositoryFactory(contextProvider);
  }

  public static JunkRepository provideJunkRepository(Context context) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideJunkRepository(context));
  }
}
