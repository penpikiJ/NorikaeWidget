package com.example.norikaewidget

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.ScriptGroup

import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
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
        val routeSpinner = findViewById<Spinner>(R.id.routespinner)
        val routeList:ArrayList<String>

        //ファイルとりあえず絶対パスで置いた。スピナーリソースに入れる方法、がわかれば行けそう。でもこれDB必要な感じあるし、あればSQLで楽に持ってこれそう
        val filename = "app/resouruces/"+ registeredStationName.text.toString() +".txt"
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

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        ).build()


        registButton.setOnClickListener{
            getSharedPreferences("savedata",0)
            val prefs:SharedPreferences = getSharedPreferences("savedata", MODE_PRIVATE)
            val stationName = registeredStationName.text.toString()
            val editor = prefs.edit()
            editor.putString("RegisteredStation", stationName)
            editor.apply()
        }
/**
        ArrayAdapter.createFromResource(
            this,
            R.array.planets_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            routeSpinner.adapter = adapter
        }
*/
        routeSpinner.setOnClickListener{
            val prefs:SharedPreferences = getSharedPreferences("savedata", MODE_PRIVATE)
            val stationName = prefs.getString("RegisteredStation", null)
            var routeList:ArrayList<String> = getRouteList(stationName.toString().replace("駅",""))
            routeList = INSTANCE.
            routeSpinner.resources = routeList
        }

    }
    override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences {
        return super.getSharedPreferences(name, mode)
    }

    fun getRouteList(stationName:String): ArrayList<String> {
//ファイルの名前に駅名入れてそれを検索する感じでいいか。同じ名前あったら一覧みたいのが共通認識のために必要
        routeList = loadAllByIds(stationName)
        return routeList
    }

//    fun MaterialPickerOnPositiveButtonClickListener
//メモ　駅名入力できればAutoCompleteにしておきたい
//必要な情報は、入力された駅から出ている路線の一覧、その路線の方向（千葉からなら東京方面か木更津方面かみたいな）、AutoComplete用の駅名一覧、駅・路線・方向がわかったらそこから時刻表取ればOK
// とりあえず北習志野で作る
}


@Entity
data class StationTimeSchedule(
   // @PrimaryKey val station: String,
    @ColumnInfo(name = "station") val station: String?,
    @ColumnInfo(name = "route") val route: String?,
    @ColumnInfo(name = "direction") val direction: String?,
    @ColumnInfo(name = "daytype") val daytype: Int,
    @ColumnInfo(name = "scedulehour") val scedulehour: Int,
    @ColumnInfo(name = "sceduleminute") val sceduleminute: Int?
)

@Dao
interface StationTimeScheduleDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<StationTimeSchedule>

    @Query("SELECT * FROM user WHERE uid IN (:station)")
    fun loadAllByIds(staion: IntArray): List<StationTimeSchedule>

    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): StationTimeSchedule

    @Insert
    fun insertAll(vararg users: StationTimeSchedule)

    @Delete
    fun delete(user: StationTimeSchedule)
}
@Database(entities = arrayOf(StationTimeSchedule::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): StationTimeSchduleDao

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