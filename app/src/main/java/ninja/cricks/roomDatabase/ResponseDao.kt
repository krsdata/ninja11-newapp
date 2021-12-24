package ninja.cricks.roomDatabase

import androidx.lifecycle.LiveData
import androidx.room.*
import ninja.cricks.models.UsersPostDBResponse

@Dao
interface ResponseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveResponse(response: Response)

    @Query("SELECT * FROM response WHERE type = :mType LIMIT 1")
    fun getResponse(mType: Long): Response

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveResponseJsonObject(responseJsonObject: ResponseJsonObject)

    @Query("SELECT * FROM ResponseJsonObject WHERE type = :mType LIMIT 1")
    fun getResponseJsonObject(mType: Long): ResponseJsonObject
}