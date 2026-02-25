package com.levent.project2002.Adepter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.levent.project2002.Helper.ManagmentCart // Sadece sınıfı import et
import com.levent.project2002.Model.ItemsModel
import com.levent.project2002.databinding.ViewholderCartBinding

class CartAdapter(
    private var list: ArrayList<ItemsModel>,
    private val managmentCart: ManagmentCart,
    private val changeListener: () -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    class ViewHolder(val binding: ViewholderCartBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ViewholderCartBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.binding.titleTxt.text = item.title
        holder.binding.feeEachItemTxt.text = "$${item.price}"
        holder.binding.numberItemTxt.text = item.numberInCart.toString()

        holder.binding.totalEachItemTxt.text =
            "$${String.format("%.2f", item.numberInCart * item.price)}"

        // Ürün resmi yükleme
        Glide.with(holder.itemView.context)
            .load(item.picUrl[0])
            .into(holder.binding.pic)

        // ➕ PLUS
        holder.binding.plusCartBtn.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                // 🔥 DÜZELTME: ManagmentCart.ChangeNumberItemsListener olarak çağrıldı
                managmentCart.plusItem(list, pos, object : ManagmentCart.ChangeNumberItemsListener {
                    override fun onChanged() {
                        notifyItemChanged(pos)
                        changeListener()
                    }
                })
            }
        }

        // ➖ MINUS
        holder.binding.minusCartBtn.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                // 🔥 DÜZELTME: ManagmentCart.ChangeNumberItemsListener olarak çağrıldı
                managmentCart.minusItem(list, pos, object : ManagmentCart.ChangeNumberItemsListener {
                    override fun onChanged() {
                        notifyItemChanged(pos)
                        changeListener()
                    }
                })
            }
        }

        // 🗑️ DELETE BUTTON (Hepsiburada tarzı)
        holder.binding.deleteBtn.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {

                // SEPETTEN SİL
                list.removeAt(pos)
                managmentCart.saveList(list)

                notifyItemRemoved(pos)
                notifyItemRangeChanged(pos, list.size)

                changeListener()
            }
        }
    }
}