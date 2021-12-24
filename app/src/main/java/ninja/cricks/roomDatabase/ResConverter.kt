package ninja.cricks.roomDatabase

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonObject
import ninja.cricks.models.ContestPreferenceModel
import ninja.cricks.models.UsersPostDBResponse
import retrofit2.Response

class ResConverter {
    
    @TypeConverter
    fun fromResToString(value: UsersPostDBResponse): String {
        val gson: String = Gson().toJson(value)
        return gson
    }
    
    @TypeConverter
    fun  fromStringToRes(string: String): UsersPostDBResponse {
        return Gson().fromJson(string, UsersPostDBResponse::class.java)
    }

    @TypeConverter
    fun fromJsonResToString(value: JsonObject): String {
        val gson: String = Gson().toJson(value)
        return gson
    }

    @TypeConverter
    fun  fromStringToJsonRes(string: String): JsonObject {
        return Gson().fromJson(string, JsonObject ::class.java)
    }
}