package com.levent.project2002.Activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.levent.project2002.Adepter.WishlistAdapter
import com.levent.project2002.Adepter.WishlistChangeListener
import com.levent.project2002.Model.ItemsModel
import com.levent.project2002.databinding.ActivityWishlistBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WishlistActivity : AppCompatActivity(), WishlistChangeListener {

    private lateinit var binding: ActivityWishlistBinding
    private val PREF_NAME = "wishlist_pref"
    private val KEY_WISHLIST = "wishlist_items"
    private val gson = Gson()
    private var wishlistItems = mutableListOf<ItemsModel>()
    private lateinit var wishlistAdapter: WishlistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWishlistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar ayarlarını buraya ekleyebilirsiniz (Opsiyonel)

        loadWishlist()
        initRecyclerView()
    }

    private fun initRecyclerView() {
        binding.rvWishlist.layoutManager = LinearLayoutManager(this)
        // 🔥 Yeni Adaptörü kullan
        wishlistAdapter = WishlistAdapter(wishlistItems, this, this)
        binding.rvWishlist.adapter = wishlistAdapter
    }

    private fun loadWishlist() {
        val shared = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val json = shared.getString(KEY_WISHLIST, null)

        if (json != null) {
            val type = object : TypeToken<MutableList<ItemsModel>>() {}.type
            wishlistItems = gson.fromJson(json, type)
        } else {
            wishlistItems = mutableListOf()
        }
    }

    // 🔥 WishlistChangeListener metodu: Favori silindiğinde çağrılır
    override fun onWishlistChanged() {
        // Favori öğe sayısı değiştiğinde başlığı güncelleyebilirsiniz
        binding.tvWishlistTitle.text = "Favorilerim (${wishlistItems.size} Ürün)"
    }

    // Aktiviteye geri dönüldüğünde listenin güncel kalması için
    override fun onResume() {
        super.onResume()
        // Eğer onResume'da loadWishlist çağrılmazsa, başka bir ekranda favori eklendiğinde liste güncellenmez.
        loadWishlist()
        wishlistAdapter.notifyDataSetChanged()
        onWishlistChanged() // Başlığı güncelle
    }
}