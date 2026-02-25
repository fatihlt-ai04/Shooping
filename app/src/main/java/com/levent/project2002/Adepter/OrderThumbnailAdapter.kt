package com.levent.project2002.Adepter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.levent.project2002.databinding.ItemOrderThumbnailBinding

class OrderThumbnailAdapter(
    private val cartItems: List<Map<String, Any>>
) : RecyclerView.Adapter<OrderThumbnailAdapter.ThumbnailViewHolder>() {

    inner class ThumbnailViewHolder(val binding: ItemOrderThumbnailBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailViewHolder {
        return ThumbnailViewHolder(
            ItemOrderThumbnailBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ThumbnailViewHolder, position: Int) {
        val item = cartItems[position]
        val context = holder.binding.root.context

        val picUrl = item["picUrl"] as? String
        val quantity = (item["quantity"] as? Long)?.toInt() ?: 1

        Glide.with(context)
            .load(picUrl)
            .into(holder.binding.imgProductThumb)

        // Adet sayısını göster

    }

    override fun getItemCount() = cartItems.size
}