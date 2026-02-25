package com.levent.project2002.Adepter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.levent.project2002.R
import com.levent.project2002.databinding.ViewholderPicBinding

class PicAdepter(
    private val items: MutableList<String>, // 'val' yerine 'private val' daha uygun
    private val onImageSelected: (String) -> Unit // lambda parametresini 'private' yapın
) : RecyclerView.Adapter<PicAdepter.Viewholder>() {

    // Seçim durumlarını takip eden değişkenler
    private var selectPosition: Int = -1
    private var lastSelectedPosition: Int = -1

    inner class Viewholder(val binding: ViewholderPicBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Viewholder { // Return tipi kısaltıldı
        val binding =
            ViewholderPicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val itemUrl = items[position]

        // Resim yükleme
        holder.binding.pic.loadImage(itemUrl)

        // --- DÜZELTME 1: Tıklama Dinleyicisinde holder.adapterPosition Kullanımı ---
        holder.binding.root.setOnClickListener {

            // Tıklama anındaki güncel pozisyonu alın
            val currentPosition = holder.adapterPosition

            if (currentPosition == RecyclerView.NO_POSITION) return@setOnClickListener // Geçersiz pozisyon kontrolü

            // Sadece yeni bir öğe seçiliyorsa işlemi yap
            if (selectPosition != currentPosition) {

                lastSelectedPosition = selectPosition
                selectPosition = currentPosition

                // Eski pozisyonu güncelle (seçimi kaldır)
                if (lastSelectedPosition != -1) {
                    notifyItemChanged(lastSelectedPosition)
                }

                // Yeni pozisyonu güncelle (seçimi yap)
                notifyItemChanged(selectPosition)

                // Seçilen resmi dışarı bildirin
                onImageSelected(items[currentPosition])
            }
        }

        // --- DÜZELTME 2: Seçim Durumu Mantığı ---
        // Seçim mantığı, sadece geçerli pozisyonu kontrol etmelidir.
        if (selectPosition == position) {
            // Seçili durum
            holder.binding.picLayout.setBackgroundResource(R.drawable.green_bg_selected)
        } else {
            // Seçili olmayan durum
            holder.binding.picLayout.setBackgroundResource(R.drawable.grey_bg)
        }
    }

    override fun getItemCount(): Int = items.size

    // Resim yükleme uzantı fonksiyonu (Doğru ve yerinde)
    fun ImageView.loadImage(url: String) {
        Glide.with(this.context)
            .load(url)
            .into(this)
    }
}