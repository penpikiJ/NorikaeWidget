package com.example.norikaewidget


import android.content.Context
import androidx.room.*

//簡単なデータベースで取り出せるかの確認
@Entity
data class autocompletelist(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "route") val route: String?
)

@Dao
interface autocompletelistDao {
    @Query("select * from autocompletelist")
    fun getAll(): List<autocompletelist>

    @Query("Insert into autocompletelist (route) values ('京成線'),('新京成線')")
    fun insert()

    @Query("Delete from autocompletelist")
    fun delete()
}

@Database(entities = arrayOf(autocompletelist::class,StationRouteUpDownDaytype::class), version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun autocompletelistDao(): autocompletelistDao
    abstract fun StationRouteUpDownDaytypeDao():StationRouteUpDownDaytypeDao

    companion object {
        val DB_NAME = "user.db"
        private lateinit var instance: AppDatabase

        fun getInstance(context: Context): AppDatabase {
            if (!::instance.isInitialized) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).fallbackToDestructiveMigration().build()
                //fallbackはmigration書くの面倒で入れているだけなので、本番環境では.buildのみでいい
            }
            return instance
        }
    }


}

//データベースのテーブル定義
@Entity
data class StationRouteUpDownDaytype(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "station") val station: String?,
    @ColumnInfo(name = "route") val route: String?,
    @ColumnInfo(name = "direction") val direction: String,
    @ColumnInfo(name = "daytype") val daytype: String,
    @ColumnInfo(name = "csv") val csv: String
)

@Dao
interface StationRouteUpDownDaytypeDao {

    @Query("SELECT * FROM StationRouteUpDownDaytype WHERE station = :station")
    fun  loadAllByStation(station: String): List<StationRouteUpDownDaytype>

    @Query("SELECT DISTINCT route FROM StationRouteUpDownDaytype WHERE station = :station")
    fun  getRouteByStation(station: String): List<String>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT DISTINCT direction FROM StationRouteUpDownDaytype WHERE station = :station AND route = :route")
    fun getDirectionByStationRoutes(station: String,route:String): List<String>

    @Query("SELECT DISTINCT csv FROM StationRouteUpDownDaytype WHERE station = :station AND route = :route AND direction= :direction AND daytype = :daytype")
    fun getCsvnameByInfo(station: String,route: String?,direction: String,daytype: String): String

    @Query("DELETE FROM StationRouteUpDownDaytype")
    fun deleteStationRouteUpDownDaytype()

    @Query("INSERT INTO StationRouteUpDownDaytype(station,route,direction,daytype,csv) VALUES (:station,:route,:direction,:daytype,:csv)")
    fun insertStationRouteUpDownDaytype(station: String,route: String?,direction: String,daytype: String,csv: String)
}

