package com.aaa.norikaewidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aaa.norikaewidget.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.concurrent.timer

/**
 * Implementation of App Widget functionality.
 */
class SimpleTimeScheduleWidget : AppWidgetProvider() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget2(context, appWidgetManager, appWidgetId)
        }
/*
        //Serviceを呼び出し
        val intent = Intent(context, ControlScheduleWidgetService::class.java)
        intent.putExtra("REQUEST_CODE", 1)
        context.stopService(intent)
        // Serviceの開始
        context.startForegroundService(intent)

 */


    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

@RequiresApi(Build.VERSION_CODES.O)
internal fun updateAppWidget2(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val prefs: SharedPreferences = context.getSharedPreferences("savedata",
        AppCompatActivity.MODE_PRIVATE
    )
    // データの作成

    var stationName = prefs.getString("Widget_Station",null)?.replace("駅","") +"駅"
    var routeName = prefs.getString("Widget_Route",null)
    val updown = prefs.getString("Widget_UpDown",null)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.simple_time_schedule_widget)
    //駅名・路線が５文字以上の場合は…で代替できるように変更
    if(stationName.length > 5){
        stationName = stationName.substring(0,5) + "…"
    }
    if(routeName?.length!! > 5){
        routeName = routeName.substring(0,5) + "…"
    }
    views.setTextViewText(R.id.appWidget_text_stationName, stationName)
    views.setTextViewText(R.id.appwidget_text_route, routeName)
    views.setTextViewText(R.id.appwidget_text_updown,updown)

    val dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
    val scheduleSet_weekday = prefs.getStringSet("SelectedTimeSchedule_Weekday",null)
    val scheduleSet_holiday = prefs.getStringSet("SelectedTimeSchedule_Holiday",null)
    var a = 0
    val scheduleList_weekday = scheduleSet_weekday?.toMutableList()
    val scheduleList_holiday = scheduleSet_holiday?.toMutableList()
    val sl_weekday = scheduleList_weekday?.get(a)?.split(",")
    val sl_holiday = scheduleList_holiday?.get(a)?.split(",")

    val launchTimeList_weekday:MutableList<LocalDateTime> = arrayListOf()
    val launchTimeList_holiday:MutableList<LocalDateTime> = arrayListOf()
    //平日・休日のリストを作成
    var i = 0
    if (scheduleSet_weekday != null) {
        while(i < sl_weekday!!.size){
            val sList = LocalDateTime.parse(sl_weekday?.get(i)?.trim().
            replace("[","")?.replace("]","")+":00")
            sList.format(dtf)
            launchTimeList_weekday.add(i,sList)
            i++
        }
    }
    i = 0
    if (scheduleSet_holiday != null) {
        while(i < sl_holiday!!.size){
            val sList = LocalDateTime.parse(sl_holiday?.get(i)?.trim().
            replace("[","")?.replace("]","")+":00")
            sList.format(dtf)
            launchTimeList_holiday.add(i,sList)
            i++
        }
    }
    //曜日の判定
    val todayDaytype = LocalDate.now().dayOfWeek.value
    val tomorrowDaytype = LocalDate.now().plusDays(1).dayOfWeek.value

    //曜日によって格納する時刻表を判断して格納するための変数(LocalDateTime)
    var todayLaunchTimeList : MutableList<LocalDateTime> = arrayListOf()
    var tomorrowLaunchTimeList : MutableList<LocalDateTime> = arrayListOf()

    if(todayDaytype == 6 or 7){
        //"土・休日"
        todayLaunchTimeList = launchTimeList_holiday
    }else{
        //"平日"
        todayLaunchTimeList = launchTimeList_weekday
    }

    if(tomorrowDaytype == 6 or 7){
        //"土・休日"
        tomorrowLaunchTimeList = launchTimeList_holiday
    }else{
        //"平日"
        tomorrowLaunchTimeList = launchTimeList_weekday
    }
    //今日と明日の時刻表を連結
    val launchTimeList:MutableList<LocalDateTime> = arrayListOf()
    i = 0
    while(i < todayLaunchTimeList.size){
        launchTimeList.add(i,todayLaunchTimeList[i])
        i++
    }
    while(i < tomorrowLaunchTimeList.size){
        launchTimeList.add(i,tomorrowLaunchTimeList[i])
        i++
    }

    //timer("timer",false, period = 1000) {
        val now = LocalDateTime.now()
        now.format(dtf)
        var arrivalLocalDateTime: LocalDateTime = now
        var x = 0
        while (x < launchTimeList.size) {
            arrivalLocalDateTime =
                now.with(LocalTime.of(launchTimeList[x].hour, launchTimeList[x].minute))
            var min = ChronoUnit.MINUTES.between(now, arrivalLocalDateTime)
            var sec = ChronoUnit.SECONDS.between(now, arrivalLocalDateTime) - min * 60
            if (min >= 0) {
                if(x >= todayLaunchTimeList.size){
                    min += 60 * 24
                }
                if (sec >= 0) {
                    //Widgetに時刻を設定
                    views.setTextViewText(
                        R.id.appwidget_text_departure,
                        "%02d".format(launchTimeList[x].hour) + ":" + "%02d".format(launchTimeList[x].minute)
                    )
                    views.setTextViewText(
                        R.id.appwidget_text_rest,
                        min.toString() + " " + "%02d".format(sec)
                    )
                    //次の１本までの時間表示
                    if( x+1 < launchTimeList.size){
                        val secondArrivalTime = now.with(LocalTime.of(launchTimeList[x+1].hour, launchTimeList[x+1].minute))
                        var mintosecond = ChronoUnit.MINUTES.between(now, secondArrivalTime)
                        if(x >= todayLaunchTimeList.size){
                            mintosecond += 60 * 24
                        }
                        var sectosecond = ChronoUnit.SECONDS.between(now, secondArrivalTime) - mintosecond * 60
                        views.setTextViewText(
                            R.id.rest_to_second,
                            mintosecond.toString() + " " + "%02d".format(sectosecond)
                        )
                    }
                    //次の次までの時間表示
                    if( x+2 < launchTimeList.size){
                        val thirdArrivalTime = now.with(LocalTime.of(launchTimeList[x+2].hour, launchTimeList[x+2].minute))
                        var mintothird = ChronoUnit.MINUTES.between(now, thirdArrivalTime)
                        if(x >= todayLaunchTimeList.size){
                            mintothird += 60 * 24
                        }
                        var sectothird = ChronoUnit.SECONDS.between(now, thirdArrivalTime) - mintothird * 60
                        views.setTextViewText(
                            R.id.rest_to_third,
                            mintothird.toString() + " " + "%02d".format(sectothird)
                        )
                    }
                    break
                }
            }
            x++
        }
    appWidgetManager.updateAppWidget(appWidgetId, views)
}