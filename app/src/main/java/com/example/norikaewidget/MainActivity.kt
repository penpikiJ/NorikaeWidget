package com.example.norikaewidget

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.io.BufferedReader
import java.io.FileInputStream
import java.lang.Exception
import java.nio.BufferUnderflowException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val registButton = findViewById<Button>(R.id.registButton)
        val registeredStationName = findViewById<TextView>(R.id.textView)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        ).build()

        var stationName:String? = ""

        registButton.setOnClickListener{  //登録ボタンの挙動
            getSharedPreferences("savedata",0)
            val prefs:SharedPreferences = getSharedPreferences("savedata", MODE_PRIVATE)
            stationName = registeredStationName.text.toString()
            val editor = prefs.edit()
            editor.putString("RegisteredStation", stationName)
            editor.apply()
        }
//main関数の中でデーベースにアクセスしてはいけないとの事なので、 kotlinのコルーチンを導入しないといけない
        val routeSpinner = findViewById<Spinner>(R.id.routespinner)

        var routeList = mutableListOf<String>()
        val ref = this
        val lifecycleScope: CoroutineScope
        class MyViewModel: Fragment() {
            init {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){ {
                    // Coroutine that will be canceled when the ViewModel is cleared.
                    ref.runOnUiThread{
                        val prefs: SharedPreferences = getSharedPreferences("savedata", MODE_PRIVATE)
                        stationName = prefs.getString("RegisteredStation", null)
                        val database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database-name").build()
                        val StationRouteUpDownDaytypeDao = database.StationRouteUpDownDaytypeDao()


                        val stationInfoList =
                            StationRouteUpDownDaytypeDao.getRoutesByStation(
                                stationName.toString().replace("駅", "")
                            ).toMutableList()

                        var i = 0
                        while (stationInfoList[i] != null) {
                            routeList.add(stationInfoList[i].route.toString())
                            i++
                        }

                    }
                }
                }
            }
        }

            //stationInfoListStr[0] = (stationInfoList[0].station,stationInfoList[0].route,stationInfoList[0].updown,stationInfoList[0].daytype)
            //まずは路線の一覧出力
            /*
            var adapter = ArrayAdapter(this,android.R.layout.simple_spinner_item,routeList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            routeSpinner.adapter = adapter
        */
            //排他的に取ってこれるならそっちのほうがいいな。路線だけ検索とか




                //var stationInfoListStr:Array<Array<String>>
//stationInfoListからroutelistに路線の一覧取り出す予定



                var adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, routeList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                routeSpinner.adapter = adapter




       // routeList = StationRouteUpDownDaytypeDao.loadAllByIds(stationName)

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
    }

    override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences {
        return super.getSharedPreferences(name, mode)
    }

//メモ　駅名入力できればAutoCompleteにしておきたい

}

//データベースのテーブル定義
@Entity
data class StationRouteUpDownDaytype(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "station") val station: String?,
    @ColumnInfo(name = "route") val route: String?,
    @ColumnInfo(name = "updown") val updown: String?,
    @ColumnInfo(name = "daytype") val daytype: Int
    )

@Entity
data class StationTimeSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "scedulehour") val scedulehour: Int,
    @ColumnInfo(name = "sceduleminute") val sceduleminute: Int?
)

@Dao
interface StationRouteUpDownDaytypeDao {

    @Query("SELECT * FROM StationRouteUpDownDaytype WHERE :station = station")
    fun  loadAllByIds(station: String): List<StationRouteUpDownDaytype>

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT DISTINCT * FROM StationRouteUpDownDaytype WHERE :station = station")
    fun getRoutesByStation(station: String): List<StationRouteUpDownDaytype>

}

@Dao
interface StationTimeScheduleDao {
/*
    @Query("SELECT * FROM StationTimeSchedule WHERE station IN (:station)")
    fun loadAllByIds(station: String): List<StationTimeSchedule>
*/
}

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
                        AppDatabase::class.java, "StationTimeSchedule.db")
                        .addCallback(object : RoomDatabase.Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)

                                val assetManager = context.resources.assets
                                var sql:String = ""+"INSERT INTO 'kitanarashino' VALUES"+""
                                try {
                                    val inputStream = assetManager.open("kitanarashino.csv")
                                    val inputStreamReader = InputStreamReader(inputStream)
                                    val bufferedReader = BufferedReader(inputStreamReader)
                                    var line: String? = bufferedReader.readLine()

                                    while(line != null){
                                        val rowData = line.split(",")
                                        sql += "('${rowData[0]}','${rowData[1]}','${rowData[2]}', ${rowData[3]}, ${rowData[4]}, ${rowData[5]}),"
                                        line = bufferedReader.readLine()
                                    }
                                    inputStream.close()
                                }catch (e:Exception){
                                    e.printStackTrace()
                                }
                                sql = sql.substring(0, sql.count() -1)
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