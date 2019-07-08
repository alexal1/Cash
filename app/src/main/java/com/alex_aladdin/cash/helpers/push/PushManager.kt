package com.alex_aladdin.cash.helpers.push

import android.app.*
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.alex_aladdin.cash.CashApp
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.helpers.CurrencyManager
import com.alex_aladdin.cash.repository.TransactionsRepository
import com.alex_aladdin.cash.repository.specifications.DaySpecification
import com.alex_aladdin.cash.ui.activities.SplashActivity
import com.alex_aladdin.cash.utils.subscribeOnUi
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class PushManager(private val context: Context) : KoinComponent {

    companion object {
        private const val HOUR_TO_SHOW_PUSH = 21
        private const val CHANNEL_ID = "daily_reports_push_notifications"
    }


    private val app by lazy { context.applicationContext as CashApp }
    private val alarmManager by lazy { context.getSystemService(ALARM_SERVICE) as AlarmManager }
    private val notificationManager by lazy { NotificationManagerCompat.from(context) }
    private val repository: TransactionsRepository by inject()
    private val currencyManager: CurrencyManager by inject()

    private val alarmPendingIntent by lazy {
        val intent = Intent(context, AlarmReceiver::class.java)
        PendingIntent.getBroadcast(context, 0, intent, 0)
    }

    private val pushPendingIntent by lazy {
        val intent = Intent(context, SplashActivity::class.java)
        PendingIntent.getActivity(context, 0, intent, 0)
    }

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

        val currentDate = app.currentDate.value!!
        val currentCurrencyIndex = currencyManager.getCurrentCurrencyIndex()
        repository
            .query(DaySpecification(currentDate, currentCurrencyIndex))
            .map { transactions ->
                val totalGain = transactions.filter { it.isGain() }.sumByDouble { it.getAmountPerDay() }
                val totalLoss = transactions.filter { !it.isGain() }.sumByDouble { it.getAmountPerDay() }
                totalGain to totalLoss
            }
            .subscribeOnUi { (totalGain, totalLoss) ->
                val notification: Notification

                if (totalGain > 0.0 || totalLoss > 0.0) {
                    val diff = totalGain - totalLoss
                    val title = if (diff >= 0.0) {
                        app.getString(
                            R.string.push_notification_title_positive,
                            currencyManager.formatMoney(diff, currencyManager.getCurrentCurrencyIndex())
                        )
                    } else {
                        app.getString(
                            R.string.push_notification_title_negative,
                            currencyManager.formatMoney(-diff, currencyManager.getCurrentCurrencyIndex())
                        )
                    }

                    notification = createNotification(title, R.string.push_notification_subtitle)
                } else {
                    notification = createNotification(
                        app.getString(R.string.push_notification_title_empty),
                        R.string.push_notification_subtitle
                    )
                }

                notificationManager.notify(
                    ++notificationId,
                    notification
                )
            }
    }

    fun checkIfNotificationsEnabled(): Boolean = notificationManager.areNotificationsEnabled()


    private fun createNotification(title: String, @StringRes text: Int) = createNotification(title, app.getString(text))

    private fun createNotification(
        title: String,
        text: String
    ): Notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle(title)
        .setContentText(text)
        .setSmallIcon(R.mipmap.ic_launcher)
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

}