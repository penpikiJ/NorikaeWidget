package com.example.norikaewidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.concurrent.timer

/**
 * Implementation of App Widget functionality.
 */
class TimeScheduleWidget : AppWidgetProvider() {
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
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
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
    // データの作成

    val stationName = prefs.getString("RegisteredStation",null) +"駅"
    val routeName = prefs.getString("RouteSpinner",null)
    val updown = prefs.getString("UpDownSpinner",null)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.time_schedule_widget)

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