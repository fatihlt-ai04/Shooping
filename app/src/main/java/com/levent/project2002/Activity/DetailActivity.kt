package com.levent.project2002.Activity

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide

import com.levent.project2002.Adepter.PicAdepter
import com.levent.project2002.Adepter.SelectModelAdepter
import com.levent.project2002.Helper.ManagmentCart
import com.levent.project2002.Model.ItemsModel
import com.levent.project2002.databinding.ActivityDetailBinding

class DetailActivity : BaseActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var item: ItemsModel
    private lateinit var managmentCart: ManagmentCart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        managmentCart = ManagmentCart(this)

        getBundle()
        initList()
    }

    private fun initList() {

        // MODELLER
        binding.modelList.apply {
            adapter = SelectModelAdepter(item.model)
            layoutManager = LinearLayoutManager(
                this@DetailActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }

        // FOTOĞRAFLAR
        Glide.with(this)
            .load(item.picUrl[0])
            .into(binding.img)

        binding.picList.apply {
            adapter = PicAdepter(item.picUrl) { selectedImageUrl ->
                Glide.with(this@DetailActivity)
                    .load(selectedImageUrl)
                    .into(binding.img)
            }
            layoutManager = LinearLayoutManager(
                this@DetailActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
    }

    private fun getBundle() {

        item = intent.getParcelableExtra("object")!!

        binding.titleTxt.text = item.title
        binding.descriptionTxt.text = item.description
        binding.priceTxt.text = "$" + item.price
        binding.ratingTxt.text = "${item.rating} Rating"

        // 🛒 ADD TO CART (ZATEN ÇALIŞAN KODUN)
        binding.addToCartBtn.setOnClickListener {
            managmentCart.insertFood(item)

        }

        // ⭐⭐⭐ BURASI YENİ – BUY NOW TIKLANINCA SEPETE EKLE + CART'A GİT
        binding.addToCartBtn.setOnClickListener {
            managmentCart.insertFood(item)
            // sepete ekle

            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)            // sepete git
        }
        // ⭐⭐⭐

        binding.backBtn.setOnClickListener { finish() }

        binding.cartBtn.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }
    }
}
