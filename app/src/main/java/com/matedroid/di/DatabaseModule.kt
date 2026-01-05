package com.matedroid.di

import android.content.Context
import androidx.room.Room
import com.matedroid.data.local.StatsDatabase
import com.matedroid.data.local.dao.AggregateDao
import com.matedroid.data.local.dao.ChargeSummaryDao
import com.matedroid.data.local.dao.DriveSummaryDao
import com.matedroid.data.local.dao.SyncStateDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideStatsDatabase(
        @ApplicationContext context: Context
    ): StatsDatabase {
        return Room.databaseBuilder(
            context,
            StatsDatabase::class.java,
            StatsDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()  // For development; use migrations in production
            .build()
    }

    @Provides
    @Singleton
    fun provideSyncStateDao(database: StatsDatabase): SyncStateDao {
        return database.syncStateDao()
    }

    @Provides
    @Singleton
    fun provideDriveSummaryDao(database: StatsDatabase): DriveSummaryDao {
        return database.driveSummaryDao()
    }

    @Provides
    @Singleton
    fun provideChargeSummaryDao(database: StatsDatabase): ChargeSummaryDao {
        return database.chargeSummaryDao()
    }

    @Provides
    @Singleton
    fun provideAggregateDao(database: StatsDatabase): AggregateDao {
        return database.aggregateDao()
    }
}
