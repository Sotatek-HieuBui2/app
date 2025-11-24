package com.smartcleaner.data.repository;

import android.content.Context;
import com.smartcleaner.data.ml.JunkClassifier;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class JunkClassifierRepositoryImpl_Factory implements Factory<JunkClassifierRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<JunkClassifier> classifierProvider;

  public JunkClassifierRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<JunkClassifier> classifierProvider) {
    this.contextProvider = contextProvider;
    this.classifierProvider = classifierProvider;
  }

  @Override
  public JunkClassifierRepositoryImpl get() {
    return newInstance(contextProvider.get(), classifierProvider.get());
  }

  public static JunkClassifierRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<JunkClassifier> classifierProvider) {
    return new JunkClassifierRepositoryImpl_Factory(contextProvider, classifierProvider);
  }

  public static JunkClassifierRepositoryImpl newInstance(Context context,
      JunkClassifier classifier) {
    return new JunkClassifierRepositoryImpl(context, classifier);
  }
}
