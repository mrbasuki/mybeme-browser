package space.mrbasukirahmat.browser.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TabDao {
    @Query("SELECT * FROM tabs ORDER BY timestamp DESC")
    fun getAllTabs(): Flow<List<TabEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTab(tab: TabEntity)

    @Delete
    suspend fun deleteTab(tab: TabEntity)

    @Query("DELETE FROM tabs WHERE id = :tabId")
    suspend fun deleteTabById(tabId: String)

    @Query("DELETE FROM tabs")
    suspend fun clearAllTabs()
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY lastVisited DESC LIMIT 100")
    fun getRecentHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordVisit(history: HistoryEntity)
}
