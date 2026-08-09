package uz.premolux.relay

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * Zamonaviy Android versiyalari fon jarayonlarini tez o'chirib
 * qo'yadi. Shu xizmat "men ishlayapman" degan doimiy bildirishnoma
 * ko'rsatib, tizimga ilovani o'chirmaslikni bildiradi.
 *
 * Bildirishnoma shaffof va halol — nima qilayotgani ochiq yozilgan.
 */
class RelayForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, buildNotification())
        return START_STICKY
    }

    private fun buildNotification(): android.app.Notification {
        val channelId = "premolux_relay_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "PremoLux SMS ulagich",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Bank SMS larini kuzatish xizmati ishlab turibdi"
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("PremoLux SMS ulagich")
            .setContentText("Bank xabarlarini kuzatmoqda")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    companion object {
        private const val NOTIF_ID = 4201
    }
}
