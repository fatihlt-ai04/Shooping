package com.levent.project2002.Adepter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.levent.project2002.R // Drawable kaynakları (placeholder/error) için eklendi
import com.levent.project2002.databinding.ItemOrderProductBinding
import java.util.Locale

/**
 * Sipariş detay ekranındaki ürün listesini yöneten adaptör.
 * Firebase'den çekilen List<Map<String, Any>> yapısındaki ürün verileriyle çalışır.
 */
class OrderProductAdapter(
    private val productList: List<Map<String, Any>>
) : RecyclerView.Adapter<OrderProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(val binding: ItemOrderProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Map<String, Any>) {

            // Verileri Map'ten güvenli bir şekilde çekme
            val name = item["name"] as? String ?: "Ürün Adı Bilinmiyor"
            val quantity = (item["quantity"] as? Number)?.toInt() ?: 1
            val price = (item["price"] as? Number)?.toDouble() ?: 0.0
            val picUrl = item["picUrl"] as? String // Görsel URL'si

            // 1. Ürün Adı ve Adet
            binding.productName.text = name
            binding.productQuantity.text = "Adet: $quantity"

            // 2. Fiyat
            binding.productPrice.text = "${String.format(Locale.getDefault(), "%.2f", price)} TL"

            // 3. Görsel Yükleme (Glide)
            if (!picUrl.isNullOrEmpty()) {
                Glide.with(binding.productImage.context)
                    .load(picUrl)
                    // Hata veya yükleme sırasında göstereceği görseller
                    .placeholder(R.drawable.loading_placeholder)
                    .error(R.drawable.image_error)
                    .into(binding.productImage)
            } else {
                // Eğer URL yoksa, hata resmini göster
                binding.productImage.setImageResource(R.drawable.image_error)
            }
        } // 🔥 Parantez buraya taşındı ve kod düzeltildi
    }

    // ... (onCreateViewHolder, onBindViewHolder, getItemCount metotları aynı) ...

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemOrderProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(productList[position])
    }

    override fun getItemCount() = productList.size
}