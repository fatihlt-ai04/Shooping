package com.levent.project2002.Adepter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.levent.project2002.Model.AddressModel
import com.levent.project2002.databinding.ItemAddressCardBinding
import android.widget.Toast
import com.levent.project2002.Activity.AddressesActivity // AddressesActivity'yi import et

// CALLBACK ARAYÜZÜNÜ TANIMLA (Değişmedi)
interface AddressActionsListener {
    fun onDeleteClicked(addressId: String)
    fun onEditClicked(address: AddressModel)
}

class AddressAdapter(
    private var addressList: MutableList<AddressModel>,
    private val listener: AddressActionsListener
) : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {

    inner class AddressViewHolder(val binding: ItemAddressCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(address: AddressModel) {

            binding.tvAddressTitle.text = address.title

            val detail = "${address.neighborhood}, ${address.street} Sk. No:${address.buildingNo}" +
                    (if (address.apartmentNo.isNotEmpty()) ", Daire No:${address.apartmentNo}" else "") +
                    ", ${address.city}"

            binding.tvAddressDetail.text = detail

            // Düzenle ikonuna tıklamayı listener'a yönlendir
            binding.iconEdit.setOnClickListener {
                Toast.makeText(binding.root.context, "${address.title} Düzenleniyor...", Toast.LENGTH_SHORT).show()
                listener.onEditClicked(address)
            }

            // Sil ikonuna tıklamayı listener'a yönlendir
            binding.iconDelete.setOnClickListener {
                Toast.makeText(binding.root.context, "${address.title} Silinecek...", Toast.LENGTH_SHORT).show()
                // 🔥 Adres silme işlemini aktiviteye bildir
                listener.onDeleteClicked(address.id)
            }

            // 🔥 YENİ KOD: TÜM KARTA TIKLAMA OLAYINI EKLE (Adres Seçimi)
            // Tıklamayı, konteksi AddressesActivity olan bir aktiviteye yönlendirir.
            binding.root.setOnClickListener {
                // Konteksin gerçekten AddressesActivity olup olmadığını kontrol et
                (binding.root.context as? AddressesActivity)?.onAddressSelected(address)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val binding = ItemAddressCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AddressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        holder.bind(addressList[position])
    }

    override fun getItemCount() = addressList.size

    // Firebase'den yeni veri geldiğinde listeyi yenilemek için kullanılır
    fun updateList(newList: MutableList<AddressModel>) {
        addressList = newList
        notifyDataSetChanged()
    }

    // SİLME İŞLEMİNDEN HEMEN SONRA ELEMANI KALDIR
    fun removeItemById(addressId: String) {
        val index = addressList.indexOfFirst { it.id == addressId }

        if (index != -1) {
            addressList.removeAt(index)
            notifyItemRemoved(index)

            if (index < addressList.size) {
                notifyItemRangeChanged(index, addressList.size - index);
            }
        }
    }
}