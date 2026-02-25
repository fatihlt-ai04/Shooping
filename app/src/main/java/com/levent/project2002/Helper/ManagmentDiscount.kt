package com.levent.project2002.Helper

import android.content.Context
import android.content.SharedPreferences

class ManagmentDiscount(private val context: Context) {

    private val PREF_NAME = "DiscountPrefs"
    private val KEY_CODE = "ActiveDiscountCode"
    private val KEY_AMOUNT = "DiscountAmount"
    private val preferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // İndirim kodunu kaydeder
    fun saveDiscountCode(code: String, amount: Double) {
        val editor = preferences.edit()
        editor.putString(KEY_CODE, code)
        editor.putFloat(KEY_AMOUNT, amount.toFloat())
        editor.apply()
    }

    // Aktif indirim kodunu döndürür
    fun getDiscountCode(): String? {
        return preferences.getString(KEY_CODE, null)
    }

    // Aktif indirim miktarını döndürür
    fun getDiscountAmount(): Double {
        return preferences.getFloat(KEY_AMOUNT, 0f).toDouble()
    }

    // 🔥 TEK KULLANIMLIK İNDİRİMİ TEMİZLER
    fun clearDiscount() {
        val editor = preferences.edit()
        editor.remove(KEY_CODE)
        editor.remove(KEY_AMOUNT)
        editor.apply()
    }
}