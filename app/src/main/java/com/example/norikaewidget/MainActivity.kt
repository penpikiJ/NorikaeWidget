package com.example.norikaewidget

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.BufferedReader
import java.io.FileInputStream
import java.lang.Exception
import java.nio.BufferUnderflowException
import android.R.attr.fragment
import android.R.attr.fragmentEnterTransition
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.text.Editable
import android.text.TextUtils.split
import android.view.ViewGroup

import android.view.LayoutInflater
import kotlinx.coroutines.Dispatchers.Main
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.io.File
import java.nio.file.Paths


class MainActivity : AppCompatActivity(),MyListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.blanklayout)
/*
        val registButton = findViewById<Button>(R.id.registButton)
        val registeredStationName = findViewById<TextView>(R.id.textView)
*/
        var stationName: String? = ""
/*
        registButton.setOnClickListener {  //登録ボタンの挙動
            getSharedPreferences("savedata", 0)
            val prefs: SharedPreferences = getSharedPreferences("savedata", MODE_PRIVATE)
            stationName = registeredStationName.text.toString()
            val editor = prefs.edit()
            editor.putString("RegisteredStation", stationName)
            editor.apply()
        }
*/
        val fragment = MainFragment()
        if (savedInstanceState == null) {
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment)
            transaction.commit()
            if (fragment != null && fragment is MainFragment) {
                //今はライフサイクルの場所的に呼べないっぽいので後で復活させる
            }
        }
    }

    override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences {
        return super.getSharedPreferences(name, mode)
    }

    override fun onClickButton() {
    }

    class MainFragment : Fragment() {

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            super.onCreateView(inflater, container, savedInstanceState)
            // 先ほどのレイアウトをここでViewとして作成します
            return inflater.inflate(R.layout.activity_main, container, false)
        }

        private var mListener: MyListener? = null

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val UpDown:List<String> = listOf("上り","下り")
            var adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                UpDown
            )
            view.findViewById<Spinner>(R.id.UpDownSpinner).adapter = adapter

            view.findViewById<Button>(R.id.registButton).setOnClickListener(object : View.OnClickListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onClick(v: View) { //ここviewじゃなくてvにしたら動いた
                    if (mListener != null) {
                        mListener?.onClickButton()
                    }
                    val intent = Intent(requireContext(), TimeSchedule::class.java)
                    startActivity(intent)
                }
            })

            view.findViewById<Button>(R.id.stationButton).setOnClickListener(object : View.OnClickListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onClick(v: View) { //ここviewじゃなくてvにしたら動いた
                    if (mListener != null) {
                        mListener?.onClickButton()
                    }
                    addItem()
                }
            })
        }

        // FragmentがActivityに追加されたら呼ばれるメソッド
        override fun onAttach(context: Context) {
            super.onAttach(context)
            // contextクラスがMyListenerを実装しているかをチェックする
            if (context is MyListener) {
                // リスナーをここでセットするようにします
                mListener = context
            }
        }

        // FragmentがActivityから離れたら呼ばれるメソッド
        override fun onDetach() {
            super.onDetach()
            // 画面からFragmentが離れたあとに処理が呼ばれることを避けるためにNullで初期化しておく
            mListener = null
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun addItem() {
            viewLifecycleOwner.lifecycleScope.launch {

                // ここからはIOスレッドで実行してもらう
                withContext(Dispatchers.IO) {
                    // テーブルに追加
                    val db = AppDatabase.getInstance(requireContext())
                    db.spinnerlistDao().delete()
                    db.spinnerlistDao().insert()

                    var station = view?.findViewById<EditText>(R.id.registeredStation) as EditText
                    db.StationRouteUpDownDaytypeDao().deleteStationRouteUpDownDaytype()

                    val fileInputStream  = resources.assets.open("StationRouteUpDownDaytype.csv")
                    val reader = BufferedReader(InputStreamReader(fileInputStream, "UTF-8"))
                    var lineBuffer: String
                    var stationList : ArrayList<String> = arrayListOf()
                    var k = 0
                    while (reader.readLine() != null) {
                        lineBuffer = reader.readLine()
                        if (lineBuffer != null) {
                            stationList.add(lineBuffer) //これ１行で読み込まれる
                            k++
                        } else {
                            break
                        }
                    }

                    var j = 0
                    while (j < stationList.size) {
                        var temp = stationList[j].split(",")
                        db.StationRouteUpDownDaytypeDao().insertStationRouteUpDownDaytype(temp[0],temp[1],temp[2].toInt(),temp[3].toInt())
                        j++
                    }
                    val gotlist = db.StationRouteUpDownDaytypeDao().loadAllByStation(station.text.toString())
                    //以下、排他的になるように処理
                    var i = 0
                    var x = 0
                    var flag = 0
                    var routeList = mutableListOf<String>()
                    while (i < gotlist.size) {
                        while(x < routeList.size){
                            if(routeList[x] == gotlist[i].route.toString()){
                                flag = 1
                                break
                            }
                            x++
                        }
                        if(flag == 0){
                            routeList.add(gotlist[i].route.toString())
                        }
                        flag = 0
                        x=0
                        i++
                    }
                    val routeSpinner = view?.findViewById<Spinner>(R.id.routespinner) as Spinner

                    var adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        routeList
                    )
                    activity?.runOnUiThread(java.lang.Runnable {routeSpinner.adapter = adapter})
                }
            }
        }
    }
}

    interface MyListener {
        fun onClickButton()
    }

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
