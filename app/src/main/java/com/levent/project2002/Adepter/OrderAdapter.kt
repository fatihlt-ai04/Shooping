package com.levent.project2002.Adepter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.levent.project2002.Model.OrderModel
import com.levent.project2002.databinding.ItemOrderCardBinding
import com.levent.project2002.Activity.OrderDetailActivity
import com.levent.project2002.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderAdapter(
    private var orderList: MutableList<OrderModel>
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    // Tarih formatlama aracı (Örn: 8 Aralık 2025)
    private val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("tr", "TR"))

    inner class OrderViewHolder(val binding: ItemOrderCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Alt adaptör (yatay ürün görselleri için)
        private lateinit var thumbnailAdapter: OrderThumbnailAdapter

        fun bind(order: OrderModel) {
            val context = binding.root.context
            val cartItems = order.cartItems

            // 1. Üst Bilgi (Tarih ve Toplam Tutar)
            val formattedDate = dateFormat.format(Date(order.orderDate))
            binding.tvOrderDate.text = formattedDate
            binding.tvOrderTotalAmount.text = "Toplam: ${String.format("%.2f", order.totalAmount)} TL"

            // 2. Sipariş Durumu
            binding.tvOrderStatus.text = order.status
            setupOrderStatus(order.status, context)

            // 🔥 3. ÇOKLU ÜRÜN GÖRSEL LİSTESİNİ BAŞLAT (rvThumbnails)

            val totalItems = cartItems.sumOf { (it["quantity"] as? Long)?.toInt() ?: 1 }

            // Alt RecyclerView için Adapter'ı kur
            thumbnailAdapter = OrderThumbnailAdapter(cartItems)
            binding.rvThumbnails.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = thumbnailAdapter
            }

            // Toplam ürün adedini göster
            binding.tvItemCountSummary.text = "Toplam $totalItems ürün teslim edildi"


            // 4. Aksiyon Butonları (Detaylar ve Değerlendir)

            // Değerlendir butonu sadece "Teslim Edildi" ise görünür
            if (order.status == "Teslim Edildi") {
                binding.btnRateOrder.visibility = View.VISIBLE
                binding.btnRateOrder.setOnClickListener {
                    Toast.makeText(context, "${order.id} siparişini değerlendir.", Toast.LENGTH_SHORT).show()
                }
            } else {
                binding.btnRateOrder.visibility = View.GONE
            }

            // Detaylar butonunun işlevi (Tüm durumlarda aktif)
            binding.btnDetails.setOnClickListener {
                val intent = Intent(context, OrderDetailActivity::class.java).apply {
                    putExtra("EXTRA_ORDER_ID", order.id)
                }
                context.startActivity(intent)
            }
        }

        // Sipariş durumuna göre renk ve ikon ayarı
        private fun setupOrderStatus(status: String, context: Context) {
            val color: Int
            val icon: Int

            when (status) {
                "Teslim Edildi" -> {
                    color = ContextCompat.getColor(context, R.color.green)
                    icon = R.drawable.ic_check
                }
                "İade Edildi" -> {
                    color = ContextCompat.getColor(context, R.color.red)
                    icon = R.drawable.ic_return_order
                }
                "Sipariş Hazırlanıyor" -> {
                    color = ContextCompat.getColor(context, R.color.orange)
                    icon = R.drawable.ic_info
                }
                else -> {
                    color = ContextCompat.getColor(context, R.color.darkGrey)
                    icon = R.drawable.ic_info
                }
            }

            binding.tvOrderStatus.setTextColor(color)
            binding.tvOrderStatus.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orderList[position])
    }

    override fun getItemCount() = orderList.size

    fun updateList(newList: MutableList<OrderModel>) {
        orderList = newList
        notifyDataSetChanged()
    }
}