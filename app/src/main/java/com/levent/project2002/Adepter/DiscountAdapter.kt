package com.levent.project2002.Adepter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.levent.project2002.Activity.ListItemsActivity // Yönlendirme hedefi
import com.levent.project2002.Helper.ManagmentCart // 🔥 ManagmentCart sınıfını import et
import com.levent.project2002.Model.DiscountModel
import com.levent.project2002.databinding.ItemDiscountCouponBinding

class DiscountAdapter(private val discountList: List<DiscountModel>) :
    RecyclerView.Adapter<DiscountAdapter.DiscountViewHolder>() {

    inner class DiscountViewHolder(val binding: ItemDiscountCouponBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DiscountModel) {
            val context = binding.root.context
            // 🔥 ManagmentCart örneğini oluştur (TinyDB'ye erişmek için)
            val managmentCart = ManagmentCart(context)

            binding.tvCouponTitle.text = item.title
            binding.tvDiscountValue.text = item.discountValue

            // Alt Limit ve Maksimum İndirim
            val maxDiscountText = item.maxDiscount?.let { " | Maks. İndirim: ${String.format("%.2f", it)} TL" } ?: ""
            binding.tvAltLimit.text = "Alt Limit: ${String.format("%.2f", item.altLimit)} TL$maxDiscountText"

            binding.tvExpiryDate.text = "Son Kullanma Tarihi: ${item.expiryDate}"

            // Süreli teklif görünürlüğü
            binding.tvLimitedOffer.visibility = if (item.isLimited) ViewGroup.VISIBLE else ViewGroup.GONE

            // Ürün Görselleri RecyclerView'ı kurma
            binding.rvCouponProducts.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                // ProductImageAdapter'ın bu adaptör içinde import edildiğinden emin olun
                adapter = ProductImageAdapter(item.products)
            }

            // 🔥 Ürünleri Gör butonuna tıklama dinleyicisi (Kuponu Kaydet ve Listeye Git)
            binding.btnUrunleriGor.setOnClickListener {

                // 1. Kuponu TinyDB'ye kaydet (Bu metodu ManagmentCart'a ekleyeceğiz)
                // managmentCart.applyCoupon(item) // Bu satır ManagmentCart'ta metot eklendikten sonra kullanılacak

                // 2. Kuponun hedeflediği ürün listesini aç
                if (item.targetType == "CATEGORY") {
                    val intent = Intent(context, ListItemsActivity::class.java).apply {
                        // ListItemsActivity'de filtrelenecek kategori ID'sini gönder
                        putExtra("id", item.targetId)
                        putExtra("title", item.title)
                    }
                    context.startActivity(intent)

                    // Kuponun uygulandığını kullanıcıya bildir
                    Toast.makeText(context, "${item.title} uygulandı! Sepete eklenen uygun ürünlerde indirim görünecektir.", Toast.LENGTH_LONG).show()

                } else {
                    // Diğer tipteki kuponlar için kullanıcıya bilgilendirme
                    Toast.makeText(context, "Bu kupon şu an uygulanamıyor.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscountViewHolder {
        val binding = ItemDiscountCouponBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DiscountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiscountViewHolder, position: Int) {
        holder.bind(discountList[position])
    }

    override fun getItemCount() = discountList.size
}