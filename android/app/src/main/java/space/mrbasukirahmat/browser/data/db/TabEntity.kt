package space.mrbasukirahmat.browser.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabs")
data class TabEntity(
    @PrimaryKey
    val id: String,
    val url: String,
    val title: String,
    val favicon: String? = null,
    val isPinned: Boolean = false,
    val source: String = "LOCAL", // "LOCAL" or "MYBEME"
    val timestamp: Long = System.currentTimeMillis()
)
