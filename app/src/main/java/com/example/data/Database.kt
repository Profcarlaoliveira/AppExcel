package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val id: Int = 1,
    val score: Int = 0,
    val unlockedLevel: Int = 1, // 1 to 4
    val completedExercises: String = "", // Comma-separated list of exercise IDs
    val completedQuizzes: String = "" // Comma-separated list of level IDs
) {
    fun isExerciseCompleted(exerciseId: String): Boolean {
        return completedExercises.split(",").contains(exerciseId)
    }

    fun isQuizCompleted(levelId: Int): Boolean {
        return completedQuizzes.split(",").contains(levelId.toString())
    }
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getProgressFlow(): Flow<UserProgress?>

    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    suspend fun getProgress(): UserProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: UserProgress)
}

@Database(entities = [UserProgress::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun progressDao(): ProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "edusheets_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class ProgressRepository(private val progressDao: ProgressDao) {
    val progressFlow: Flow<UserProgress?> = progressDao.getProgressFlow()

    suspend fun getProgressOrDefault(): UserProgress {
        return progressDao.getProgress() ?: UserProgress()
    }

    suspend fun updateScore(pointsToAdd: Int) {
        val current = getProgressOrDefault()
        val updated = current.copy(score = current.score + pointsToAdd)
        progressDao.saveProgress(updated)
    }

    suspend fun completeExercise(exerciseId: String, pointsToAdd: Int) {
        val current = getProgressOrDefault()
        val currentList = current.completedExercises.split(",").filter { it.isNotEmpty() }.toMutableSet()
        if (currentList.add(exerciseId)) {
            val updatedString = currentList.joinToString(",")
            val updated = current.copy(
                completedExercises = updatedString,
                score = current.score + pointsToAdd
            )
            progressDao.saveProgress(updated)
        }
    }

    suspend fun completeQuiz(levelId: Int, pointsToAdd: Int) {
        val current = getProgressOrDefault()
        val currentList = current.completedQuizzes.split(",").filter { it.isNotEmpty() }.toMutableSet()
        if (currentList.add(levelId.toString())) {
            val updatedString = currentList.joinToString(",")
            val nextLevel = if (levelId == current.unlockedLevel && current.unlockedLevel < 4) {
                current.unlockedLevel + 1
            } else {
                current.unlockedLevel
            }
            val updated = current.copy(
                completedQuizzes = updatedString,
                unlockedLevel = nextLevel,
                score = current.score + pointsToAdd
            )
            progressDao.saveProgress(updated)
        }
    }

    suspend fun resetProgress() {
        progressDao.saveProgress(UserProgress())
    }
}
