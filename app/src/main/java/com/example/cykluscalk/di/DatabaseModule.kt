package com.example.cykluscalk.di

import android.content.Context
import androidx.room.Room
import com.example.cykluscalk.data.AppDatabase
import com.example.cykluscalk.data.DayRecordDao
import com.example.cykluscalk.data.PregnancyDao
import com.example.cykluscalk.data.TagDao
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "cyklus_calk_db"
        )
        .fallbackToDestructiveMigration() // Pro zjednodušení vývoje při změně schématu
        .build()
    }

    @Provides
    fun provideDayRecordDao(database: AppDatabase): DayRecordDao {
        return database.dayRecordDao()
    }

    @Provides
    fun provideTagDao(database: AppDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    fun providePregnancyDao(database: AppDatabase): PregnancyDao {
        return database.pregnancyDao()
    }
}
