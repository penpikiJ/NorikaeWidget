package com.example.norikaewidget
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.*

class AppWidgetAlarm(context: Context) {
    private val ALARM_ID = 0
    private val INTERVAL_MILLIS = 500
    private val mContext: Context
    fun startAlarm() {
        val calendar: Calendar = Calendar.getInstance()
        calendar.add(Calendar.MILLISECOND, INTERVAL_MILLIS)
        val alarmIntent = Intent(mContext, TimeScheduleWidget::class.java)
        alarmIntent.action = TimeScheduleWidget.ACTION_AUTO_UPDATE
        val pendingIntent = PendingIntent.getBroadcast(
            mContext,
            ALARM_ID,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )
        val alarmManager = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // RTC does not wake the device up
        alarmManager.setInexactRepeating(
            AlarmManager.RTC,
            calendar.getTimeInMillis(),
            INTERVAL_MILLIS.toLong(),
            pendingIntent
        )
    }

    fun stopAlarm() {
        val alarmIntent = Intent(TimeScheduleWidget.ACTION_AUTO_UPDATE)
        val pendingIntent = PendingIntent.getBroadcast(
            mContext,
            ALARM_ID,
            alarmIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )
        val alarmManager = mContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    init {
        mContext = context
    }
}