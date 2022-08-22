package fancode.cricks.roomDatabase

import android.content.Context
import androidx.room.*

@Database(entities = [Response::class, ResponseJsonObject::class],version = 1)
@TypeConverters(ResConverter::class)
abstract class ResponseDatabase: RoomDatabase() {
    abstract fun responseDao(): ResponseDao
    companion object{
        @Volatile
        private var instance: ResponseDatabase? = null

        fun getInstance(context: Context): ResponseDatabase {
            if (instance == null) {
                synchronized(this) {
                    instance = Room.databaseBuilder(context.applicationContext, ResponseDatabase::class.java, "responseDB").build()
                }
            }
            return instance!!
        }
    }
}