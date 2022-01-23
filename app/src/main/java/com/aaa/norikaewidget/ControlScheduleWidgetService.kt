package com.aaa.norikaewidget

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import android.content.SharedPreferences
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import java.time.format.DateTimeFormatter

class ControlScheduleWidgetService : IntentService("ControlScheduleWidgetService") {


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onHandleIntent(intent: Intent?) {
        //利用者への通知をサービス作成から５秒以内に作成（intentservriceの仕様上必須）
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val name = "通知のタイトル的情報を設定"
        val id = "norikaeWidget_foreground"
        val notifyDescription = "この通知の詳細情報を設定します"

        if (manager.getNotificationChannel(id) == null) {
            val mChannel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)
            mChannel.apply {
                description = notifyDescription
            }
            manager.createNotificationChannel(mChannel)
        }

        val notification = NotificationCompat.Builder(this,id).apply {
            //mContentTitle = "通知のタイトル"
            //mContentText = "通知の内容"
            setSmallIcon(R.drawable.ic_launcher_background)
        }.build()
/*
        Thread(
            Runnable {
                (0..5).map {
                    Thread.sleep(1000)
                }
                stopForeground(Service.STOP_FOREGROUND_DETACH)
            }
        ).start()
*/
        startForeground(1, notification)

        val context = applicationContext
        val count = 315360000
        var c = 0
        val views = RemoteViews(context.packageName, R.layout.simple_time_schedule_widget)
        val dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val appWidgetId = appWidgetManager.getAppWidgetIds(ComponentName(this, SimpleTimeScheduleWidget::class.java))
        val prefs: SharedPreferences = context.getSharedPreferences("savedata",
            AppCompatActivity.MODE_PRIVATE
        )
        // データの作成
        var stationName = prefs.getString("Widget_Station",null)?.replace("駅","") +"駅"
        var routeName = prefs.getString("Widget_Route",null)
        val updown = prefs.getString("Widget_UpDown",null)
        // Construct the RemoteViews object

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
        while(c< count) {
            c++

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
                            min.toString() + " " + "%02d".format(sec)
                            )
                            //次の１本までの時間表示
                        if( x+1 < launchTimeList.size){
                            val secondArrivalTime = now.with(LocalTime.of(launchTimeList[x+1].hour, launchTimeList[x+1].minute))
                            var mintosecond = ChronoUnit.MINUTES.between(now, secondArrivalTime)
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
                Thread.sleep(1000);
            }
    }
}