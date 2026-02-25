package com.levent.project2002.Adepter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.levent.project2002.Activity.DetailActivity
import com.levent.project2002.Model.ItemsModel
import com.levent.project2002.databinding.ViewholderRecommendedBinding

class RecommendedAdepter(
    private val items: MutableList<ItemsModel>
) : RecyclerView.Adapter<RecommendedAdepter.Viewholder>() {

    class Viewholder(val binding: ViewholderRecommendedBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val PREF_NAME = "wishlist_pref"
    private val KEY_WISHLIST = "wishlist_items"
    private val gson = Gson()

    // ---- WISHLIST GET/SET ----
    private fun getWishlist(context: Context): MutableList<ItemsModel> {
        val shared = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = shared.getString(KEY_WISHLIST, null) ?: return mutableListOf()

        val type = object : TypeToken<MutableList<ItemsModel>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun saveWishlist(context: Context, list: MutableList<ItemsModel>) {
        val shared = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        shared.edit().putString(KEY_WISHLIST, gson.toJson(list)).apply()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val binding = ViewholderRecommendedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        with(holder.binding) {

            // --- ÜRÜN VERİLERİ ---
            titleTxt.text = item.title
            ratingTxt.text = item.rating.toString()
            priceTxt.text = "$${item.price}"

            Glide.with(context)
                .load(item.picUrl.firstOrNull())
                .into(pic)

            // --- KALP BUTONU DURUMU ---
            favoriteIconBtn.setImageResource(
                if (item.isLiked) com.levent.project2002.R.drawable.ic_heart_filled
                else com.levent.project2002.R.drawable.ic_heart
            )

            // ---- DETAY SAYFASI ----
            root.setOnClickListener {
                val intent = Intent(context, DetailActivity::class.java)
                intent.putExtra("object", item)
                ContextCompat.startActivity(context, intent, null)
            }

            // ---- KALP TIKLANINCA ----
            favoriteIconBtn.setOnClickListener {

                val wishlist = getWishlist(context)

                item.isLiked = !item.isLiked

                if (item.isLiked) {
                    wishlist.add(item)
                } else {
                    wishlist.removeAll { it.title == item.title }
                }

                saveWishlist(context, wishlist)

                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
