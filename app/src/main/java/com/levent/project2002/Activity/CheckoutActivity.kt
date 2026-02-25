package com.levent.project2002.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.levent.project2002.databinding.ActivityCheckoutBinding
import com.levent.project2002.Helper.ManagmentCart
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.levent.project2002.Helper.ManagmentDiscount
import java.util.Calendar
import android.os.Handler
import android.os.Looper

class CheckoutActivity : BaseActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var managmentCart: ManagmentCart
    private lateinit var managmentDiscount: ManagmentDiscount
    private var selectedAddressId: String? = null
    private val addressIds = mutableListOf<String>()
    private var totalAmount: Double = 0.0

    // ------------------------------------
    // 🔥 1. ADIM: ACTIVITY RESULT İÇİN YENİ YAPI
    // AddressesActivity'den gelen sonucu yakalamak için Launcher tanımı
    // ------------------------------------
    private val addressResultLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val newAddressId = data?.getStringExtra("EXTRA_SELECTED_ADDRESS_ID")

            if (newAddressId != null) {
                // Seçilen yeni adresi global değişkene atıyoruz
                selectedAddressId = newAddressId

                // Dropdown listesini (Spinner'ı) yeniden yükleyerek
                // yeni seçilen adresi kullanıcıya gösteriyoruz.
                // Seçilen ID'yi, otomatik seçilmesi için gönderiyoruz.
                loadAddressesForDropdown(selectId = newAddressId)

                Toast.makeText(this, "Adres başarıyla güncellendi.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        managmentCart = ManagmentCart(this)
        managmentDiscount = ManagmentDiscount(this)

        val totalAmountText = intent.getStringExtra("EXTRA_TOTAL_AMOUNT_TEXT")
            ?.replace("$", "")?.replace(",", ".")?.trim()
        totalAmount = totalAmountText?.toDoubleOrNull() ?: 0.0

        setupToolbar()
        // onCreate'de varsayılan yükleme
        loadAddressesForDropdown()
        setupPaymentOptions()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarCheckout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = "Güvenli Ödeme"
        binding.toolbarCheckout.setNavigationOnClickListener { finish() }
    }

    // ------------------------------------
    // 🔥 2. ADIM: loadAddressesForDropdown METODU GÜNCELLENDİ
    // Artık seçilen adresi otomatik olarak spinner'da gösterebilir.
    // ------------------------------------
    private fun loadAddressesForDropdown(selectId: String? = null) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(userId)
            .collection("addresses")
            .get()
            .addOnSuccessListener { result ->
                val addressNameList = mutableListOf<String>()
                addressIds.clear()
                var selectedPosition = 0 // Yeni seçilen adresin pozisyonunu tutar

                result.documents.forEachIndexed { index, doc ->
                    val title = doc.getString("title") ?: "Başlıksız Adres"
                    val city = doc.getString("city") ?: ""
                    val district = doc.getString("district") ?: ""
                    val addressId = doc.id
                    addressIds.add(addressId)
                    addressNameList.add("$title ($district/$city)")

                    // Eğer selectId (yani AddressesActivity'den gelen ID) varsa, pozisyonu kaydet.
                    if (addressId == selectId) {
                        selectedPosition = index
                    }
                }

                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    addressNameList
                )
                binding.spinnerDeliveryAddress.adapter = adapter

                if (addressIds.isNotEmpty()) {
                    // Spinner'ın o adrese ayarlanması
                    binding.spinnerDeliveryAddress.setSelection(selectedPosition)
                    selectedAddressId = addressIds[selectedPosition] // selectedAddressId'yi de güncelle

                } else {
                    selectedAddressId = null
                }


                binding.spinnerDeliveryAddress.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            selectedAddressId = addressIds[position]
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {}
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Adresler yüklenemedi: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setupPaymentOptions() {
        if (totalAmount > 0) {
            // Taksit seçeneklerini ayarla
            binding.checkBoxInstallment1.text = "Tek Çekim ${String.format("%.2f", totalAmount)} TL"
            val installment2Amount = totalAmount / 2.0
            binding.checkBoxInstallment2.text = "2 Taksit 2 x ${String.format("%.2f", installment2Amount)} TL"
            val installment3Amount = totalAmount / 3.0
            binding.checkBoxInstallment3.text = "3 Taksit 3 x ${String.format("%.2f", installment3Amount)} TL"
            val installment4Amount = totalAmount / 4.0
            binding.checkBoxInstallment4.text = "4 Taksit 4 x ${String.format("%.2f", installment4Amount)} TL"
        }

        binding.checkBoxInstallment1.isChecked = true

        binding.btnConfirmAndFinish.setOnClickListener {
            if (selectedAddressId.isNullOrEmpty()) {
                Toast.makeText(this, "Lütfen teslimat adresi seçin.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kart bilgileri kontrolü
            if (validateCardDetails()) {
                saveOrderAndFinish()
            }
        }
    }

    // Kart Bilgilerini Kontrol Etme ve Doğrulama
    private fun validateCardDetails(): Boolean {
        val cardNumber = binding.etCardNumber.text.toString().trim()
        val expMonth = binding.etExpirationMonth.text.toString().toIntOrNull()
        val expYear = binding.etExpirationYear.text.toString().toIntOrNull()
        val cvv = binding.etCVV.text.toString().trim()

        if (cardNumber.isEmpty() || expMonth == null || expYear == null || cvv.isEmpty()) {
            Toast.makeText(this, "Lütfen kart bilgilerini eksiksiz doldurun.", Toast.LENGTH_LONG).show()
            return false
        }
        if (cardNumber.length != 16 || !cardNumber.all { it.isDigit() }) {
            Toast.makeText(this, "Kart numarası 16 rakamdan oluşmalıdır.", Toast.LENGTH_LONG).show()
            return false
        }
        if (expMonth < 1 || expMonth > 12) {
            Toast.makeText(this, "Geçerli bir son kullanma ayı (01-12) giriniz.", Toast.LENGTH_LONG).show()
            return false
        }
        if (cvv.length !in 3..4 || !cvv.all { it.isDigit() }) {
            Toast.makeText(this, "CVV/CVC kodu 3 veya 4 rakamdan oluşmalıdır.", Toast.LENGTH_LONG).show()
            return false
        }

        val currentYear = Calendar.getInstance().get(Calendar.YEAR) % 100
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

        if (expYear < currentYear) {
            Toast.makeText(this, "Kartınızın son kullanma tarihi geçmiş.", Toast.LENGTH_LONG).show()
            return false
        } else if (expYear == currentYear && expMonth < currentMonth) {
            Toast.makeText(this, "Kartınızın son kullanma tarihi geçmiş.", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    // SİPARİŞİ KAYDETME VE BİTİRME METODU (10 saniye Gecikmeli Kargo Durumu Güncellemesi)
    private fun saveOrderAndFinish() {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "Ödeme yapmadan önce giriş yapın.", Toast.LENGTH_LONG).show()
            return
        }

        // Adresin null olmadığını garanti et
        if (selectedAddressId.isNullOrEmpty()) {
            Toast.makeText(this, "Lütfen teslimat adresi seçin.", Toast.LENGTH_SHORT).show()
            return
        }
        val addressIdToSave: String = selectedAddressId!! // Seçilen adres ID'si

        val cardNumber = binding.etCardNumber.text.toString().trim()
        val paymentLast4 = if (cardNumber.length >= 4) cardNumber.takeLast(4) else "XXXX"

        val paymentMethod = when {
            binding.checkBoxInstallment1.isChecked -> "Tek Çekim"
            binding.checkBoxInstallment2.isChecked -> "2 Taksit"
            binding.checkBoxInstallment3.isChecked -> "3 Taksit"
            binding.checkBoxInstallment4.isChecked -> "4 Taksit"
            else -> {
                Toast.makeText(this, "Lütfen bir ödeme yöntemi seçin.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // KARGO BİLGİLERİ VE BAŞLANGIÇ DURUMU
        val cargoCompanyName = "Trendyol Express"
        val initialTrackingNumber = "T" + System.currentTimeMillis().toString().takeLast(10)

        val orderData = hashMapOf<String, Any>(
            "userId" to userId,
            "addressId" to addressIdToSave, // Seçilen adres ID'si kaydedildi
            "orderDate" to System.currentTimeMillis(),
            "totalAmount" to totalAmount,
            "paymentMethod" to paymentMethod,
            "paymentLast4" to paymentLast4,
            "status" to "Sipariş Hazırlanıyor",
            "shippingCompany" to cargoCompanyName,
            "trackingNumber" to initialTrackingNumber // Takip numarasını hemen kaydet
        )

        // Sepet içeriği
        val cartItemsList = managmentCart.getListCart().map { item ->
            hashMapOf(
                "itemId" to item.title.trim(),
                "name" to item.title.trim(),
                "quantity" to item.numberInCart,
                "price" to item.price,
                "picUrl" to (item.picUrl.firstOrNull() ?: "")
            )
        }
        orderData["cartItems"] = cartItemsList

        // Firestore'a kaydetme işlemi
        db.collection("orders")
            .add(orderData)
            .addOnSuccessListener { documentReference ->

                val savedOrderId = documentReference.id

                // 1. Sepeti temizle ve İndirimi temizle
                managmentCart.clearCart()
                if (this::managmentDiscount.isInitialized) {
                    managmentDiscount.clearDiscount()
                }


                // 2. 10 SANİYE GECİKMELİ GÖREVİ TANIMLA: Durumu güncelle
                Handler(Looper.getMainLooper()).postDelayed({

                    // Sipariş durumunu "Kargoya Verildi" olarak güncelle
                    db.collection("orders")
                        .document(savedOrderId)
                        .update("status", "Kargoya Verildi")
                        .addOnSuccessListener {
                            // Durum güncellendi.
                        }
                        .addOnFailureListener {
                            // Hata durumunda log atılabilir.
                        }

                }, 10000L) // 10 saniye gecikme


                // 3. Başarı Ekranına Yönlendirme (Gecikmesiz)
                try {
                    val intent = Intent(this@CheckoutActivity, OrderSuccessActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()

                } catch (e: Exception) {
                    // Yönlendirme hatası durumunda Ana Sayfaya güvenli dönüş
                    Toast.makeText(this, "Sipariş başarılı, ancak onay ekranı açılırken hata oluştu.", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@CheckoutActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Hata: Sipariş oluşturulamadı: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
    // ------------------------------------
    // 🔥 3. ADIM: setupListeners METODU GÜNCELLENDİ
    // Adres Yönetimi akışını AddressResultLauncher'a bağlar.
    // ------------------------------------
    private fun setupListeners() {
        binding.tvAddEditAddress.setOnClickListener {
            // AddressesActivity'yi başlatmak için Launcher'ı kullan
            val intent = Intent(this, AddressesActivity::class.java)
            addressResultLauncher.launch(intent)
        }
    }
}