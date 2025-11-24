package com.smartcleaner.di;

import android.content.Context;
import com.smartcleaner.domain.repository.MessagingCleanerRepository;
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
public final class RepositoryModule_ProvideMessagingCleanerRepositoryFactory implements Factory<MessagingCleanerRepository> {
  private final Provider<Context> contextProvider;

  public RepositoryModule_ProvideMessagingCleanerRepositoryFactory(
      Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public MessagingCleanerRepository get() {
    return provideMessagingCleanerRepository(contextProvider.get());
  }

  public static RepositoryModule_ProvideMessagingCleanerRepositoryFactory create(
      Provider<Context> contextProvider) {
    return new RepositoryModule_ProvideMessagingCleanerRepositoryFactory(contextProvider);
  }

  public static MessagingCleanerRepository provideMessagingCleanerRepository(Context context) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideMessagingCleanerRepository(context));
  }
}
