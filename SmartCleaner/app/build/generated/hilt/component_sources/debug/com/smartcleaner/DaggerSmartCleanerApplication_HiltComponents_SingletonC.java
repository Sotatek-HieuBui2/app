package com.smartcleaner;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.hilt.work.HiltWrapper_WorkerFactoryModule;
import androidx.hilt.work.WorkerAssistedFactory;
import androidx.hilt.work.WorkerFactoryModule_ProvideFactoryFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.smartcleaner.data.ml.JunkClassifier;
import com.smartcleaner.data.worker.AutoCleanWorker;
import com.smartcleaner.data.worker.AutoCleanWorker_AssistedFactory;
import com.smartcleaner.di.RepositoryModule;
import com.smartcleaner.di.RepositoryModule_ProvideDashboardRepositoryFactory;
import com.smartcleaner.di.RepositoryModule_ProvideDuplicateFinderRepositoryFactory;
import com.smartcleaner.di.RepositoryModule_ProvideEmptyFolderRepositoryFactory;
import com.smartcleaner.di.RepositoryModule_ProvideJunkClassifierRepositoryFactory;
import com.smartcleaner.di.RepositoryModule_ProvideJunkRepositoryFactory;
import com.smartcleaner.di.RepositoryModule_ProvideLeftoverRepositoryFactory;
import com.smartcleaner.di.RepositoryModule_ProvideMessagingCleanerRepositoryFactory;
import com.smartcleaner.di.RepositoryModule_ProvidePreferencesRepositoryFactory;
import com.smartcleaner.di.RepositoryModule_ProvideRootRepositoryFactory;
import com.smartcleaner.di.RepositoryModule_ProvideStorageAnalyzerRepositoryFactory;
import com.smartcleaner.di.RepositoryModule_ProvideUnusedAppRepositoryFactory;
import com.smartcleaner.domain.repository.DashboardRepository;
import com.smartcleaner.domain.repository.DuplicateFinderRepository;
import com.smartcleaner.domain.repository.EmptyFolderRepository;
import com.smartcleaner.domain.repository.JunkClassifierRepository;
import com.smartcleaner.domain.repository.JunkRepository;
import com.smartcleaner.domain.repository.LeftoverRepository;
import com.smartcleaner.domain.repository.MessagingCleanerRepository;
import com.smartcleaner.domain.repository.PreferencesRepository;
import com.smartcleaner.domain.repository.RootRepository;
import com.smartcleaner.domain.repository.StorageAnalyzerRepository;
import com.smartcleaner.domain.repository.UnusedAppRepository;
import com.smartcleaner.domain.usecase.classifier.ClassifyJunkFilesUseCase;
import com.smartcleaner.domain.usecase.duplicate.DeleteDuplicatesUseCase;
import com.smartcleaner.domain.usecase.duplicate.FindDuplicatesUseCase;
import com.smartcleaner.domain.usecase.emptyfolder.DeleteEmptyFoldersUseCase;
import com.smartcleaner.domain.usecase.emptyfolder.ScanEmptyFoldersUseCase;
import com.smartcleaner.domain.usecase.leftover.DeleteLeftoverFilesUseCase;
import com.smartcleaner.domain.usecase.leftover.ScanLeftoverFilesUseCase;
import com.smartcleaner.domain.usecase.messaging.DeleteMessagingMediaUseCase;
import com.smartcleaner.domain.usecase.messaging.ScanMessagingAppsUseCase;
import com.smartcleaner.domain.usecase.root.CheckRootAccessUseCase;
import com.smartcleaner.domain.usecase.root.CleanSystemCacheUseCase;
import com.smartcleaner.domain.usecase.storage.AnalyzeStorageUseCase;
import com.smartcleaner.domain.usecase.unusedapp.AnalyzeUnusedAppsUseCase;
import com.smartcleaner.domain.usecase.unusedapp.UninstallAppUseCase;
import com.smartcleaner.presentation.MainActivity;
import com.smartcleaner.presentation.classifier.ClassifierViewModel;
import com.smartcleaner.presentation.classifier.ClassifierViewModel_HiltModules_KeyModule_ProvideFactory;
import com.smartcleaner.presentation.dashboard.DashboardViewModel;
import com.smartcleaner.presentation.dashboard.DashboardViewModel_HiltModules_KeyModule_ProvideFactory;
import com.smartcleaner.presentation.duplicate.DuplicateViewModel;
import com.smartcleaner.presentation.duplicate.DuplicateViewModel_HiltModules_KeyModule_ProvideFactory;
import com.smartcleaner.presentation.emptyfolder.EmptyFolderViewModel;
import com.smartcleaner.presentation.emptyfolder.EmptyFolderViewModel_HiltModules_KeyModule_ProvideFactory;
import com.smartcleaner.presentation.leftover.LeftoverViewModel;
import com.smartcleaner.presentation.leftover.LeftoverViewModel_HiltModules_KeyModule_ProvideFactory;
import com.smartcleaner.presentation.messaging.MessagingCleanerViewModel;
import com.smartcleaner.presentation.messaging.MessagingCleanerViewModel_HiltModules_KeyModule_ProvideFactory;
import com.smartcleaner.presentation.root.RootModeViewModel;
import com.smartcleaner.presentation.root.RootModeViewModel_HiltModules_KeyModule_ProvideFactory;
import com.smartcleaner.presentation.settings.SettingsViewModel;
import com.smartcleaner.presentation.settings.SettingsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.smartcleaner.presentation.storage.StorageAnalyzerViewModel;
import com.smartcleaner.presentation.storage.StorageAnalyzerViewModel_HiltModules_KeyModule_ProvideFactory;
import com.smartcleaner.presentation.unusedapp.UnusedAppViewModel;
import com.smartcleaner.presentation.unusedapp.UnusedAppViewModel_HiltModules_KeyModule_ProvideFactory;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.flags.HiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.Preconditions;
import dagger.internal.SingleCheck;
import java.util.Map;
import java.util.Set;
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
public final class DaggerSmartCleanerApplication_HiltComponents_SingletonC {
  private DaggerSmartCleanerApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    /**
     * @deprecated This module is declared, but an instance is not used in the component. This method is a no-op. For more, see https://dagger.dev/unused-modules.
     */
    @Deprecated
    public Builder hiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule(
        HiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule hiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule) {
      Preconditions.checkNotNull(hiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule);
      return this;
    }

    /**
     * @deprecated This module is declared, but an instance is not used in the component. This method is a no-op. For more, see https://dagger.dev/unused-modules.
     */
    @Deprecated
    public Builder hiltWrapper_WorkerFactoryModule(
        HiltWrapper_WorkerFactoryModule hiltWrapper_WorkerFactoryModule) {
      Preconditions.checkNotNull(hiltWrapper_WorkerFactoryModule);
      return this;
    }

    /**
     * @deprecated This module is declared, but an instance is not used in the component. This method is a no-op. For more, see https://dagger.dev/unused-modules.
     */
    @Deprecated
    public Builder repositoryModule(RepositoryModule repositoryModule) {
      Preconditions.checkNotNull(repositoryModule);
      return this;
    }

    public SmartCleanerApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements SmartCleanerApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public SmartCleanerApplication_HiltComponents.ActivityRetainedC build() {
      return new ActivityRetainedCImpl(singletonCImpl);
    }
  }

  private static final class ActivityCBuilder implements SmartCleanerApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public SmartCleanerApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements SmartCleanerApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public SmartCleanerApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements SmartCleanerApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public SmartCleanerApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements SmartCleanerApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public SmartCleanerApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements SmartCleanerApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public SmartCleanerApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements SmartCleanerApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public SmartCleanerApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends SmartCleanerApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends SmartCleanerApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends SmartCleanerApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends SmartCleanerApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Set<String> getViewModelKeys() {
      return ImmutableSet.<String>of(ClassifierViewModel_HiltModules_KeyModule_ProvideFactory.provide(), DashboardViewModel_HiltModules_KeyModule_ProvideFactory.provide(), DuplicateViewModel_HiltModules_KeyModule_ProvideFactory.provide(), EmptyFolderViewModel_HiltModules_KeyModule_ProvideFactory.provide(), LeftoverViewModel_HiltModules_KeyModule_ProvideFactory.provide(), MessagingCleanerViewModel_HiltModules_KeyModule_ProvideFactory.provide(), RootModeViewModel_HiltModules_KeyModule_ProvideFactory.provide(), SettingsViewModel_HiltModules_KeyModule_ProvideFactory.provide(), StorageAnalyzerViewModel_HiltModules_KeyModule_ProvideFactory.provide(), UnusedAppViewModel_HiltModules_KeyModule_ProvideFactory.provide());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }
  }

  private static final class ViewModelCImpl extends SmartCleanerApplication_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<ClassifierViewModel> classifierViewModelProvider;

    private Provider<DashboardViewModel> dashboardViewModelProvider;

    private Provider<DuplicateViewModel> duplicateViewModelProvider;

    private Provider<EmptyFolderViewModel> emptyFolderViewModelProvider;

    private Provider<LeftoverViewModel> leftoverViewModelProvider;

    private Provider<MessagingCleanerViewModel> messagingCleanerViewModelProvider;

    private Provider<RootModeViewModel> rootModeViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<StorageAnalyzerViewModel> storageAnalyzerViewModelProvider;

    private Provider<UnusedAppViewModel> unusedAppViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    private ClassifyJunkFilesUseCase classifyJunkFilesUseCase() {
      return new ClassifyJunkFilesUseCase(singletonCImpl.provideJunkClassifierRepositoryProvider.get());
    }

    private FindDuplicatesUseCase findDuplicatesUseCase() {
      return new FindDuplicatesUseCase(singletonCImpl.provideDuplicateFinderRepositoryProvider.get());
    }

    private DeleteDuplicatesUseCase deleteDuplicatesUseCase() {
      return new DeleteDuplicatesUseCase(singletonCImpl.provideDuplicateFinderRepositoryProvider.get());
    }

    private ScanEmptyFoldersUseCase scanEmptyFoldersUseCase() {
      return new ScanEmptyFoldersUseCase(singletonCImpl.provideEmptyFolderRepositoryProvider.get());
    }

    private DeleteEmptyFoldersUseCase deleteEmptyFoldersUseCase() {
      return new DeleteEmptyFoldersUseCase(singletonCImpl.provideEmptyFolderRepositoryProvider.get());
    }

    private ScanLeftoverFilesUseCase scanLeftoverFilesUseCase() {
      return new ScanLeftoverFilesUseCase(singletonCImpl.provideLeftoverRepositoryProvider.get());
    }

    private DeleteLeftoverFilesUseCase deleteLeftoverFilesUseCase() {
      return new DeleteLeftoverFilesUseCase(singletonCImpl.provideLeftoverRepositoryProvider.get());
    }

    private ScanMessagingAppsUseCase scanMessagingAppsUseCase() {
      return new ScanMessagingAppsUseCase(singletonCImpl.provideMessagingCleanerRepositoryProvider.get());
    }

    private DeleteMessagingMediaUseCase deleteMessagingMediaUseCase() {
      return new DeleteMessagingMediaUseCase(singletonCImpl.provideMessagingCleanerRepositoryProvider.get());
    }

    private CheckRootAccessUseCase checkRootAccessUseCase() {
      return new CheckRootAccessUseCase(singletonCImpl.provideRootRepositoryProvider.get());
    }

    private CleanSystemCacheUseCase cleanSystemCacheUseCase() {
      return new CleanSystemCacheUseCase(singletonCImpl.provideRootRepositoryProvider.get());
    }

    private AnalyzeStorageUseCase analyzeStorageUseCase() {
      return new AnalyzeStorageUseCase(singletonCImpl.provideStorageAnalyzerRepositoryProvider.get());
    }

    private AnalyzeUnusedAppsUseCase analyzeUnusedAppsUseCase() {
      return new AnalyzeUnusedAppsUseCase(singletonCImpl.provideUnusedAppRepositoryProvider.get());
    }

    private UninstallAppUseCase uninstallAppUseCase() {
      return new UninstallAppUseCase(singletonCImpl.provideUnusedAppRepositoryProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.classifierViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.dashboardViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.duplicateViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.emptyFolderViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.leftoverViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.messagingCleanerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.rootModeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.storageAnalyzerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
      this.unusedAppViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 9);
    }

    @Override
    public Map<String, Provider<ViewModel>> getHiltViewModelMap() {
      return ImmutableMap.<String, Provider<ViewModel>>builderWithExpectedSize(10).put("com.smartcleaner.presentation.classifier.ClassifierViewModel", ((Provider) classifierViewModelProvider)).put("com.smartcleaner.presentation.dashboard.DashboardViewModel", ((Provider) dashboardViewModelProvider)).put("com.smartcleaner.presentation.duplicate.DuplicateViewModel", ((Provider) duplicateViewModelProvider)).put("com.smartcleaner.presentation.emptyfolder.EmptyFolderViewModel", ((Provider) emptyFolderViewModelProvider)).put("com.smartcleaner.presentation.leftover.LeftoverViewModel", ((Provider) leftoverViewModelProvider)).put("com.smartcleaner.presentation.messaging.MessagingCleanerViewModel", ((Provider) messagingCleanerViewModelProvider)).put("com.smartcleaner.presentation.root.RootModeViewModel", ((Provider) rootModeViewModelProvider)).put("com.smartcleaner.presentation.settings.SettingsViewModel", ((Provider) settingsViewModelProvider)).put("com.smartcleaner.presentation.storage.StorageAnalyzerViewModel", ((Provider) storageAnalyzerViewModelProvider)).put("com.smartcleaner.presentation.unusedapp.UnusedAppViewModel", ((Provider) unusedAppViewModelProvider)).build();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.smartcleaner.presentation.classifier.ClassifierViewModel 
          return (T) new ClassifierViewModel(viewModelCImpl.classifyJunkFilesUseCase());

          case 1: // com.smartcleaner.presentation.dashboard.DashboardViewModel 
          return (T) new DashboardViewModel(singletonCImpl.provideDashboardRepositoryProvider.get());

          case 2: // com.smartcleaner.presentation.duplicate.DuplicateViewModel 
          return (T) new DuplicateViewModel(viewModelCImpl.findDuplicatesUseCase(), viewModelCImpl.deleteDuplicatesUseCase());

          case 3: // com.smartcleaner.presentation.emptyfolder.EmptyFolderViewModel 
          return (T) new EmptyFolderViewModel(viewModelCImpl.scanEmptyFoldersUseCase(), viewModelCImpl.deleteEmptyFoldersUseCase());

          case 4: // com.smartcleaner.presentation.leftover.LeftoverViewModel 
          return (T) new LeftoverViewModel(viewModelCImpl.scanLeftoverFilesUseCase(), viewModelCImpl.deleteLeftoverFilesUseCase());

          case 5: // com.smartcleaner.presentation.messaging.MessagingCleanerViewModel 
          return (T) new MessagingCleanerViewModel(viewModelCImpl.scanMessagingAppsUseCase(), viewModelCImpl.deleteMessagingMediaUseCase());

          case 6: // com.smartcleaner.presentation.root.RootModeViewModel 
          return (T) new RootModeViewModel(viewModelCImpl.checkRootAccessUseCase(), viewModelCImpl.cleanSystemCacheUseCase());

          case 7: // com.smartcleaner.presentation.settings.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.providePreferencesRepositoryProvider.get());

          case 8: // com.smartcleaner.presentation.storage.StorageAnalyzerViewModel 
          return (T) new StorageAnalyzerViewModel(viewModelCImpl.analyzeStorageUseCase());

          case 9: // com.smartcleaner.presentation.unusedapp.UnusedAppViewModel 
          return (T) new UnusedAppViewModel(viewModelCImpl.analyzeUnusedAppsUseCase(), viewModelCImpl.uninstallAppUseCase(), singletonCImpl.provideUnusedAppRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends SmartCleanerApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;

      initialize();

    }

    @SuppressWarnings("unchecked")
    private void initialize() {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends SmartCleanerApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends SmartCleanerApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<JunkRepository> provideJunkRepositoryProvider;

    private Provider<PreferencesRepository> providePreferencesRepositoryProvider;

    private Provider<AutoCleanWorker_AssistedFactory> autoCleanWorker_AssistedFactoryProvider;

    private Provider<JunkClassifier> junkClassifierProvider;

    private Provider<JunkClassifierRepository> provideJunkClassifierRepositoryProvider;

    private Provider<DashboardRepository> provideDashboardRepositoryProvider;

    private Provider<DuplicateFinderRepository> provideDuplicateFinderRepositoryProvider;

    private Provider<EmptyFolderRepository> provideEmptyFolderRepositoryProvider;

    private Provider<LeftoverRepository> provideLeftoverRepositoryProvider;

    private Provider<MessagingCleanerRepository> provideMessagingCleanerRepositoryProvider;

    private Provider<RootRepository> provideRootRepositoryProvider;

    private Provider<StorageAnalyzerRepository> provideStorageAnalyzerRepositoryProvider;

    private Provider<UnusedAppRepository> provideUnusedAppRepositoryProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private Map<String, Provider<WorkerAssistedFactory<? extends ListenableWorker>>> mapOfStringAndProviderOfWorkerAssistedFactoryOf(
        ) {
      return ImmutableMap.<String, Provider<WorkerAssistedFactory<? extends ListenableWorker>>>of("com.smartcleaner.data.worker.AutoCleanWorker", ((Provider) autoCleanWorker_AssistedFactoryProvider));
    }

    private HiltWorkerFactory hiltWorkerFactory() {
      return WorkerFactoryModule_ProvideFactoryFactory.provideFactory(mapOfStringAndProviderOfWorkerAssistedFactoryOf());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideJunkRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<JunkRepository>(singletonCImpl, 1));
      this.providePreferencesRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<PreferencesRepository>(singletonCImpl, 2));
      this.autoCleanWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<AutoCleanWorker_AssistedFactory>(singletonCImpl, 0));
      this.junkClassifierProvider = DoubleCheck.provider(new SwitchingProvider<JunkClassifier>(singletonCImpl, 4));
      this.provideJunkClassifierRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<JunkClassifierRepository>(singletonCImpl, 3));
      this.provideDashboardRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<DashboardRepository>(singletonCImpl, 5));
      this.provideDuplicateFinderRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<DuplicateFinderRepository>(singletonCImpl, 6));
      this.provideEmptyFolderRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<EmptyFolderRepository>(singletonCImpl, 7));
      this.provideLeftoverRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<LeftoverRepository>(singletonCImpl, 8));
      this.provideMessagingCleanerRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<MessagingCleanerRepository>(singletonCImpl, 9));
      this.provideRootRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<RootRepository>(singletonCImpl, 10));
      this.provideStorageAnalyzerRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<StorageAnalyzerRepository>(singletonCImpl, 11));
      this.provideUnusedAppRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<UnusedAppRepository>(singletonCImpl, 12));
    }

    @Override
    public void injectSmartCleanerApplication(SmartCleanerApplication smartCleanerApplication) {
      injectSmartCleanerApplication2(smartCleanerApplication);
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return ImmutableSet.<Boolean>of();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    @CanIgnoreReturnValue
    private SmartCleanerApplication injectSmartCleanerApplication2(
        SmartCleanerApplication instance) {
      SmartCleanerApplication_MembersInjector.injectWorkerFactory(instance, hiltWorkerFactory());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.smartcleaner.data.worker.AutoCleanWorker_AssistedFactory 
          return (T) new AutoCleanWorker_AssistedFactory() {
            @Override
            public AutoCleanWorker create(Context context, WorkerParameters workerParams) {
              return new AutoCleanWorker(context, workerParams, singletonCImpl.provideJunkRepositoryProvider.get(), singletonCImpl.providePreferencesRepositoryProvider.get());
            }
          };

          case 1: // com.smartcleaner.domain.repository.JunkRepository 
          return (T) RepositoryModule_ProvideJunkRepositoryFactory.provideJunkRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.smartcleaner.domain.repository.PreferencesRepository 
          return (T) RepositoryModule_ProvidePreferencesRepositoryFactory.providePreferencesRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.smartcleaner.domain.repository.JunkClassifierRepository 
          return (T) RepositoryModule_ProvideJunkClassifierRepositoryFactory.provideJunkClassifierRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.junkClassifierProvider.get());

          case 4: // com.smartcleaner.data.ml.JunkClassifier 
          return (T) new JunkClassifier(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 5: // com.smartcleaner.domain.repository.DashboardRepository 
          return (T) RepositoryModule_ProvideDashboardRepositoryFactory.provideDashboardRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 6: // com.smartcleaner.domain.repository.DuplicateFinderRepository 
          return (T) RepositoryModule_ProvideDuplicateFinderRepositoryFactory.provideDuplicateFinderRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 7: // com.smartcleaner.domain.repository.EmptyFolderRepository 
          return (T) RepositoryModule_ProvideEmptyFolderRepositoryFactory.provideEmptyFolderRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 8: // com.smartcleaner.domain.repository.LeftoverRepository 
          return (T) RepositoryModule_ProvideLeftoverRepositoryFactory.provideLeftoverRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 9: // com.smartcleaner.domain.repository.MessagingCleanerRepository 
          return (T) RepositoryModule_ProvideMessagingCleanerRepositoryFactory.provideMessagingCleanerRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 10: // com.smartcleaner.domain.repository.RootRepository 
          return (T) RepositoryModule_ProvideRootRepositoryFactory.provideRootRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 11: // com.smartcleaner.domain.repository.StorageAnalyzerRepository 
          return (T) RepositoryModule_ProvideStorageAnalyzerRepositoryFactory.provideStorageAnalyzerRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 12: // com.smartcleaner.domain.repository.UnusedAppRepository 
          return (T) RepositoryModule_ProvideUnusedAppRepositoryFactory.provideUnusedAppRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
