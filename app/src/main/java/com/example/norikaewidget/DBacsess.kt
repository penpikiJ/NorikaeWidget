package com.example.norikaewidget


import android.content.Context
import androidx.room.*

//簡単なデータベースで取り出せるかの確認
@Entity
data class spinnerlist(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "route") val route: String?
)

@Dao
interface spinnerlistDao {
    @Query("select * from spinnerlist")
    fun getAll(): List<spinnerlist>

    @Query("Insert into spinnerlist (route) values ('京成線'),('新京成線')")
    fun insert()

    @Update
    fun update(route: spinnerlist)

    @Query("Delete from spinnerlist")
    fun delete()
}

@Database(entities = arrayOf(spinnerlist::class,StationRouteUpDownDaytype::class), version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun spinnerlistDao(): spinnerlistDao
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
    @ColumnInfo(name = "updown") val updown: Int,
    @ColumnInfo(name = "daytype") val daytype: Int
)
/*
@Entity
data class StationTimeSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "scedulehour") val scedulehour: Int,
    @ColumnInfo(name = "sceduleminute") val sceduleminute: Int?
)
*/
@Dao
interface StationRouteUpDownDaytypeDao {

    @Query("SELECT * FROM StationRouteUpDownDaytype WHERE station = :station")
    fun  loadAllByStation(station: String): List<StationRouteUpDownDaytype>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT DISTINCT * FROM StationRouteUpDownDaytype WHERE station = :station AND route = :route")
    fun getDirectionByStationRoutes(station: String,route:String): List<StationRouteUpDownDaytype>

    @Query("DELETE FROM StationRouteUpDownDaytype")
    fun deleteStationRouteUpDownDaytype()

    @Query("INSERT INTO StationRouteUpDownDaytype(station,route,updown,daytype) VALUES (:station,:route,:updown,:daytype)")
    fun insertStationRouteUpDownDaytype(station: String,route: String?,updown: Int,daytype: Int)
}

/*
@Dao
interface StationTimeScheduleDao {
    @Query("SELECT * FROM StationTimeSchedule WHERE station IN (:station)")
    fun loadAllByIds(station: String): List<StationTimeSchedule>
}
 */
/*
@Database(entities = arrayOf(StationRouteUpDownDaytype::class), version = 1,exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun StationRouteUpDownDaytypeDao(): StationRouteUpDownDaytypeDao

    companion object {

        private var INSTANCE: AppDatabase? = null

        private val lock = Any()

        fun getInstance(context: Context): AppDatabase {
            synchronized(lock) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        AppDatabase::class.java, "database-name")
                        .addCallback(object : RoomDatabase.Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)

                                val assetManager = context.resources.assets
                                var sql:String = ""+"INSERT INTO 'StationRouteUpDownDaytype' VALUES"+""
                                try {
                                sql += "北習志野,新京成線,0,0"
                                /*
                                    val inputStream = assetManager.open("StationRouteUpDownDaytype.csv")
                                    val inputStreamReader = InputStreamReader(inputStream)
                                    val bufferedReader = BufferedReader(inputStreamReader)
                                    var line: String? = bufferedReader.readLine()

                                    while(line != null){
                                        val rowData = line.split(",")
                                        sql += "('${rowData[0]}','${rowData[1]}','${rowData[2]}', ${rowData[3]}),"
                                        line = bufferedReader.readLine()
                                    }
                                    inputStream.close()

                                     */
                                }catch (e:Exception){
                                    e.printStackTrace()
                                }
                                //sql = sql.substring(0, sql.count() -1)
                                db.execSQL(sql)
                            }
                        })
                        .build()
                }
                return INSTANCE!!
            }
        }
    }
}

*/

/*パスでのcsvファイル読み込みを、駅名・路線・上下が決まった後に次のページに移動するときに使う。ロジック作ったけど次のページで使うからここではコメントアウトしてメモ
val filename = "app/resouruces/StationRouteUpDownDaytype.txt"
val fileInputStream : FileInputStream = openFileInput(filename)
val reader = BufferedReader(InputStreamReader(fileInputStream, "UTF-8"))
var lineBuffer: String
var stationList : ArrayList<String> = arrayListOf()
var i = 0
while (true) {
    lineBuffer = reader.readLine()
    if (lineBuffer != null) {
        stationList[i] = lineBuffer
        i++
    } else {
        break
    }
}
*/
// val database = AppDatabase.getInstance(applicationContext)
/*
            class MyViewModel : Fragment() {
                init {
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        // Coroutine that will be canceled when the ViewModel is cleared.
                        ref.runOnUiThread {
                            val prefs: SharedPreferences =
                                getSharedPreferences("savedata", MODE_PRIVATE)
                            stationName = prefs.getString("RegisteredStation", null)
                            stationName = "北習志野" //DBアクセスできるまでこれで実験
                            val StationRouteUpDownDaytypeDao = database.StationRouteUpDownDaytypeDao()

                            val stationInfoList =
                                (StationRouteUpDownDaytypeDao.getRoutesByStation(stationName.toString().replace("駅", ""))).toMutableList()

                            var i = 0
                            while (stationInfoList[i] != null) {
                                routeList.add(stationInfoList[i].route.toString())
                                i++
                            }
                        }
                    }
                }
            }
        */
