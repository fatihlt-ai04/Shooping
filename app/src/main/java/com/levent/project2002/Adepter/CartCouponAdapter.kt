package com.levent.project2002.Adepter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.levent.project2002.Model.DiscountModel
import com.levent.project2002.databinding.ItemCartCouponCardBinding // Yeni Layout
import com.levent.project2002.Helper.ManagmentCart

// Kupon seçildiğinde CartActivity'ye haber veren listener
interface CouponSelectListener {
    fun onCouponApplied(coupon: DiscountModel)
}

class CartCouponAdapter(
    private val couponList: List<DiscountModel>,
    private val listener: CouponSelectListener
) : RecyclerView.Adapter<CartCouponAdapter.CouponViewHolder>() {

    inner class CouponViewHolder(val binding: ItemCartCouponCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DiscountModel) {

            // Kupon detaylarını göster
            binding.tvDiscountTitle.text = item.title
            binding.tvDiscountValue.text = item.discountValue
            binding.tvAltLimit.text = "Min. ${item.altLimit} TL"

            // Kupon Kartına Tıklama İşlemi
            binding.couponCardContainer.setOnClickListener {
                // Kuponu uygula ve CartActivity'ye bildir
                listener.onCouponApplied(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponViewHolder {
        val binding = ItemCartCouponCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CouponViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CouponViewHolder, position: Int) {
        holder.bind(couponList[position])
    }

    override fun getItemCount() = couponList.size
}