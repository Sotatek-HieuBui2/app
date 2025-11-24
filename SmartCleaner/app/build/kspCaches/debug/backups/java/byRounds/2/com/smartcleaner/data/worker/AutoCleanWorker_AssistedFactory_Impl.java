package com.smartcleaner.data.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class AutoCleanWorker_AssistedFactory_Impl implements AutoCleanWorker_AssistedFactory {
  private final AutoCleanWorker_Factory delegateFactory;

  AutoCleanWorker_AssistedFactory_Impl(AutoCleanWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public AutoCleanWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<AutoCleanWorker_AssistedFactory> create(
      AutoCleanWorker_Factory delegateFactory) {
    return InstanceFactory.create(new AutoCleanWorker_AssistedFactory_Impl(delegateFactory));
  }
}
