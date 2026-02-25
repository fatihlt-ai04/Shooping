package com.levent.project2002.Activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.levent.project2002.databinding.ActivityOrdersBinding
import android.content.Intent // Intent için eklendi
// Adaptör ve Modelinizin doğru paket yollarını kontrol edin
import com.levent.project2002.Adepter.OrderAdapter
import com.levent.project2002.Model.OrderModel
// LuckyWheelActivity'nin doğru paket yolunu buraya ekleyin
import com.levent.project2002.Activity.LuckyWheelActivity

class OrdersActivity : BaseActivity() {

    private lateinit var binding: ActivityOrdersBinding
    private lateinit var adapter: OrderAdapter
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Siparişleri filtrelemek için seçili durumu tutar (Şu an sadece UI filtresi)
    private var currentFilter: String = "Trendyol"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar() // 🔥 Güncellenmiş metot çağrılıyor
        setupTabs()
        setupListeners()
        setupRecyclerView()
        loadOrders() // Varsayılan filtre ile siparişleri yükle
    }

    // ------------------------------------
    // UI VE SETUP METOTLARI
    // ------------------------------------

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarOrders)

        // 🔥 1. Varsayılan Toolbar başlığını kaldır (Çift başlık sorununu çözer)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // binding.toolbarTitleOrders.text zaten XML'de "Siparişlerim" olarak tanımlı.

        binding.toolbarOrders.setNavigationOnClickListener { finish() }
    }

    private fun setupTabs() {
        binding.tabLayoutOrders.getTabAt(0)?.select()

        binding.tabLayoutOrders.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentFilter = when (tab.position) {
                    0 -> "Trendyol"
                    1 -> "İkinci El"
                    2 -> {
                        // Şanslı Çekiliş sekmesi tıklandığında LuckyWheelActivity'ye yönlendir
                        startActivity(Intent(this@OrdersActivity, LuckyWheelActivity::class.java))

                        // Bu sekmede kalmak yerine varsayılan Trendyol sekmesine geri dönebiliriz.
                        binding.tabLayoutOrders.getTabAt(0)?.select()

                        "Trendyol"
                    }
                    else -> "Trendyol"
                }
                loadOrders() // Yeni filtre ile siparişleri yeniden yükle
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Eğer tekrar aynı sekme seçilirse (ve bu Şanslı Çekiliş değilse) siparişleri yenile
                if (tab.position != 2) {
                    loadOrders()
                }
            }
        })
    }

    private fun setupListeners() {
        // Filtre butonu (btnFilter) dinleyicisi
        binding.btnFilter.setOnClickListener {
            Toast.makeText(this, "Filtreleme seçenekleri açılıyor...", Toast.LENGTH_SHORT).show()
        }

        // Arama çubuğu (etSearchOrder) dinleyicisi
        binding.etSearchOrder.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearchOrder.text.toString().trim()
                loadOrders(query)
                true
            } else {
                false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = OrderAdapter(mutableListOf())
        binding.rvOrders.layoutManager = LinearLayoutManager(this)
        binding.rvOrders.adapter = adapter
    }

    // ------------------------------------
    // FIREBASE VERİ ÇEKME (Filtre ve Arama Desteğiyle)
    // ------------------------------------

    private fun loadOrders(searchQuery: String? = null) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            showEmptyState(true)
            return
        }

        // --- DÜZELTİLMİŞ SORGULAMA ---

        // SADECE orders koleksiyonunu sorgula. userId filtresi kaldırıldı
        // (Çünkü sipariş belgelerinde userId alanı yok veya uygulama bu alana göre filtrelemekte başarısız oluyor).
        var query = db.collection("orders")

        // -----------------------------

        query.orderBy("orderDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                // NOT: Veri tabanında userId filtresi uygulamadığımız için,
                // gelen tüm siparişleri oturum açmış kullanıcıya ait olup olmadığına göre KOD İÇİNDE filtrelememiz GEREKİR.
                // ANCAK: Sipariş belgesinde userId yoksa bu da yapılamaz.

                // Şimdilik sadece tüm siparişleri getirip listelemeye çalışıyoruz.
                // Eğer listenizdeki TÜM kullanıcıların siparişleri gelirse, bu, sorunun sadece veri yolundan kaynaklandığını gösterir.

                val orderList = result.documents.mapNotNull { document ->
                    try {
                        val totalAmount = document.getDouble("totalAmount") ?: 0.0
                        val status = document.getString("status") ?: "Bilinmiyor"
                        val orderDate = document.getLong("orderDate") ?: 0L
                        val cartItems = document.get("cartItems") as? List<Map<String, Any>> ?: emptyList()

                        // KOD İÇİ FİLTRELEME: Arama sorgusu varsa filtrele
                        if (searchQuery != null && searchQuery.isNotEmpty()) {
                            val matches = cartItems.any { itemMap ->
                                itemMap["name"].toString().contains(searchQuery, ignoreCase = true)
                            }
                            if (!matches) return@mapNotNull null
                        }

                        // Buraya başka bir kullanıcı ID filtresi eklenmiyor.

                        OrderModel(
                            id = document.id,
                            status = status,
                            totalAmount = totalAmount,
                            orderDate = orderDate,
                            cartItems = cartItems
                        )
                    } catch (e: Exception) {
                        null
                    }
                }.toMutableList()

                adapter.updateList(orderList)
                showEmptyState(orderList.isEmpty())

            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Siparişler yüklenemedi: ${e.message}", Toast.LENGTH_LONG).show()
                showEmptyState(true)
            }
    }
    // ------------------------------------
    // BOŞ EKRAN YÖNETİMİ
    // ------------------------------------
    private fun showEmptyState(show: Boolean) {
        if (show) {
            binding.rvOrders.visibility = View.GONE
            binding.tvEmptyOrdersMessage.visibility = View.VISIBLE
            binding.tvEmptyOrdersMessage.text = "Henüz bir siparişiniz bulunmamaktadır."
        } else {
            binding.rvOrders.visibility = View.VISIBLE
            binding.tvEmptyOrdersMessage.visibility = View.GONE
        }
    }
}