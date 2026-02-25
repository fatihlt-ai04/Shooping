package com.levent.project2002.Adepter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.levent.project2002.R
import com.levent.project2002.databinding.ItemCouponProductImageBinding // ItemCouponProductImage.xml binding sınıfı

class ProductImageAdapter(private val imageUrls: List<String>) :
    RecyclerView.Adapter<ProductImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: ItemCouponProductImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemCouponProductImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val url = imageUrls[position]
        Glide.with(holder.binding.imgProductCoupon.context)
            .load(url)
            .error(R.drawable.ic_broken_image) // Hata ikonunuzu kullanın
            .into(holder.binding.imgProductCoupon)
    }

    override fun getItemCount() = imageUrls.size
}