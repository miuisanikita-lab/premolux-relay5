package uz.premolux.relay

object SmsFilter {

    // Bank nomi tekshirilmaydi — faqat matnda OTP/pul kalit so'z bor-yo'qligini tekshiradi
    fun isOtpSms(body: String?, prefs: Prefs): Boolean {
        if (body.isNullOrBlank()) return false
        val bodyLower = body.lowercase()
        return prefs.keywordArray().any { bodyLower.contains(it) }
    }

    // 4-8 xonali raqamni (OTP kodini) matndan ajratib oladi
    fun extractCode(body: String): String? =
        Regex("""\b\d{4,8}\b""").find(body)?.value
}
