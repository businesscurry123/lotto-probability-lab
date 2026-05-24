package com.lottolab.probability.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [DrawResultEntity::class, SavedNumberSetEntity::class, DailyCombinationEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class LottoDatabase : RoomDatabase() {
    abstract fun drawResultDao(): DrawResultDao
    abstract fun savedNumberSetDao(): SavedNumberSetDao
    abstract fun dailyCombinationDao(): DailyCombinationDao

    companion object {
        @Volatile
        private var instance: LottoDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE saved_number_sets ADD COLUMN favorite INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE saved_number_sets ADD COLUMN collectionName TEXT NOT NULL DEFAULT '기본'")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS daily_combinations (
                        date TEXT NOT NULL,
                        type TEXT NOT NULL,
                        number1 INTEGER NOT NULL,
                        number2 INTEGER NOT NULL,
                        number3 INTEGER NOT NULL,
                        number4 INTEGER NOT NULL,
                        number5 INTEGER NOT NULL,
                        number6 INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        PRIMARY KEY(date, type)
                    )
                    """.trimIndent(),
                )
            }
        }

        fun getInstance(context: Context): LottoDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                LottoDatabase::class.java,
                "lotto_probability_lab.db",
            ).addMigrations(MIGRATION_1_2).build().also { instance = it }
        }
    }
}
