package com.levent.project2002.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager

import com.levent.project2002.Adepter.CartAdapter
import com.levent.project2002.Helper.ManagmentCart
import com.levent.project2002.databinding.ActivityCartBinding
import com.levent.project2002.Adepter.CartCouponAdapter
import com.levent.project2002.Adepter.CouponSelectListener
import com.levent.project2002.Model.DiscountModel
import com.levent.project2002.Helper.ManagmentDiscount // 🔥 Çark İndirim Yöneticisi

class CartActivity : BaseActivity(), CouponSelectListener {

    private lateinit var binding: ActivityCartBinding
    private lateinit var managmentCart: ManagmentCart
    private lateinit var managmentDiscount: ManagmentDiscount // 🔥 Yeni İndirim Yöneticisi

    // Kargo ücreti
    private val deliveryFee: Double = 5.0

    // Ham toplam ve indirimli toplamı tutmak için değişkenler
    private var subTotalHam: Double = 0.0
    private var subTotalIndirimli: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        managmentCart = ManagmentCart(this)
        managmentDiscount = ManagmentDiscount(this) // 🔥 İndirim Yöneticisi Başlatıldı

        initCartList()
        initCouponList()
        calculateCart() // 🔥 Hesaplama başlatılıyor
        setListener()
    }

    // ... (initCartList ve initCouponList metotları aynı kalır) ...

    private fun initCartList() {
        val cartItems = managmentCart.getListCart()

        binding.cartList.adapter = CartAdapter(cartItems, managmentCart) {
            calculateCart()
        }

        binding.cartList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    private fun initCouponList() {
        // Kupon listesi verileri
        val availableCoupons = listOf(
            DiscountModel(title = "Kulaklık & Aksesuar", discountValue = "%10 TRY", altLimit = 350.0, maxDiscount = 200.0, expiryDate = "", products = emptyList(), targetId = "2", targetType = "CATEGORY"),
            DiscountModel(title = "Laptop & PC", discountValue = "150 TL", altLimit = 1000.0, maxDiscount = null, expiryDate = "", products = emptyList(), targetId = "0", targetType = "CATEGORY"),
            DiscountModel(title = "PS5 Konsol", discountValue = "%5 TRY", altLimit = 4000.0, maxDiscount = 500.0, expiryDate = "", products = emptyList(), targetId = "3", targetType = "CATEGORY")
        )

        binding.rvCouponList.apply {
            layoutManager = LinearLayoutManager(this@CartActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = CartCouponAdapter(availableCoupons, this@CartActivity)
        }
    }


    private fun calculateCart() {
        val taxRate = 0.18

        // 1. Ham Fiyatı hesapla (Kupon uygulanmamış hali)
        subTotalHam = managmentCart.getListCart().sumOf { it.price * it.numberInCart }

        // 2. Kupon uygulandıktan sonraki indirimli fiyatı ManagmentCart'tan al (Manuel Kuponlar)
        subTotalIndirimli = managmentCart.getTotalFee()

        // 3. Çark İndirimini Al (Otomatik Kuponlar)
        val wheelDiscountAmount = managmentDiscount.getDiscountAmount()
        val wheelDiscountCode = managmentDiscount.getDiscountCode()

        // Toplam Uygulanan İndirim = Manuel Kupon İndirimi + Çark İndirimi
        val totalAppliedDiscount = (subTotalHam - subTotalIndirimli) + wheelDiscountAmount

        // İndirimler uygulandıktan sonraki yeni ara toplam (subTotalIndirimli)
        val totalSubTotalAfterAllDiscounts = subTotalIndirimli - wheelDiscountAmount

        // Not: Çark indirimi sıfırdan küçük olamaz
        val totalFinal = if (totalSubTotalAfterAllDiscounts > 0) {
            totalSubTotalAfterAllDiscounts + (totalSubTotalAfterAllDiscounts * taxRate) + deliveryFee
        } else {
            // Eğer indirimler ürünü bedava yaparsa sadece KDV ve kargo alınabilir
            0.0 + deliveryFee
        }

        val taxAmount = totalSubTotalAfterAllDiscounts * taxRate

        // 4. UI Güncelleme ve İndirim Gösterimi
        binding.subTotalTxt.text = "$${String.format("%.2f", subTotalHam)}"
        binding.taxTxt.text = "$${String.format("%.2f", taxAmount)}"
        binding.deliveryTxt.text = "$${String.format("%.2f", deliveryFee)}"
        binding.totalTxt.text = "$${String.format("%.2f", totalFinal)}"

        // 🔥 Genel İndirim Yazısı ve Miktarını Gösterme
        if (totalAppliedDiscount > 0) {
            binding.discountAmountTxt.text = "-$${String.format("%.2f", totalAppliedDiscount)}"
            binding.discountContainer.visibility = View.VISIBLE

            // Eğer Çark İndirimi uygulandıysa başlıkta göster (Opsiyonel)
            if (wheelDiscountAmount > 0) {
                // Not: XML'de buna uygun bir TextView varsa oraya yazın.
                Toast.makeText(this, "Çark İndirimi (${wheelDiscountAmount} TL) uygulandı!", Toast.LENGTH_SHORT).show()
            }

        } else {
            binding.discountContainer.visibility = View.GONE
        }
    }

    // ... (setListener metodu aynı kalır) ...

    private fun setListener() {
        binding.backBtn.setOnClickListener { finish() }

        binding.confirmBtn.setOnClickListener {
            if (managmentCart.getListCart().isEmpty()) {
                Toast.makeText(this, "Sepetinizde ürün bulunmamaktadır.", Toast.LENGTH_SHORT).show()
            } else {
                navigateToCheckout()
            }
        }
    }

    private fun navigateToCheckout() {
        val totalAmountText = binding.totalTxt.text.toString()

        val intent = Intent(this, CheckoutActivity::class.java).apply {
            // İndirimli toplam tutarı gönder
            putExtra("EXTRA_TOTAL_AMOUNT_TEXT", totalAmountText)

            // 🔥 Çark indirimi aktifse, ödeme başarılı olduktan sonra temizlenmesi gerektiğini unutmayın.
            // Bu temizlik CheckoutActivity'de yapılacaktır.
        }
        startActivity(intent)
    }

    // 🔥 CouponSelectListener Metodu: Manuel Kupon tıklandığında çağrılır
    override fun onCouponApplied(coupon: DiscountModel) {
        // 1. Kuponu kalıcı olarak kaydet
        managmentCart.applyCoupon(coupon)

        // 2. Sepeti yeniden hesapla ve UI'yı güncelle
        calculateCart()

        Toast.makeText(this, "${coupon.title} başarıyla uygulandı! Sepeti kontrol edin.", Toast.LENGTH_SHORT).show()
    }
}