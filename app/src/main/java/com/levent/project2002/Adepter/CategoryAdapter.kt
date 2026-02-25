package com.levent.project2002.Adepter

import android.content.Intent
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.levent.project2002.R
import com.levent.project2002.Model.CategoryModel
import com.levent.project2002.databinding.ViewholderCategoryBinding
import com.levent.project2002.Activity.ListItemsActivity

class CategoryAdapter(private val items: MutableList<CategoryModel>): // 'private' önerilir
    RecyclerView.Adapter<CategoryAdapter.Viewholder>() {

    private var selectPosition: Int = -1
    private var lastSelectedPosition: Int = -1

    inner class Viewholder (val binding: ViewholderCategoryBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Viewholder { // Return tipi kısaltıldı
        val binding = ViewholderCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        // 1. Veri Bağlama
        holder.binding.titleTxt.text = item.title ?: "Kategori"
        Glide.with(context)
            .load(item.picUrl)
            .into(holder.binding.pic)

        // 2. Seçim Durumu Mantığı (Görünüm Güncellemesi)
        if (selectPosition == position) {
            // Seçiliyse (Yeşil/Beyaz Tema)
            holder.binding.pic.setBackgroundResource(0)
            holder.binding.mainLayout.setBackgroundResource(R.drawable.green_button_bg)

            // Simge rengini beyaza ayarla
            ImageViewCompat.setImageTintList(
                holder.binding.pic,
                ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.white))
            )
            // Metin rengini beyaza ayarla
            holder.binding.titleTxt.setTextColor(
                ContextCompat.getColor(context, android.R.color.white)
            )
        } else {
            // Seçili değilse (Varsayılan Gri/Siyah Tema)
            holder.binding.pic.setBackgroundResource(R.drawable.grey_bg)
            holder.binding.mainLayout.setBackgroundResource(0)

            // Simge rengini siyaha ayarla
            ImageViewCompat.setImageTintList(
                holder.binding.pic,
                ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.black))
            )
            // Metin rengini siyaha ayarla
            holder.binding.titleTxt.setTextColor(
                ContextCompat.getColor(context, android.R.color.black)
            )
        }

        // 3. Tıklama Dinleyicisi
        holder.binding.root.setOnClickListener{

            // 🛑 KRİTİK DÜZELTME 5: Position değişkeni, holder.adapterPosition ile alınmalı.
            val currentPosition = holder.adapterPosition

            if (currentPosition != RecyclerView.NO_POSITION) {

                // Seçim mantığını sadece geçerli pozisyonlar için uygula
                lastSelectedPosition = selectPosition
                selectPosition = currentPosition

                // Eski ve yeni pozisyonları güncelle
                if (lastSelectedPosition != -1) notifyItemChanged(lastSelectedPosition)
                notifyItemChanged(selectPosition)

                // 🛑 KRİTİK DÜZELTME 6: Intent başlatma geciktirmesi kaldırıldı ve doğru başlatma metodu kullanıldı.
                // Gecikme olmadan Activity başlatma
                val intent = Intent(context, ListItemsActivity::class.java).apply {
                    putExtra("id", item.id.toString())
                    putExtra("title", item.title)
                }
                context.startActivity(intent) // Context'ten direkt olarak başlatıldı
            }
        }
    }

    override fun getItemCount(): Int = items.size
}