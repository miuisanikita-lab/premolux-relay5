package uz.premolux.relay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val prefs = Prefs(context)
        if (!prefs.active) return

        Telephony.Sms.Intents.getMessagesFromIntent(intent).forEach { msg ->
            val sender = msg.originatingAddress ?: return@forEach
            val body   = msg.messageBody ?: return@forEach
            if (!SmsFilter.isOtpSms(body, prefs)) return@forEach

            val code = SmsFilter.extractCode(body)
            RelayApi.forward(prefs, sender, body, code) { ok, _ ->
                if (ok) {
                    prefs.forwardedCount = prefs.forwardedCount + 1
                    prefs.lastForwardedAt = System.currentTimeMillis()
                }
            }
        }
    }
}
