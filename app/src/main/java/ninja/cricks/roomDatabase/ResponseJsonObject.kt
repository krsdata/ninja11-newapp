package ninja.cricks.roomDatabase

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.JsonObject

@Entity(tableName = "ResponseJsonObject")
data class ResponseJsonObject(
    @PrimaryKey(autoGenerate = true)
    val type: Long = 0,
    val timestamp: Long = 0,
    val res: JsonObject
)
