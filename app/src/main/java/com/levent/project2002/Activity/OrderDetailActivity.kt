package com.levent.project2002.Activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.levent.project2002.Adepter.OrderProductAdapter
import com.levent.project2002.databinding.ActivityOrderDetailBinding
import android.content.Intent // 🔥 Intent için eklendi
// TrackingActivity'nin doğru paket yolunu buraya ekleyin
import com.levent.project2002.Activity.TrackingActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityOrderDetailBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("tr", "TR"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        val orderId = intent.getStringExtra("EXTRA_ORDER_ID")

        if (orderId != null) {
            loadOrderDetail(orderId)
        } else {
            Toast.makeText(this, "Sipariş numarası bulunamadı.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbarDetail.setNavigationOnClickListener { finish() }
    }

    private fun loadOrderDetail(orderId: String) {
        binding.progressBarDetail.visibility = View.VISIBLE

        db.collection("orders")
            .document(orderId)
            .get()
            .addOnSuccessListener { document ->
                binding.progressBarDetail.visibility = View.GONE

                if (document.exists()) {

                    val orderMap = document.data ?: return@addOnSuccessListener

                    val totalAmount = orderMap["totalAmount"] as? Double ?: 0.0
                    val status = orderMap["status"] as? String ?: "Bilinmiyor"
                    val orderDateMillis = document.getLong("orderDate") ?: 0L
                    val addressId = orderMap["addressId"] as? String

                    // 🔥 YENİ VERİLER: Kargo ve Ödeme
                    val subTotal = orderMap["subTotal"] as? Double ?: (totalAmount - 5.0)
                    val discountAmount = orderMap["discountAmount"] as? Double ?: 0.0
                    val deliveryFee = orderMap["deliveryFee"] as? Double ?: 5.0
                    val paymentMethod = orderMap["paymentMethod"] as? String ?: "Tek Çekim"
                    val cardLast4 = orderMap["cardNumberLast4"] as? String ?: "****"

                    // 🔥 KARGO TAKİP BİLGİLERİ (FireStore'dan çekildi)
                    val cargoCompanyName = orderMap["shippingCompany"] as? String ?: "Trendyol Express"
                    val trackingNumber = orderMap["trackingNumber"] as? String ?: "BILINMIYOR"

                    val formattedDate = dateFormat.format(Date(orderDateMillis))
                    val shortId = orderId.takeLast(8).uppercase(Locale.getDefault())

                    // Teslimat No + Tarih
                    binding.tvDeliveryNumber.text = "Teslimat No: #${shortId} (Tarih: $formattedDate)"
                    binding.tvSellerName.text = "Satıcı: PRODİZAYN"
                    binding.tvOrderStatus.text = status

                    // ÖZET BÖLÜMÜNÜ DOLDURMA
                    setupPaymentSummary(subTotal, discountAmount, deliveryFee, totalAmount, paymentMethod, cardLast4)

                    // 🔥 KARGO BİLGİLERİNİ GÖSTER VE BUTONU AYARLA
                    binding.tvCargoFirm.text = "Kargo Firması: $cargoCompanyName"
                    binding.tvShipmentInfo.text = "Sipariş durumu: $status"
                    setupTrackCargoButton(cargoCompanyName, trackingNumber) // 🔥 Yeni Metot Çağrısı

                    // İptal butonu kontrolü
                    checkStatusAndSetupCancelButton(status, orderId)

                    // Adres yükleme
                    if (addressId != null) {
                        loadAddressDetails(addressId)
                    } else {
                        binding.tvAddressDetails.text = "Teslimat adresi bulunamadı."
                    }

                    // Ürün listesi
                    val cartItems = orderMap["cartItems"] as? List<Map<String, Any>> ?: emptyList()
                    setupProductsRecyclerView(cartItems)

                    // Satıcı takip
                    binding.btnFollowSeller.setOnClickListener {
                        Toast.makeText(this, "Satıcı takip edildi!", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(this, "Sipariş detayları bulunamadı.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                binding.progressBarDetail.visibility = View.GONE
                Toast.makeText(
                    this,
                    "Detaylar yüklenirken hata oluştu: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
    }

    // 🔥 YENİ METOT: Kargom Nerede Butonunu ve Yönlendirmesini Ayarlar
    private fun setupTrackCargoButton(cargoCompanyName: String, trackingNumber: String) {
        // Varsayım: XML'de btnTrackCargo ID'li bir buton/ImageView var.

        binding.btnTrackCargo.setOnClickListener {
            if (trackingNumber == "BILINMIYOR" || trackingNumber.isNullOrEmpty()) {
                Toast.makeText(this, "Takip numarası henüz atanmadı.", Toast.LENGTH_SHORT).show()
            } else {
                // TrackingActivity'ye takip numarasını göndererek yönlendir
                val intent = Intent(this@OrderDetailActivity, TrackingActivity::class.java).apply {
                    putExtra("EXTRA_TRACKING_NUMBER", trackingNumber)
                    putExtra("EXTRA_CARGO_COMPANY", cargoCompanyName)
                }
                startActivity(intent)
            }
        }
    }


    private fun setupPaymentSummary(
        subTotal: Double,
        discountAmount: Double,
        deliveryFee: Double,
        totalAmount: Double,
        paymentMethod: String,
        cardLast4: String
    ) {
        // Kart Bilgisi
        binding.tvPaymentCardAndInstallment.text =
            "**** **** **** $cardLast4 - $paymentMethod"

        // Ara Toplam
        binding.tvSummarySubtotal.text = String.format("%.2f TL", subTotal)

        // Kargo Ücreti
        binding.tvSummaryDeliveryFee.text = String.format("%.2f TL", deliveryFee)

        // İndirim Satırı
        if (discountAmount > 0) {
            binding.llDiscountRow.visibility = View.VISIBLE
            binding.tvSummaryDiscount.text = String.format("-%.2f TL", discountAmount)
        } else {
            binding.llDiscountRow.visibility = View.GONE
        }

        // Genel Toplam
        binding.tvSummaryTotal.text = String.format("%.2f TL", totalAmount)
    }


    private fun checkStatusAndSetupCancelButton(status: String, orderId: String) {

        val canCancel =
            status == "Sipariş Hazırlanıyor" ||
                    status == "Yeni Sipariş"

        if (canCancel) {
            binding.btnCancelOrder.visibility = View.VISIBLE
            binding.btnCancelOrder.setOnClickListener {
                cancelOrder(orderId)
            }
        } else {
            binding.btnCancelOrder.visibility = View.GONE
        }
    }

    private fun cancelOrder(orderId: String) {

        val updates = hashMapOf<String, Any>(
            "status" to "İptal Edildi",
            "cancellationDate" to System.currentTimeMillis()
        )

        Toast.makeText(this, "Sipariş iptal ediliyor...", Toast.LENGTH_SHORT).show()

        db.collection("orders").document(orderId)
            .update(updates)
            .addOnSuccessListener {

                Toast.makeText(this, "Sipariş başarıyla iptal edildi.", Toast.LENGTH_LONG).show()

                // Ekranı güncelle
                binding.tvOrderStatus.text = "İptal Edildi"
                binding.btnCancelOrder.visibility = View.GONE

                finish() // Sipariş detaydan çık
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "İptal başarısız oldu: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadAddressDetails(addressId: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("addresses")
            .document(addressId)
            .get()
            .addOnSuccessListener { document ->

                val title = document.getString("title") ?: "Başlıksız Adres"
                val city = document.getString("city") ?: ""
                val district = document.getString("district") ?: ""
                val fullAddress =
                    document.getString("address") ?: "Adres detayı mevcut değil."

                binding.tvAddressDetails.text =
                    "Başlık: $title ($district/$city)\nAdres: $fullAddress"
            }
            .addOnFailureListener {
                binding.tvAddressDetails.text = "Adres detayları yüklenemedi."
            }
    }

    private fun setupProductsRecyclerView(cartItems: List<Map<String, Any>>) {
        if (cartItems.isEmpty()) {
            binding.rvProducts.visibility = View.GONE
            binding.tvProductsTitle.text = "Sipariş Edilen Ürünler (0 Ürün)"
            return
        }

        val adapter = OrderProductAdapter(cartItems)
        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = adapter
    }
}