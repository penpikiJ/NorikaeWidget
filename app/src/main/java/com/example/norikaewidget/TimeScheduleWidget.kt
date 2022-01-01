package com.example.norikaewidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.concurrent.timer

/**
 * Implementation of App Widget functionality.
 */
class TimeScheduleWidget : AppWidgetProvider() {

    companion object{
        const val ACTION_AUTO_UPDATE= "AUTO_UPDATE"
        const val ACTION_BUTTON_PUSHED = "BUTTON_PUSHED"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if(intent.action.equals(ACTION_AUTO_UPDATE)){
            val thisAppWidgetComponetName = ComponentName(context.packageName,javaClass.name)
            val appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(thisAppWidgetComponetName)
            if (appWidgetIds != null && appWidgetIds.size > 0) {
                onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds)
            }
        }
        //Button押したときの処理
        if(intent.action.equals(ACTION_BUTTON_PUSHED)){
            val thisAppWidgetComponetName = ComponentName(context.packageName,javaClass.name)
            val appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(thisAppWidgetComponetName)
            if (appWidgetIds != null && appWidgetIds.size > 0) {
                onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds)
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
        val appWidgetAlarm = AppWidgetAlarm(context.applicationContext)
        appWidgetAlarm.startAlarm()
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisAppWidgetComponetName = ComponentName(context.packageName,javaClass.name)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidgetComponetName)
        if (appWidgetIds.isEmpty()){
            //stop Alarm
            val appWidgetAlarm = AppWidgetAlarm(context.applicationContext)
            appWidgetAlarm.stopAlarm()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val prefs: SharedPreferences = context.getSharedPreferences("savedata",
        AppCompatActivity.MODE_PRIVATE
    )


    // 代入するデータの作成
    var stationName = prefs.getString("Widget_Station",null)?.replace("駅","") +"駅"
    var routeName = prefs.getString("Widget_Route",null)
    val updown = prefs.getString("Widget_UpDown",null)

    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.time_schedule_widget)

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
    val dtf2 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    val scheduleSet = prefs.getStringSet("SelectedTimeSchedule",null)
    var a = 0
    val scheduleList = scheduleSet?.toMutableList()
    val sl = scheduleList?.get(a)?.split(",")
    val launchTimeList:MutableList<LocalDateTime> = arrayListOf()
    var i = 0
    if (scheduleSet != null) {
        while(i < sl!!.size){
            val sList = LocalDateTime.parse(sl?.get(i)?.trim().
            replace("[","")?.replace("]","")+":00")
            sList.format(dtf)
            launchTimeList.add(i,sList)
            i++
        }
    }
    timer("timer",false, period = 1000) {
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
                if (sec >= 0) {
                    //Widgetに時刻を設定
                    views.setTextViewText(
                        R.id.appwidget_text_departure,
                        "%02d".format(launchTimeList[x].hour) + ":" + "%02d".format(launchTimeList[x].minute)
                    )
                    views.setTextViewText(
                        R.id.appwidget_text_rest,
                        min.toString() + ":" + "%02d".format(sec)
                    )
                    //次の１本までの時間表示
                    if( x+1 < launchTimeList.size){
                        val secondArrivalTime = now.with(LocalTime.of(launchTimeList[x+1].hour, launchTimeList[x+1].minute))
                        var mintosecond = ChronoUnit.MINUTES.between(now, secondArrivalTime)
                        var sectosecond = ChronoUnit.SECONDS.between(now, secondArrivalTime) - mintosecond * 60
                        views.setTextViewText(
                            R.id.rest_to_second,
                            mintosecond.toString() + ":" + "%02d".format(sectosecond)
                        )
                    }
                    //次の次までの時間表示
                    if( x+2 < launchTimeList.size){
                        val thirdArrivalTime = now.with(LocalTime.of(launchTimeList[x+2].hour, launchTimeList[x+2].minute))
                        var mintothird = ChronoUnit.MINUTES.between(now, thirdArrivalTime)
                        var sectothird = ChronoUnit.SECONDS.between(now, thirdArrivalTime) - mintothird * 60
                        views.setTextViewText(
                            R.id.rest_to_third,
                            mintothird.toString() + ":" + "%02d".format(sectothird)
                        )
                    }

                    break
                }
            }
            x++
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}