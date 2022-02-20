package com.aaa.norikaewidget


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build

import androidx.recyclerview.widget.LinearLayoutManager

import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.collections.ArrayList
import kotlin.concurrent.timer



class TimeListFragment : Fragment(), MyListener {
    lateinit var mAdapter: CustomAdapter
    lateinit var mTrainList: ArrayList<DataForSchedule>
    private var mListener: MyListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_time_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //検索画面への遷移ボタンの処理
        view.findViewById<Button>(R.id.buttonToMain).setOnClickListener(object : View.OnClickListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onClick(v: View) { //ここviewじゃなくてvにしたら動いた
                if (mListener != null) {
                    mListener?.onClickButton()
                }
                val prefs: SharedPreferences = requireContext().getSharedPreferences(
                    "savedata",
                    AppCompatActivity.MODE_PRIVATE
                )
                val editor = prefs.edit()
                editor.putInt("FromPage", 0)
                editor.apply()
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
            }
        })
        //サービス用のintentを定義
        val serviceIntent = Intent(requireContext(), ControlScheduleWidgetService::class.java)

        //ウィジェット更新ボタンの処理（sharedpreferenceにデータを入れる）
        view.findViewById<Button>(R.id.buttonUpdateWidget).setOnClickListener(object : View.OnClickListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onClick(v: View) { //ここviewじゃなくてvにしたら動いた
                if (mListener != null) {
                    mListener?.onClickButton()
                }
                val prefs: SharedPreferences = requireContext().getSharedPreferences(
                    "savedata",
                    AppCompatActivity.MODE_PRIVATE
                )
                val stationName = prefs.getString("RegisteredStation",null)
                val routeName = prefs.getString("RouteSpinner",null)
                val direction = prefs.getString("UpDownSpinner",null)
                val editor = prefs.edit()
                editor.putString("Widget_Station", stationName)
                editor.putString("Widget_Route", routeName)
                editor.putString("Widget_UpDown", direction)
                editor.apply()
                Toast.makeText(requireContext(), "Widgetを更新しました", Toast.LENGTH_SHORT).show()

                // Serviceの停止
                requireContext().stopService(serviceIntent)
                // Serviceの開始
                requireContext().startForegroundService(serviceIntent)
            }
        })
    }
    override fun onClickButton() {
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        val rCont = requireContext()
        rCont.getSharedPreferences("savedata", 0)
        val prefs: SharedPreferences = rCont.getSharedPreferences("savedata",
            AppCompatActivity.MODE_PRIVATE
        )
        // データの作成
        val stationName = prefs.getString("RegisteredStation", null)?.replace("駅", "")
        val routeName = prefs.getString("RouteSpinner", null)
        val direction = prefs.getString("UpDownSpinner", null)

        try {
            //平日の時刻表を取得
            var dayTypeStr = "平日"
            val filenameWeekDay = getTimeScheduleFilename(dayTypeStr)
            var filename = filenameWeekDay

            //平日の時刻表を取得
            val stationTimeList_Weekday = loadTimeSchedule(filename)
            //時刻表をLocalDateTime型に変換して保持
            val launchTimeList_Weekday = convertTimeScheduletoLocalDateTime(stationTimeList_Weekday)

            //土・休日の時刻表を取得
            dayTypeStr = "土・休日"
            val filenameHoliday = getTimeScheduleFilename(dayTypeStr)
            filename = filenameHoliday

            //土・休日の時刻表を取得
            val stationTimeList_Holiday = loadTimeSchedule(filename)
            //時刻表をLocalDateTime型に変換して保持
            val launchTimeList_Holiday = convertTimeScheduletoLocalDateTime(stationTimeList_Holiday)

            //平日と土・休日の時刻表を別々に保存
            saveScheduleData(launchTimeList_Weekday,launchTimeList_Holiday)

            //nowの取得とformat設定
            val dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")

            timer("timer", false, period = 1000) {
                //曜日の判定
                val todayDaytype = LocalDate.now().dayOfWeek.value
                val tomorrowDaytype = LocalDate.now().plusDays(1).dayOfWeek.value

                //曜日によって格納する時刻表を判断して格納するための変数
                var todayStationTimeList :ArrayList<String> = arrayListOf()
                var tomorrowStationTimeList :ArrayList<String> = arrayListOf()

                //曜日によって格納する時刻表を判断して格納するための変数(LocalDateTime)
                var todayLaunchTimeList : MutableList<LocalDateTime> = arrayListOf()
                var tomorrowLaunchTimeList : MutableList<LocalDateTime> = arrayListOf()

                if(todayDaytype == 6 or 7){
                    //"土・休日"
                    todayStationTimeList = stationTimeList_Holiday
                    todayLaunchTimeList = launchTimeList_Holiday
                }else{
                    //"平日"
                    todayStationTimeList = stationTimeList_Weekday
                    todayLaunchTimeList = launchTimeList_Weekday
                }

                if(tomorrowDaytype == 6 or 7){
                    //"土・休日"
                    tomorrowStationTimeList = stationTimeList_Holiday
                    tomorrowLaunchTimeList = launchTimeList_Holiday
                }else{
                    //"平日"
                    tomorrowStationTimeList = stationTimeList_Weekday
                    tomorrowLaunchTimeList = launchTimeList_Weekday
                }

                //描画用の時刻表リスト
                mTrainList = arrayListOf()

                //現在時刻を取得し、フォーマット
                val now = LocalDateTime.now()
                now.format(dtf)

                var y = 0
                fun addToTrainList(stationTimeList:ArrayList<String>,launchTimeList: MutableList<LocalDateTime>,todayFlag:Int){
                    var arrivalLocalDateTime: LocalDateTime = now
                    var x = 0

                    while (x < launchTimeList.size) {
                        var sp = stationTimeList[x].split(",")
                        arrivalLocalDateTime =
                            now.with(LocalTime.of(launchTimeList[x].hour, launchTimeList[x].minute))
                        var min = ChronoUnit.MINUTES.between(now, arrivalLocalDateTime) % (24 * 60)
                        val sec = ChronoUnit.SECONDS.between(now,
                            arrivalLocalDateTime) % (24 * 60 * 60) - min * 60
                        if (min >= 0 || todayFlag == 0) {
                            if(min < 0){
                                min += 60 * 24  //次の日はマイナスになるため1日の分数を加算
                            }
                            if (sec >= 0 || todayFlag == 0) {
                                var schedulecard = DataForSchedule(routeName.toString(),
                                    direction.toString(),
                                    ("%02d".format(sp[0].toInt())) + ":" + ("%02d".format(sp[1].toInt())).toString(),
                                    min.toString() + ":" + "%02d".format(sec))
                                if (min < 10) {
                                    schedulecard = DataForSchedule(routeName.toString(),
                                        direction.toString(),
                                        ("%02d".format(sp[0].toInt())) + ":" + ("%02d".format(sp[1].toInt())).toString(),
                                        "  " + min.toString() + ":" + "%02d".format(sec))
                                }
                                mTrainList.add(y, schedulecard)
                                y++
                            }
                        }
                        x++
                    }
                }
                var todayFlag = 1
                addToTrainList(todayStationTimeList,todayLaunchTimeList,todayFlag)
                todayFlag = 0
                addToTrainList(tomorrowStationTimeList,tomorrowLaunchTimeList,todayFlag)

                activity?.runOnUiThread(Runnable {
                    // RecyclerViewの取得
                    val recyclerView =
                        view?.findViewById<RecyclerView>(R.id.recyclerView) as RecyclerView

                    // LayoutManagerの設定
                    recyclerView?.layoutManager = LinearLayoutManager(requireContext())

                    // CustomAdapterの生成と設定
                    mAdapter = CustomAdapter(mTrainList)
                    recyclerView?.adapter = mAdapter
                })
            }
        } catch (e: Exception) {
            Toast.makeText(rCont, "処理中です", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }
    }

    fun getTimeScheduleFilename(dayTypeStr:String): String {
        var filename: String = ""
        val rCont = requireContext()
        rCont.getSharedPreferences("savedata", 0)
        val prefs: SharedPreferences = rCont.getSharedPreferences("savedata",
            AppCompatActivity.MODE_PRIVATE
        )
        // データの作成
        val stationName = prefs.getString("RegisteredStation", null)?.replace("駅", "")
        val routeName = prefs.getString("RouteSpinner", null)
        val direction = prefs.getString("UpDownSpinner", null)

        //時刻表ファイルの呼び出し
        viewLifecycleOwner.lifecycleScope.launch {
            // ここからはIOスレッドで実行してもらう
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(requireContext())
                var x = 0
                //x = db.StationRouteUpDownDaytypeDao().getErrorCodeByInfo(stationName.toString(),routeName,direction.toString(),dayTypeStr)
                //if(x != 1){
                    filename = db.StationRouteUpDownDaytypeDao().getCsvnameByInfo(stationName.toString(),routeName,direction.toString(),dayTypeStr)
                //}
            }
        }
        //filenameが入るまで3秒までは待つ様に変更
        var x = 0
        while (filename == "") {
            Thread.sleep(200)  // wait for 0.5 second
            x++
            if ((x % 4) == 0) {
                Toast.makeText(rCont, "処理をお待ちください。", Toast.LENGTH_SHORT).show()
            }
            if (x > 6) {
                break
            }
        }
        return filename
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadTimeSchedule(filename:String):ArrayList<String>{
        val fileInputStream = resources.assets.open(filename)
        val reader = BufferedReader(InputStreamReader(fileInputStream, "UTF-8"))
        reader.readLine()
        var lineBuffer: String
        var stationTimeList: ArrayList<String> = arrayListOf()
        var k = 0
        var temp: String? = ""
        while (temp != null) {
            lineBuffer = temp
            if (lineBuffer != null) {
                if (temp != "") {
                    stationTimeList.add(lineBuffer)
                }
                temp = reader.readLine()
                k++
            } else {
                break
            }
        }
        return stationTimeList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertTimeScheduletoLocalDateTime(stationTimeList:ArrayList<String>):MutableList<LocalDateTime>{
        var i: Int = 0
        val launchTimeList: MutableList<LocalDateTime> = arrayListOf()
        while (i < stationTimeList.size) {
            var sp = stationTimeList[i].split(",")
            if (sp[1] != "") {
                launchTimeList.add(i,
                    LocalDateTime.now().with(LocalTime.of(sp[0].toInt(), sp[1].toInt())))
            }
            i++
        }
        return launchTimeList
    }

    fun saveScheduleData(launchTimeList_Weekday:MutableList<LocalDateTime>,
                         launchTimeList_Holiday:MutableList<LocalDateTime>){
        //選択された時刻表をデバイスに保存する。
        val rCont = requireContext()
        rCont.getSharedPreferences("savedata", 0)
        val prefs: SharedPreferences = rCont.getSharedPreferences("savedata",
            AppCompatActivity.MODE_PRIVATE
        )
        val editor = prefs.edit()
        val scheduleSetWeekday: MutableSet<String> = mutableSetOf()

        var c = 0
        while (c < launchTimeList_Weekday.size) {
            scheduleSetWeekday.add(launchTimeList_Weekday.toString())
            c++
        }
        editor.putStringSet("SelectedTimeSchedule_Weekday", scheduleSetWeekday)

        var d = 0
        val scheduleSetHoliday: MutableSet<String> = mutableSetOf()
        while (d < launchTimeList_Holiday.size) {
            scheduleSetHoliday.add(launchTimeList_Holiday.toString())
            d++
        }
        editor.putStringSet("SelectedTimeSchedule_Holiday", scheduleSetHoliday)

        editor.apply()
    }
}

data class DataForSchedule(
    val route: String,
    val updown: String,
    val arrivalTime: String, //とりあえずString Dateにするかも
    val leastTime: String,
)

class CustomAdapter(private val trainList: ArrayList<DataForSchedule>): RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
    // Viewの初期化
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val route: TextView
        val updown: TextView
        val arrivalTime: TextView
        val leastTime: TextView

        init {
            route = v.findViewById(R.id.textView5)
            updown = v.findViewById(R.id.textView7)
            arrivalTime = v.findViewById(R.id.textView9)
            leastTime = v.findViewById(R.id.textView3)
        }
    }
    // レイアウトの設定
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.timelistcard, viewGroup, false)
        return ViewHolder(v)
    }

    // Viewの設定
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val trainTime = trainList[position]

        viewHolder.route.text = trainTime.route
        viewHolder.updown.text = trainTime.updown
        viewHolder.arrivalTime.text = trainTime.arrivalTime
        viewHolder.leastTime.text = trainTime.leastTime
    }

    // 表示数を返す
    override fun getItemCount() = trainList.size
}