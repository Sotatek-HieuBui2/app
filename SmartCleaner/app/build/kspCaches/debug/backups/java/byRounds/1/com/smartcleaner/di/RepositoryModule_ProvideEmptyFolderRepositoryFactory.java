package com.smartcleaner.di;

import android.content.Context;
import com.smartcleaner.domain.repository.EmptyFolderRepository;
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
public final class RepositoryModule_ProvideEmptyFolderRepositoryFactory implements Factory<EmptyFolderRepository> {
  private final Provider<Context> contextProvider;

  public RepositoryModule_ProvideEmptyFolderRepositoryFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public EmptyFolderRepository get() {
    return provideEmptyFolderRepository(contextProvider.get());
  }

  public static RepositoryModule_ProvideEmptyFolderRepositoryFactory create(
      Provider<Context> contextProvider) {
    return new RepositoryModule_ProvideEmptyFolderRepositoryFactory(contextProvider);
  }

  public static EmptyFolderRepository provideEmptyFolderRepository(Context context) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideEmptyFolderRepository(context));
  }
}
