package com.levent.project2002.Helper

import android.content.Context
import android.widget.Toast
import com.levent.project2002.Helper.TinyDB
import com.levent.project2002.Model.ItemsModel
import com.levent.project2002.Model.DiscountModel

class ManagmentCart(val context: Context) {

    private val tinyDB = TinyDB(context)
    private val CART_LIST_KEY = "CartList"
    private val COUPON_KEY = "AppliedCoupon"

    fun insertFood(item: ItemsModel) {
        var listFood = getListCart()
        val existAlready = listFood.any { it.title == item.title }
        val index = listFood.indexOfFirst { it.title == item.title }

        if (existAlready) {
            listFood[index].numberInCart = item.numberInCart
        } else {
            listFood.add(item)
        }
        tinyDB.putListObject(CART_LIST_KEY, listFood)
        Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show()
    }

    fun getListCart(): ArrayList<ItemsModel> {
        return tinyDB.getListObject(CART_LIST_KEY) ?: arrayListOf()
    }

    fun minusItem(listFood: ArrayList<ItemsModel>, position: Int, listener: ChangeNumberItemsListener) {
        if (listFood[position].numberInCart == 1) {
            listFood.removeAt(position)
        } else {
            listFood[position].numberInCart--
        }
        tinyDB.putListObject(CART_LIST_KEY, listFood)
        listener.onChanged()
    }

    fun plusItem(listFood: ArrayList<ItemsModel>, position: Int, listener: ChangeNumberItemsListener) {
        listFood[position].numberInCart++
        tinyDB.putListObject(CART_LIST_KEY, listFood)
        listener.onChanged()
    }

    fun saveList(list: ArrayList<ItemsModel>) {
        tinyDB.putListObject(CART_LIST_KEY, list)
    }

    // 💰 Kuponu hesaba katarak toplam ücreti hesaplar
    fun getTotalFee(): Double {
        val listFood = getListCart()
        var subtotal = 0.0
        var discountAmount = 0.0

        for (item in listFood) {
            subtotal += item.price * item.numberInCart
        }

        val appliedCoupon = getAppliedCoupon()

        if (appliedCoupon != null && subtotal >= appliedCoupon.altLimit) {
            when (appliedCoupon.discountValue.trim()) {
                "150 TL" -> { discountAmount = 150.0 }
                "200 TL" -> { discountAmount = 200.0 }

                else -> {
                    val percentageMatch = Regex("%(\\d+)").find(appliedCoupon.discountValue)
                    if (percentageMatch != null) {
                        val percentage = percentageMatch.groupValues[1].toDoubleOrNull() ?: 0.0
                        discountAmount = subtotal * (percentage / 100.0)
                    } else if (appliedCoupon.discountValue.contains("%")) {
                        val value = appliedCoupon.discountValue.replace("%", "").replace("TRY", "").trim().toDoubleOrNull() ?: 0.0
                        discountAmount = subtotal * (value / 100.0)
                    }
                }
            }

            if (appliedCoupon.maxDiscount != null && discountAmount > appliedCoupon.maxDiscount) {
                discountAmount = appliedCoupon.maxDiscount
            }
        }

        return (subtotal - discountAmount).coerceAtLeast(0.0)
    }

    fun applyCoupon(coupon: DiscountModel) {
        tinyDB.putObject(COUPON_KEY, coupon)
    }

    // 🔥 DÜZELTİLDİ: TinyDB'den kaynaklanan NullPointerException hatalarını yakalar
    fun getAppliedCoupon(): DiscountModel? {
        return try {
            // Eğer TinyDB'de kupon yoksa veya hatalı serileştirilmişse Null dönebilir.
            val couponObject = tinyDB.getObject(COUPON_KEY, DiscountModel::class.java)
            couponObject as? DiscountModel
        } catch (e: Exception) {
            // Hata oluşursa (NullPointerException dahil) çökme yerine null döndür
            e.printStackTrace()
            null
        }
    }

    fun clearCart() {
        tinyDB.remove(CART_LIST_KEY)
        tinyDB.remove(COUPON_KEY)
    }

    interface ChangeNumberItemsListener {
        fun onChanged()
    }
}