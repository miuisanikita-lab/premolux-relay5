package uz.premolux.relay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val prefs = Prefs(context)
        if (!prefs.active) return

        val svc = Intent(context, RelayForegroundService::class.java)
        ContextCompat.startForegroundService(context, svc)
    }
}
