package com.smartcleaner;

import androidx.hilt.work.HiltWorkerFactory;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class SmartCleanerApplication_MembersInjector implements MembersInjector<SmartCleanerApplication> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  public SmartCleanerApplication_MembersInjector(
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  public static MembersInjector<SmartCleanerApplication> create(
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new SmartCleanerApplication_MembersInjector(workerFactoryProvider);
  }

  @Override
  public void injectMembers(SmartCleanerApplication instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  @InjectedFieldSignature("com.smartcleaner.SmartCleanerApplication.workerFactory")
  public static void injectWorkerFactory(SmartCleanerApplication instance,
      HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
