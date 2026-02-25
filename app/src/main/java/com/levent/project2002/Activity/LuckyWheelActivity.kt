package com.levent.project2002.Activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.levent.project2002.Helper.ManagmentDiscount
import com.levent.project2002.databinding.ActivityLuckyWheelBinding
import java.util.Random
import android.content.SharedPreferences
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit // Zaman hesaplamaları için eklendi

class LuckyWheelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLuckyWheelBinding
    private val managmentDiscount by lazy { ManagmentDiscount(this) }
    private val auth = FirebaseAuth.getInstance()

    private val wheelSections = listOf(100, 75, 50, 30, 1000, 500, 250, 150)
    private val random = Random()

    private val PREF_NAME = "LuckyWheelPrefs"
    private val KEY_SPIN_COUNT_PREFIX = "spinCount_"
    private val KEY_LAST_RESET_TIME_PREFIX = "lastResetTime_" // 🔥 Yeni: Son sıfırlama zamanı
    private val MAX_SPINS = 2 // Maksimum çevirme hakkı 2

    // 10 dakika (Milisaniye cinsinden)
    private val RESET_INTERVAL_MS = TimeUnit.MINUTES.toMillis(10)

    private lateinit var prefs: SharedPreferences
    private var currentSpinCount: Int = 0

    private val FULL_ROTATION = 360f
    private var winningAmount = 0
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLuckyWheelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        userId = auth.currentUser?.uid

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Kazandıran Çark"

        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        // 🔥 Aktivite her açıldığında/geri dönüldüğünde hakkı kontrol et
        checkSpinAvailability()
    }

    private fun checkSpinAvailability() {
        if (userId == null) {
            Toast.makeText(this, "Çarkı çevirmek için giriş yapmalısınız.", Toast.LENGTH_LONG).show()
            showLimitMessageAndDisableButton(true, "Giriş yapmanız gerekiyor.")
            return
        }

        // Kullanıcının verilerini al
        currentSpinCount = prefs.getInt(KEY_SPIN_COUNT_PREFIX + userId, 0)
        val lastResetTime = prefs.getLong(KEY_LAST_RESET_TIME_PREFIX + userId, 0L)
        val currentTime = System.currentTimeMillis()

        // 🔥 1. SIFIRLAMA KONTROLÜ: 10 dakika geçti mi?
        if (currentTime - lastResetTime >= RESET_INTERVAL_MS) {
            // Eğer süre dolmuşsa, hakkı sıfırla ve zamanı güncelle
            currentSpinCount = 0
            prefs.edit()
                .putInt(KEY_SPIN_COUNT_PREFIX + userId, 0)
                .putLong(KEY_LAST_RESET_TIME_PREFIX + userId, currentTime)
                .apply()
        }

        // 🔥 2. HAK KONTROLÜ: Mevcut hak bitti mi?
        if (currentSpinCount >= MAX_SPINS) {

            // Kalan süreyi hesapla
            val nextResetTime = lastResetTime + RESET_INTERVAL_MS
            val remainingTimeMs = nextResetTime - currentTime

            val remainingMinutes = remainingTimeMs / 60000
            val remainingSeconds = (remainingTimeMs % 60000) / 1000

            val timeMessage = if (remainingTimeMs > 0) {
                String.format("%02d dakika %02d saniye", remainingMinutes, remainingSeconds)
            } else {
                // Teorik olarak bu noktaya gelmemeli, süre dolduysa sıfırlanmalıydı.
                "Hemen şimdi"
            }

            showLimitMessageAndDisableButton(true, "Çevirme hakkınız bitti. Yeni hak $timeMessage sonra.")
        } else {
            // Hak varsa butonu aktif et ve kalan hakkı göster
            val remainingSpins = MAX_SPINS - currentSpinCount
            showLimitMessageAndDisableButton(false, "Çevir ($remainingSpins hak kaldı)")
        }
    }

    private fun setupListeners() {
        binding.btnSpinWheel.setOnClickListener {
            if (binding.btnSpinWheel.isEnabled) {
                spinWheel()
            }
        }
    }

    private fun spinWheel() {
        if (userId == null || currentSpinCount >= MAX_SPINS) {
            checkSpinAvailability()
            return
        }

        binding.btnSpinWheel.isEnabled = false
        binding.tvWinnings.visibility = View.GONE

        val targetIndex = random.nextInt(wheelSections.size)
        winningAmount = wheelSections[targetIndex]

        val degreesPerSection = 360f / wheelSections.size
        val targetDegree = targetIndex * degreesPerSection + (degreesPerSection / 2f)
        val fullCircles = 5 + random.nextInt(4)
        val totalRotation = (fullCircles * 360f) + targetDegree

        val animator = ObjectAnimator.ofFloat(
            binding.imgLuckyWheel,
            "rotation",
            binding.imgLuckyWheel.rotation,
            totalRotation
        )

        animator.duration = 4000L
        animator.interpolator = DecelerateInterpolator()

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)

                // Sayacı artır ve sonucu işle
                incrementSpinCount()
                handleWinnings()
            }
        })

        animator.start()
    }

    // Çevirme sayısını artır ve kalıcı kaydet
    private fun incrementSpinCount() {
        if (userId != null) {
            currentSpinCount++
            // Zamanı güncelleme: Sadece hak sıfırlandığında zaman güncellenir.
            // Bu metotta sadece sayıyı güncelliyoruz.
            prefs.edit().putInt(KEY_SPIN_COUNT_PREFIX + userId, currentSpinCount).apply()
        }
    }

    private fun handleWinnings() {
        // 1. Kazanılan ödülü göster
        binding.tvWinnings.text = "🎉 Tebrikler! ${winningAmount} TL indirim kazandınız."
        binding.tvWinnings.visibility = View.VISIBLE

        // 2. İndirim kodunu kaydet
        val discountCode = "CAK_INDIRIM_${winningAmount}TL_HAK${currentSpinCount}"
        managmentDiscount.saveDiscountCode(discountCode, winningAmount.toDouble())

        Toast.makeText(this, "${winningAmount} TL indirim kodu sepete eklendi! ($discountCode)", Toast.LENGTH_LONG).show()

        // 3. Hak kontrolünü tekrar yap ve butonu ayarla
        checkSpinAvailability()
    }

    // Butonu devre dışı bırakır/aktif eder ve mesajı ayarlar
    private fun showLimitMessageAndDisableButton(isLimitReached: Boolean, message: String) {
        if (isLimitReached) {
            binding.btnSpinWheel.isEnabled = false
            binding.btnSpinWheel.text = "Hak Bitti"
            binding.tvSpinLimitMessage.text = message
            binding.tvSpinLimitMessage.visibility = View.VISIBLE
        } else {
            binding.btnSpinWheel.isEnabled = true
            binding.btnSpinWheel.text = message // Burada "Çevir (X hak kaldı)" yazar
            binding.tvSpinLimitMessage.visibility = View.GONE
        }
    }
}