/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.helpers.push

import android.app.*
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.madewithlove.daybalance.CashApp
import com.madewithlove.daybalance.R
import com.madewithlove.daybalance.dto.Money
import com.madewithlove.daybalance.features.history.HistoryActivity
import com.madewithlove.daybalance.helpers.DatesManager
import com.madewithlove.daybalance.model.BalanceLogic
import com.madewithlove.daybalance.utils.*
import java.util.*

class PushManager(
    private val context: Context,
    private val balanceLogic: BalanceLogic,
    private val datesManager: DatesManager
) {

    companion object {

        const val OPENED_BY_PUSH = "opened_by_push"

        private const val HOUR_TO_SHOW_PUSH = 22
        private const val CHANNEL_ID = "daily_reports_push_notifications"

    }


    private val app by lazy { context.applicationContext as CashApp }
    private val alarmManager by lazy { context.getSystemService(ALARM_SERVICE) as AlarmManager }
    private val notificationManager by lazy { NotificationManagerCompat.from(context) }

    private val alarmPendingIntent by lazy {
        val intent = Intent(context, AlarmReceiver::class.java)
        PendingIntent.getBroadcast(context, 0, intent, 0)
    }

    private val pushPendingIntent by lazy {
        val intent = Intent(context, HistoryActivity::class.java).apply {
            putExtra(OPENED_BY_PUSH, true)
        }
        PendingIntent.getActivity(context, 0, intent, 0)
    }

    private val calendar = CalendarFactory.getInstance()
    private val dc = DisposableCache()

    private var notificationId = 0


    init {
        createNotificationChannel()
    }


    fun schedulePushNotifications() {
        val calendar = GregorianCalendar.getInstance().apply {
            if (get(Calendar.HOUR_OF_DAY) >= HOUR_TO_SHOW_PUSH) {
                add(Calendar.DAY_OF_MONTH, 1)
            }

            set(Calendar.HOUR_OF_DAY, HOUR_TO_SHOW_PUSH)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            alarmPendingIntent
        )
    }

    fun cancelPushNotifications() {
        alarmManager.cancel(alarmPendingIntent)
    }

    fun showPushNotification(forced: Boolean = false) {
        if (app.isInForeground && !forced) {
            return
        }

        balanceLogic.balanceObservable
            .take(1)
            .map { balance ->
                val dayLimit = balance.dayLimit ?: throw IllegalArgumentException("dayLimit is null in the balance for push notification")
                Money.by(dayLimit.amount - balance.dayLoss.amount)
            }
            .subscribeOnUi { diff ->
                val diffString = TextFormatter.formatMoney(Money.by(diff.amount.abs()))
                val title: String
                val text: String

                if (diff.amount.signum() >= 0) {
                    title = context.getString(R.string.push_notification_title_positive, diffString)
                    text = if (datesManager.currentDate.isLastMonthDay()) {
                        context.getString(R.string.push_notification_text_positive_last_day)
                    } else {
                        context.getString(R.string.push_notification_text_positive)
                    }
                } else {
                    title = context.getString(R.string.push_notification_title_negative, diffString)
                    text = if (datesManager.currentDate.isLastMonthDay()) {
                        context.getString(R.string.push_notification_text_negative_last_day)
                    } else {
                        context.getString(R.string.push_notification_text_negative)
                    }
                }

                val notification = createNotification(title, text)
                notificationManager.notify(++notificationId, notification)
            }
            .cache(dc)
    }

    fun areNotificationsEnabled(): Boolean {
        if (!notificationManager.areNotificationsEnabled()) {
            return false
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return true
        }

        val channel = notificationManager.getNotificationChannel(CHANNEL_ID) ?: return true
        return channel.importance != NotificationManager.IMPORTANCE_NONE
    }

    fun dispose() {
        dc.drain()
    }


    private fun createNotification(
        title: String,
        text: String
    ): Notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle(title)
        .setStyle(NotificationCompat.BigTextStyle().bigText(text))
        .setSmallIcon(R.drawable.ic_notification)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setContentIntent(pushPendingIntent)
        .setAutoCancel(true)
        .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.push_notifications_channel_name)
            val descriptionText = context.getString(R.string.push_notifications_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = descriptionText
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun Date.isLastMonthDay(): Boolean {
        calendar.time = this

        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        return currentDay == maxDay
    }

}