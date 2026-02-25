package com.levent.project2002.Adepter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.levent.project2002.R
import com.levent.project2002.databinding.ItemShipmentEventBinding
import com.levent.project2002.Activity.ShipmentEvent
import android.graphics.Typeface // 🔥 Typeface import edildi

class ShipmentTimelineAdapter(
    private val items: MutableList<ShipmentEvent>
) : RecyclerView.Adapter<ShipmentTimelineAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemShipmentEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: ShipmentEvent, position: Int) {
            binding.tvEventDescription.text = event.description

            // Tarih ve Saati birleştir
            binding.tvEventTime.text = "${event.date} - ${event.time}"

            // İlk öğe ve son öğe için çizgi görünürlüğünü ayarla
            binding.timelineLineTop.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
            binding.timelineLineBottom.visibility = if (position == itemCount - 1) View.INVISIBLE else View.VISIBLE

            // Aktif/Tamamlanmış durumu görselleştirme
            val tickDrawable = if (event.isCompleted) {
                // Aktif renkli nokta
                R.drawable.timeline_dot_active
            } else {
                // Gri nokta
                R.drawable.timeline_dot_pending
            }
            binding.timelineTick.background = ContextCompat.getDrawable(itemView.context, tickDrawable)

            // 🔥 YAZI TİPİ DÜZELTİLDİ: textStyle yerine setTypeface kullanıldı
            if (position == 0) {
                // En üstteki event (En yeni olay) daha kalın yazı
                binding.tvEventDescription.setTypeface(null, Typeface.BOLD)
                binding.tvEventTime.setTypeface(null, Typeface.BOLD)
                binding.tvEventTime.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
            } else {
                // Diğer eventler normal yazı
                binding.tvEventDescription.setTypeface(null, Typeface.NORMAL)
                binding.tvEventTime.setTypeface(null, Typeface.NORMAL)
                binding.tvEventTime.setTextColor(ContextCompat.getColor(itemView.context, R.color.darkGrey))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemShipmentEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    // TrackingActivity'den kolayca event eklemek için
    fun addEvent(event: ShipmentEvent) {
        // En yeni event'i en üste (0. index'e) ekler
        items.add(0, event)
        notifyItemInserted(0)

        // Eğer ilk item değiştiği için yazı stilinin de güncellenmesi gerekiyorsa
        // eski 0. index (yeni 1. index) güncellenmeli.
        if (items.size > 1) {
            notifyItemChanged(1)
        }
    }
}