package uz.premolux.relay

import android.os.Build
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** PremoLux serveriga SMS ma'lumotini yuboradi — bitta, qattiq belgilangan manzilga. */
object RelayApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private const val TAG = "PremoLuxRelay"

    fun forward(
        prefs: Prefs,
        sender: String,
        body: String,
        code: String?,
        onResult: (ok: Boolean, message: String) -> Unit
    ) {
        val json = JSONObject().apply {
            put("token", prefs.deviceToken)
            put("device", Build.MODEL ?: "android")
            put("sender", sender)
            put("body", body)
            put("code", code ?: JSONObject.NULL)
            put("received_at", System.currentTimeMillis())
        }

        val reqBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(Prefs.SERVER_URL)
            .post(reqBody)
            .addHeader("Authorization", "Bearer ${prefs.deviceToken}")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                Log.w(TAG, "Yuborilmadi: ${e.message}")
                onResult(false, e.message ?: "Tarmoq xatosi")
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val ok = response.isSuccessful
                response.close()
                onResult(ok, if (ok) "Yuborildi" else "Server xatosi: ${response.code}")
            }
        })
    }

    fun ping(prefs: Prefs, onResult: (ok: Boolean) -> Unit) {
        val request = Request.Builder()
            .url(Prefs.PING_URL)
            .addHeader("Authorization", "Bearer ${prefs.deviceToken}")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) { onResult(false) }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val ok = response.isSuccessful
                response.close()
                onResult(ok)
            }
        })
    }
}
