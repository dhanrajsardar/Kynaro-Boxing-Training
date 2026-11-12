package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val missionDay: Int,
    val durationSeconds: Int,
    val kcalBurned: Int,
    val punchesLanded: Int,
    val roundsCompleted: Int,
    val xpEarned: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "completed_camp_days")
data class CompletedCampDayEntity(
    @PrimaryKey val dayNumber: Int,
    val completedAt: Long = System.currentTimeMillis(),
    val punchesLanded: Int,
    val kcalBurned: Int
)

@Dao
interface KynaroDao {
    @Query("SELECT * FROM workout_sessions ORDER BY timestamp DESC")
    fun getAllWorkoutSessions(): Flow<List<WorkoutSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSession(session: WorkoutSessionEntity)

    @Query("SELECT * FROM completed_camp_days ORDER BY dayNumber ASC")
    fun getCompletedCampDays(): Flow<List<CompletedCampDayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markCampDayCompleted(entity: CompletedCampDayEntity)

    @Query("DELETE FROM workout_sessions")
    suspend fun clearSessions()

    @Query("DELETE FROM completed_camp_days")
    suspend fun clearCampDays()
}

@Database(
    entities = [WorkoutSessionEntity::class, CompletedCampDayEntity::class],
    version = 1,
    exportSchema = false
)
abstract class KynaroDatabase : RoomDatabase() {
    abstract fun kynaroDao(): KynaroDao

    companion object {
        @Volatile
        private var INSTANCE: KynaroDatabase? = null

        fun getInstance(context: Context): KynaroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KynaroDatabase::class.java,
                    "kynaro_boxing_db_v2"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
