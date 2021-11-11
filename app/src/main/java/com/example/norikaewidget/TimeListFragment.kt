package com.example.norikaewidget

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.ContactsContract

import androidx.recyclerview.widget.LinearLayoutManager

import android.util.AttributeSet
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timer


class TimeListFragment : Fragment() {
    lateinit var mAdapter: CustomAdapter
    lateinit var mTrainList: ArrayList<DataForSchedule>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_time_schedule, container, false)
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
        val stationName = prefs.getString("RegisteredStation",null)
        val routeName = prefs.getString("RouteSpinner",null)
        val direction = prefs.getString("UpDownSpinner",null)
        //曜日判定
        val daytype = LocalDate.now().dayOfWeek.value
        val filename:String
        if(daytype == 6 or 7){
            filename = stationName +"_"+ routeName +"_"+ direction +"_H"+ ".csv"
        }else{
            filename = stationName +"_"+ routeName +"_"+ direction + ".csv"
        }

        val fileInputStream  = resources.assets.open(filename)
        val reader = BufferedReader(InputStreamReader(fileInputStream, "UTF-8"))
        var lineBuffer: String
        var stationTimeList : ArrayList<String> = arrayListOf()
        var k = 0
        var temp :String? = ""
        while (temp != null) {
            lineBuffer = temp
            if (lineBuffer != null) {
                if (temp != ""){
                    stationTimeList.add(lineBuffer)
                }
                temp = reader.readLine()
                k++
            } else {
                break
            }
        }
        //nowの取得とformat設定
        val dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")


        mTrainList = arrayListOf()
        var i: Int = 0
        val launchTimeList:MutableList<LocalDateTime> = arrayListOf()
        while(i < stationTimeList.size){
            var sp = stationTimeList[i].split(",")
            //var schedulecard = DataForSchedule(routeName.toString(),direction.toString(),sp[0]+":"+sp[1],"00:00")
            //ここに入れる
            //mTrainList.add(i,schedulecard)
            launchTimeList.add(i,LocalDateTime.now().with(LocalTime.of(sp[0].toInt(),sp[1].toInt())))
            i++
        }

        kotlin.concurrent.timer("timer",false, period = 1000){
            val now = LocalDateTime.now()
            now.format(dtf)
            var arrivalLocalDateTime:LocalDateTime = now
            var x = 0
            var y = 0
            while(x < launchTimeList.size){
                var leastTime:LocalDateTime = LocalDateTime.now()
                var sp = stationTimeList[x].split(",")
                arrivalLocalDateTime =now.with(LocalTime.of(launchTimeList[x].hour,launchTimeList[x].minute))
                val min = ChronoUnit.MINUTES.between(now,arrivalLocalDateTime)
                val sec = ChronoUnit.SECONDS.between(now,arrivalLocalDateTime) - min * 60
                if(min >= 0){
                    if(sec >= 0){
                        var schedulecard = DataForSchedule(routeName.toString(),direction.toString(),sp[0]+":"+sp[1],min.toString()+":"+sec.toString())
                        mTrainList.add(y,schedulecard)
                        y++
                    }
                }
                x++

            }

            activity?.runOnUiThread(java.lang.Runnable {
                // RecyclerViewの取得
                val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerView) as RecyclerView

                // LayoutManagerの設定
                recyclerView?.layoutManager = LinearLayoutManager(requireContext())

                // CustomAdapterの生成と設定
                mAdapter = CustomAdapter(mTrainList)
                recyclerView?.adapter = mAdapter
            })
        }
    }
}

data class DataForSchedule(
    val route :String,
    val updown :String,
    val arrivalTime :String, //とりあえずString Dateにするかも
    val leastTime :String
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