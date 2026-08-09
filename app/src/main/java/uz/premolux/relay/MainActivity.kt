package uz.premolux.relay

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import uz.premolux.relay.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private lateinit var prefs: Prefs
    private var pulseAnim: AnimatorSet? = null

    private val permLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted[Manifest.permission.RECEIVE_SMS] == true) enableRelay()
        renderPermBadge()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        prefs = Prefs(this)

        renderStatus(animate = false)
        renderPermBadge()

        b.powerBtn.setOnClickListener {
            if (prefs.active) disableRelay() else requestSmsPermission()
        }
    }

    override fun onResume() {
        super.onResume()
        renderPermBadge()
        renderStatus(animate = false)
    }

    private fun requestSmsPermission() {
        val needed = mutableListOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            needed += Manifest.permission.POST_NOTIFICATIONS
        }
        val notGranted = needed.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isEmpty()) enableRelay() else permLauncher.launch(notGranted.toTypedArray())
    }

    private fun enableRelay() {
        prefs.active = true
        ContextCompat.startForegroundService(this, Intent(this, RelayForegroundService::class.java))
        renderStatus(animate = true)
        renderPermBadge()
    }

    private fun disableRelay() {
        prefs.active = false
        stopService(Intent(this, RelayForegroundService::class.java))
        renderStatus(animate = true)
    }

    // ── vizual holat: tugma, halqa, matn, pulsatsiya ──
    private fun renderStatus(animate: Boolean) {
        val active = prefs.active

        b.powerBtn.setBackgroundResource(if (active) R.drawable.ring_on else R.drawable.ring_off)
        b.powerIcon.alpha = if (active) 1f else 0.5f
        b.statusDot.setBackgroundResource(if (active) R.drawable.dot_active else R.drawable.dot_idle)

        if (active) {
            val last = if (prefs.lastForwardedAt > 0)
                DateUtils.getRelativeTimeSpanString(prefs.lastForwardedAt).toString() else "hali yo'q"
            b.status.text = "Faol"
            b.hint.text = "Fonda ishlamoqda · bosib o'chirish"
            b.lastValue.text = last
            startPulse()
        } else {
            b.status.text = "Nofaol"
            b.hint.text = "Yoqish uchun bosing"
            b.lastValue.text = "—"
            stopPulse()
        }

        if (animate) {
            b.powerBtn.animate().scaleX(0.9f).scaleY(0.9f).setDuration(90)
                .withEndAction { b.powerBtn.animate().scaleX(1f).scaleY(1f).setDuration(180)
                    .setInterpolator(DecelerateInterpolator(2f)).start() }.start()
            animateCount(prefs.forwardedCount)
        } else {
            b.countValue.text = prefs.forwardedCount.toString()
        }
    }

    private fun animateCount(target: Int) {
        val current = b.countValue.text.toString().toIntOrNull() ?: 0
        if (current == target) { b.countValue.text = target.toString(); return }
        ValueAnimator.ofInt(current, target).apply {
            duration = 420
            addUpdateListener { b.countValue.text = (it.animatedValue as Int).toString() }
            start()
        }
    }

    // ── ikkita halqa navbat bilan kattalashib, so'nib boradi ──
    private fun startPulse() {
        if (pulseAnim?.isRunning == true) return
        pulseAnim = AnimatorSet().apply {
            val ring1 = pulseSet(b.pulse1, 0L)
            val ring2 = pulseSet(b.pulse2, 900L)
            playTogether(ring1, ring2)
            start()
        }
    }

    private fun pulseSet(view: android.view.View, delay: Long): Animator {
        view.scaleX = 1f; view.scaleY = 1f; view.alpha = 0f
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.35f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.35f)
        val alpha = ObjectAnimator.ofFloat(view, "alpha", 0.55f, 0f)
        return AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 1800
            startDelay = delay
            interpolator = DecelerateInterpolator()
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(a: Animator) {
                    if (prefs.active) start() else view.alpha = 0f
                }
            })
        }
    }

    private fun stopPulse() {
        pulseAnim?.childAnimations?.forEach { (it as? AnimatorSet)?.cancel() }
        pulseAnim?.cancel()
        b.pulse1.alpha = 0f
        b.pulse2.alpha = 0f
    }

    private fun renderPermBadge() {
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED

        b.permIcon.setImageResource(if (granted) R.drawable.ic_check else R.drawable.ic_lock)
        b.permStatus.text = if (granted) "SMS ruxsati berilgan" else "SMS ruxsati kerak"
    }
}
