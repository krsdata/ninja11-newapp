package ninja.cricks.roomDatabase

import androidx.room.Entity
import androidx.room.PrimaryKey
import ninja.cricks.models.UsersPostDBResponse

@Entity(tableName = "Response")
data class Response(
    @PrimaryKey(autoGenerate = true)
    val type: Long = 0,
    val timestamp: Long = 0,
    val res: UsersPostDBResponse
)

