package com.vitanova.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vitanova.app.data.local.dao.BrainDao
import com.vitanova.app.data.local.dao.FitnessDao
import com.vitanova.app.data.local.dao.FocusDao
import com.vitanova.app.data.local.dao.HabitDao
import com.vitanova.app.data.local.dao.HrvDao
import com.vitanova.app.data.local.dao.MoodDao
import com.vitanova.app.data.local.dao.NutritionDao
import com.vitanova.app.data.local.dao.SleepDao
import com.vitanova.app.data.local.entity.AppUsage
import com.vitanova.app.data.local.entity.BrainTest
import com.vitanova.app.data.local.entity.CognitiveTest
import com.vitanova.app.data.local.entity.FitnessActivity
import com.vitanova.app.data.local.entity.FocusSession
import com.vitanova.app.data.local.entity.GpsPoint
import com.vitanova.app.data.local.entity.Habit
import com.vitanova.app.data.local.entity.HabitCompletion
import com.vitanova.app.data.local.entity.HrvReading
import com.vitanova.app.data.local.entity.Meal
import com.vitanova.app.data.local.entity.MoodEntry
import com.vitanova.app.data.local.entity.SleepSample
import com.vitanova.app.data.local.entity.SleepSession
import com.vitanova.app.data.local.entity.StepRecord

@Database(
    entities = [
        SleepSession::class,
        SleepSample::class,
        HrvReading::class,
        FitnessActivity::class,
        GpsPoint::class,
        StepRecord::class,
        FocusSession::class,
        AppUsage::class,
        Meal::class,
        CognitiveTest::class,
        BrainTest::class,
        Habit::class,
        HabitCompletion::class,
        MoodEntry::class
    ],
    version = 1,
    exportSchema = true
)
abstract class VitaNovaDatabase : RoomDatabase() {

    abstract fun sleepDao(): SleepDao
    abstract fun hrvDao(): HrvDao
    abstract fun fitnessDao(): FitnessDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun focusDao(): FocusDao
    abstract fun brainDao(): BrainDao
    abstract fun habitDao(): HabitDao
    abstract fun moodDao(): MoodDao

    companion object {
        private const val DATABASE_NAME = "vitanova.db"

        @Volatile
        private var instance: VitaNovaDatabase? = null

        fun getInstance(context: Context): VitaNovaDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    VitaNovaDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
