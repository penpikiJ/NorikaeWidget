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
import android.app.Activity
import android.text.Editable
import android.view.ViewGroup

import android.view.LayoutInflater
import kotlinx.coroutines.Dispatchers.Main
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import android.widget.Toast




class MainActivity : AppCompatActivity(),MyListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val registButton = findViewById<Button>(R.id.registButton)
        val registeredStationName = findViewById<TextView>(R.id.textView)

        var stationName:String? = ""

        registButton.setOnClickListener{  //登録ボタンの挙動
            getSharedPreferences("savedata",0)
            val prefs:SharedPreferences = getSharedPreferences("savedata", MODE_PRIVATE)
            stationName = registeredStationName.text.toString()
            val editor = prefs.edit()
            editor.putString("RegisteredStation", stationName)
            editor.apply()
        }
        //main関数の中でデーベースにアクセスしてはいけないとの事なので、 kotlinのコルーチンを導入
        //ここでデータベースにアクセスして配列を取得
        val routeSpinner = findViewById<Spinner>(R.id.routespinner)
        var routeList = mutableListOf<String>()

        var adapter = ArrayAdapter(this,android.R.layout.simple_spinner_item, routeList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        routeSpinner.adapter = adapter

        val fragment = MainFragment()
        if (savedInstanceState == null) {
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment)
            transaction.commit()
            if (fragment != null && fragment is MainFragment) {
                //fragment.addItem() 今はライフサイクルの場所的に呼べないっぽいので後で復活させる
            }
        }



        val stationButton = findViewById<Button>(R.id.stationButton)
        stationButton.setOnClickListener(){
            //routeList.add(0,"keisei")
            routeSpinner.adapter = adapter
        }
    }


    override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences {
        return super.getSharedPreferences(name, mode)
    }

    override fun onClickButton() {
        Toast.makeText(this, "MainFragmentからクリックされました!", Toast.LENGTH_SHORT).show()
    }

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
        view.findViewById<Button>(R.id.registButton).setOnClickListener(object:View.OnClickListener {
            override fun onClick(v :View) { //ここviewじゃなくてvにしたら動いた
                if (mListener != null) {
                    mListener?.onClickButton()
                }
                view.findViewById<TextView>(R.id.textView2).text = "フラグメントから入力"
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
//lifecycle呼ぶのこのタイミングじゃダメなんだろうな これはreturnのタイミング変えて対応する感じっぽい？
    fun addItem() {
        viewLifecycleOwner.lifecycleScope.launch {
            val db = AppDatabase.getInstance(requireContext())
            // ここからはIOスレッドで実行してもらう
            withContext(Dispatchers.IO) {
                // テーブルに追加
                db.spinnerlistDao().insert()
            }
            val gotlist = db.spinnerlistDao().getAll()
            val i: Int = 0
            var routeList = mutableListOf<String>()
            while (gotlist[i] != null) {
                routeList.add(gotlist[i].route.toString())
            }
            //returnでリスト返すのはできない模様
            val routeSpinner = view?.findViewById<Spinner>(R.id.routespinner) as Spinner
            //var routeList = mutableListOf<String>()

            var adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, routeList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            routeList.add(0, "keisei")
            routeSpinner.adapter = adapter
        }
    }
/*
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val registbutton = view.findViewById<Button>(R.id.registButton)
        registbutton.setOnClickListener(view.OnCLickListener() {
            override fun onClick(v: View) {
                if (mListener != null) {
                    mListener?.onClickButton()
                }
                v.findViewById<TextView>(R.id.textView2).text = "ににん"

            }
        }
        )
    }
 */
}

    // Viewが生成し終わった時に呼ばれるメソッド
    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val stationButton = view.findViewById<Button>(R.id.stationButton)
        stationButton.setOnClickListener(){ //これがMainActivity下ならちゃんと動作するのは確認済み
            val routeSpinner = view.findViewById<Spinner>(R.id.routespinner) as Spinner
            var routeList = mutableListOf<String>()

            var adapter = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_item, routeList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            routeList.add(0,"keisei")
            routeSpinner.adapter = adapter
        }
        //え、Fragment内で呼べるの？
    }
}
*/
interface MyListener {
    fun onClickButton()
}


//簡単なデータベースで取り出せるかの確認
@Entity
data class  spinnerlist(
    @PrimaryKey(autoGenerate = true)val id:Int,
    @ColumnInfo(name = "route")val route: String?
)

@Dao
interface spinnerlistDao{
    @Query("select * from spinnerlist")
    fun getAll():List<spinnerlist>

    @Query("Insert into spinnerlist (route) values ('京成線'),('新京成線')")
    fun insert()

    @Update
    fun update(route: spinnerlist)

    @Delete
    fun delete(route: spinnerlist)
}

@Database(entities = arrayOf(spinnerlist::class), version = 1,exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun spinnerlistDao(): spinnerlistDao

    companion object{
        val DB_NAME = "user.db"
        private lateinit var instance: AppDatabase

        fun getInstance(context: Context): AppDatabase {
            if (!::instance.isInitialized) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).build()
            }
        return instance
        }
    }
}

/*
//データベースのテーブル定義
@Entity
data class StationRouteUpDownDaytype(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "station") val station: String?,
    @ColumnInfo(name = "route") val route: String?,
    @ColumnInfo(name = "updown") val updown: Int,
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

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT DISTINCT * FROM StationRouteUpDownDaytype WHERE :station = station AND :route = route")
    fun getDirectionByStationRoutes(station: String,route:String): List<StationRouteUpDownDaytype>

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
