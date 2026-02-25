package com.levent.project2002.Activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.levent.project2002.Adepter.DiscountAdapter
import com.levent.project2002.Model.DiscountModel
import com.levent.project2002.databinding.ActivityOffersBinding

class OffersActivity : BaseActivity() {

    private lateinit var binding: ActivityOffersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOffersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        initDiscountsRecyclerView()
    }

    private fun setupToolbar() {
        // XML'de toolbar ID'nizin 'toolbarOffers' olduğunu varsayıyoruz
        setSupportActionBar(binding.toolbarOffers)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "İndirim Kuponlarım"
        binding.toolbarOffers.setNavigationOnClickListener { finish() }
    }

    private fun initDiscountsRecyclerView() {
        // 🔥 GERÇEK ÜRÜN İSİMLERİNİZİ VE KATEGORİ ID'LERİNİZİ KULLANAN VERİLER
        val sampleDiscounts = listOf(
            DiscountModel(
                title = "Kulaklık ve Aksesuarlarda %10 İndirim Fırsatı",
                discountValue = "%10 TRY",
                altLimit = 350.0,
                maxDiscount = 200.0,
                expiryDate = "30.12.2025",
                // Bu URL'ler, Headphone/Aksesuar kategorisine ait ürün görselleri olmalıdır (Kategori ID: 2)
                products = listOf("URL_HEADPHONE_1", "URL_HEADPHONE_2"),
                isLimited = false,
                targetId = "2",        // 🔥 Headphone kategorisi ID'si
                targetType = "CATEGORY"
            ),
            DiscountModel(
                title = "Laptop ve PC Aksesuarlarında Özel Teklif",
                discountValue = "150 TL",
                altLimit = 1000.0,
                maxDiscount = null,
                expiryDate = "15.12.2025",
                // Bu URL'ler, Laptop/PC kategorisine ait ürün görselleri olmalıdır (Kategori ID: 0)
                products = listOf("URL_LAPTOP_1", "URL_LAPTOP_2"),
                isLimited = true,
                targetId = "0",        // 🔥 PC kategorisi ID'si
                targetType = "CATEGORY"
            ),
            DiscountModel(
                title = "PS5 Konsollarında Süper Fırsat",
                discountValue = "%5 TRY",
                altLimit = 4000.0,
                maxDiscount = 500.0,
                expiryDate = "25.12.2025",
                // Bu URL'ler, Console kategorisine ait ürün görselleri olmalıdır (Kategori ID: 3)
                products = listOf("URL_PS5_1"),
                isLimited = false,
                targetId = "3",        // 🔥 Console kategorisi ID'si
                targetType = "CATEGORY"
            ),
            // Başlangıçtaki Powertec örneğini de ekleyelim (Eğer isterseniz)
            DiscountModel(
                title = "Powertec Kişisel Bakım Aletlerinde Sınırsız İndirim",
                discountValue = "%5 TRY",
                altLimit = 500.0,
                maxDiscount = 150.0,
                expiryDate = "16.12.2025",
                products = listOf("URL_POWERTEC_1", "URL_POWERTEC_2"),
                isLimited = false,
                targetId = "99",       // 🔥 Bu kategori yoksa, geçici bir ID (Listeleme boş dönecektir)
                targetType = "CATEGORY"
            )
        )

        binding.rvDiscounts.apply {
            layoutManager = LinearLayoutManager(this@OffersActivity, LinearLayoutManager.VERTICAL, false)
            adapter = DiscountAdapter(sampleDiscounts)
        }
    }
}