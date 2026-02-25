package com.levent.project2002.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.google.firebase.auth.FirebaseAuth
import com.levent.project2002.Activity.BaseActivity
import com.levent.project2002.Adepter.CategoryAdapter
import com.levent.project2002.Adepter.RecommendedAdepter
import com.levent.project2002.Model.CategoryModel
import com.levent.project2002.Model.SliderModel
import com.levent.project2002.databinding.ActivityMainBinding
import com.levent.project2002.viewmodel.MainViewModel
import com.levent.project2002.Adepter.SliderAdepter
import kotlin.jvm.java

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        initBanner()
        initCategory()
        initRecommeded()

        // 🚨 YENİ: ÜRÜN EKLEME FAB BUTONU AKTİFLEŞTİRİLİYOR
        binding.fabAddProduct.setOnClickListener {
            // AddProductActivity'yi başlat
            startActivity(Intent(this, AddProductActivity::class.java))
        }

        // 🛒 SEPET BUTONU
        binding.cartIcon.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
        // ❤️ WISHLIST İKONU
        binding.wishlistIcon.setOnClickListener {
            startActivity(Intent(this, WishlistActivity::class.java))
        }
        // 👤 PROFİL İKONU EKLENTİSİ
        binding.profileIcon.setOnClickListener {
            checkLoginAndNavigate()
        }

        // 🔥 YENİ: SİPARİŞLERİM İKONU (ordersContainer ID'si kullanılıyor)
        binding.ordersContainer.setOnClickListener {
            navigateToOrders()
        }
    }

    // 🚦 Giriş Durumunu Kontrol Eden ve Yönlendiren Fonksiyon (Profil İçin)
    private fun checkLoginAndNavigate() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    // 🔥 YENİ METOT: Siparişler Aktivitesine Yönlendirme
    private fun navigateToOrders() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Giriş yapmışsa Siparişler ekranına git
            val intent = Intent(this, OrdersActivity::class.java)
            startActivity(intent)
        } else {
            // Giriş yapmamışsa, LoginActivity'ye yönlendir
            Toast.makeText(this, "Siparişlerinizi görmek için lütfen giriş yapın.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    // --- Mevcut Diğer Metotlar ---

    private fun initRecommeded() {
        binding.progressBarRecommend.visibility = View.VISIBLE
        viewModel.recommended.observe(this,{
            binding.viewRecommendation.layoutManager= GridLayoutManager(this@MainActivity,2)
            binding.viewRecommendation.adapter= RecommendedAdepter(it)
            binding.progressBarRecommend.visibility= View.GONE
        })
        viewModel.loadRecommended()
    }

    private fun initCategory() {
        val context = this@MainActivity

        binding.progessBarCategory.visibility = View.VISIBLE

        viewModel.categories.observe(this) { categoryList ->

            binding.viewCategory.layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            binding.viewCategory.adapter = CategoryAdapter(categoryList.toMutableList())
            binding.progessBarCategory.visibility = View.GONE
        }
        viewModel.loadCategories()
    }

    private fun banners(image: List<SliderModel>) {
        binding.viewPager2.adapter = SliderAdepter(image, binding.viewPager2)
        binding.viewPager2.clipToPadding = false
        binding.viewPager2.clipChildren = false
        binding.viewPager2.offscreenPageLimit = 3
        binding.viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        val compositePageTransformer = CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(40))
        }

        binding.viewPager2.setPageTransformer(compositePageTransformer)

        if (image.size > 1) {
            binding.dotIndicator.visibility = View.VISIBLE
        }
    }

    private fun initBanner() {
        binding.progressBarSlider.visibility = View.VISIBLE

        viewModel.banner.observe(this, Observer<List<SliderModel>> { list ->
            banners(list)
            binding.progressBarSlider.visibility = View.GONE
        })

        viewModel.loadBanners()
    }
}