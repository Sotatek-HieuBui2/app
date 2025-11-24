package com.smartcleaner.data.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.smartcleaner.domain.repository.JunkRepository;
import com.smartcleaner.domain.repository.PreferencesRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
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
public final class AutoCleanWorker_Factory {
  private final Provider<JunkRepository> junkRepositoryProvider;

  private final Provider<PreferencesRepository> preferencesRepositoryProvider;

  public AutoCleanWorker_Factory(Provider<JunkRepository> junkRepositoryProvider,
      Provider<PreferencesRepository> preferencesRepositoryProvider) {
    this.junkRepositoryProvider = junkRepositoryProvider;
    this.preferencesRepositoryProvider = preferencesRepositoryProvider;
  }

  public AutoCleanWorker get(Context context, WorkerParameters workerParams) {
    return newInstance(context, workerParams, junkRepositoryProvider.get(), preferencesRepositoryProvider.get());
  }

  public static AutoCleanWorker_Factory create(Provider<JunkRepository> junkRepositoryProvider,
      Provider<PreferencesRepository> preferencesRepositoryProvider) {
    return new AutoCleanWorker_Factory(junkRepositoryProvider, preferencesRepositoryProvider);
  }

  public static AutoCleanWorker newInstance(Context context, WorkerParameters workerParams,
      JunkRepository junkRepository, PreferencesRepository preferencesRepository) {
    return new AutoCleanWorker(context, workerParams, junkRepository, preferencesRepository);
  }
}
