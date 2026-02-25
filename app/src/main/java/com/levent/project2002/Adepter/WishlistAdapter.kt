package com.levent.project2002.Adepter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.levent.project2002.Helper.ManagmentCart
import com.levent.project2002.Model.ItemsModel
import com.levent.project2002.R
import com.levent.project2002.databinding.ItemFavoriteRichBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable

// Activity'ye favori listesinin değiştiğini bildirmek için
interface WishlistChangeListener {
    fun onWishlistChanged()
}

class WishlistAdapter(
    private var list: MutableList<ItemsModel>,
    private val context: Context,
    private val listener: WishlistChangeListener
) : RecyclerView.Adapter<WishlistAdapter.ViewHolder>() {

    private val managmentCart = ManagmentCart(context)

    inner class ViewHolder(val binding: ItemFavoriteRichBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemFavoriteRichBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        // 1. Görsel ve Temel Veri
        holder.binding.tvFavoriteTitle.text = item.title
        holder.binding.tvRating.text = String.format("%.1f", item.rating)

        Glide.with(holder.itemView.context)
            .load(item.picUrl.firstOrNull() ?: "")
            .into(holder.binding.imgFavoriteProduct)

        // 2. Fiyatlar (Simülasyon: Varsayılan %15 indirim yapıyoruz)
        val originalPrice = item.price / 0.85 // İndirimsiz fiyatı yaklaşık bul
        val discountedPrice = item.price
        val hasDiscount = item.price < originalPrice

        if (hasDiscount) {
            holder.binding.tvOldPrice.text = String.format("%.2f $", originalPrice)
            holder.binding.tvOldPrice.visibility = View.VISIBLE
            // Üstü çizili yap
            holder.binding.tvOldPrice.paintFlags = holder.binding.tvOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            holder.binding.tvCurrentPrice.text = String.format("%.2f $", discountedPrice)
        } else {
            holder.binding.tvOldPrice.visibility = View.GONE
            holder.binding.tvCurrentPrice.text = String.format("%.2f $", originalPrice)
        }

        // 3. Stok ve Kargo Etiketleri
        // 🔥 STOK KONTROLLERİ VE UYARILARI TAMAMEN KALDIRILDI
        holder.binding.tvStockStatus.visibility = View.GONE

        // Kargo Bilgisi (Sabit varsayım)
        holder.binding.tvDeliveryInfo.text = "HIZLI KARGO"

        // 4. Sepete Ekle
        holder.binding.btnAddToCart.setOnClickListener {
            // 🔥 Stok kontrolü kaldırıldı, varsayılan olarak sepete eklenir
            item.numberInCart = 1
            managmentCart.insertFood(item)
        }

        // 5. Favoriden Kaldır (Sil)
        holder.binding.btnRemoveFavorite.setOnClickListener {
            removeWishlistItem(position)
        }
    }

    // Favori Listesinden Silme Metodu
    private fun removeWishlistItem(position: Int) {
        if (position < 0 || position >= list.size) return

        val itemToRemove = list[position]
        list.removeAt(position)

        // SharedPreferences üzerinden listeyi kaydet
        saveWishlistToPrefs()

        notifyItemRemoved(position)
        notifyItemRangeChanged(position, list.size)

        Toast.makeText(context, "${itemToRemove.title} favorilerden kaldırıldı.", Toast.LENGTH_SHORT).show()
        listener.onWishlistChanged()
    }

    // SharedPreferences üzerinden listeyi kaydetme
    private fun saveWishlistToPrefs() {
        val shared = context.getSharedPreferences("wishlist_pref", Context.MODE_PRIVATE)
        val editor = shared.edit()
        val json = Gson().toJson(list)
        editor.putString("wishlist_items", json)
        editor.apply()
    }
}