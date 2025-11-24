package com.smartcleaner.di

import android.content.Context
import com.smartcleaner.data.ml.JunkClassifier
import com.smartcleaner.data.repository.*
import com.smartcleaner.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideLeftoverRepository(
        @ApplicationContext context: Context
    ): LeftoverRepository {
        return LeftoverRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideJunkRepository(
        @ApplicationContext context: Context
    ): JunkRepository {
        return JunkRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideEmptyFolderRepository(
        @ApplicationContext context: Context
    ): EmptyFolderRepository {
        return EmptyFolderRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideUnusedAppRepository(
        @ApplicationContext context: Context
    ): UnusedAppRepository {
        return UnusedAppRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideJunkClassifierRepository(
        @ApplicationContext context: Context,
        classifier: JunkClassifier
    ): JunkClassifierRepository {
        return JunkClassifierRepositoryImpl(context, classifier)
    }

    @Provides
    @Singleton
    fun provideDuplicateFinderRepository(
        @ApplicationContext context: Context
    ): DuplicateFinderRepository {
        return DuplicateFinderRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideMessagingCleanerRepository(
        @ApplicationContext context: Context
    ): MessagingCleanerRepository {
        return MessagingCleanerRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideStorageAnalyzerRepository(
        @ApplicationContext context: Context
    ): StorageAnalyzerRepository {
        return StorageAnalyzerRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideRootRepository(
        @ApplicationContext context: Context
    ): RootRepository {
        return RootRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideDashboardRepository(
        @ApplicationContext context: Context
    ): DashboardRepository {
        return DashboardRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(
        @ApplicationContext context: Context
    ): PreferencesRepository {
        return PreferencesRepositoryImpl(context)
    }
}
