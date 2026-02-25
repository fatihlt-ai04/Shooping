package com.levent.project2002.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.levent.project2002.Activity.BaseActivity
import com.levent.project2002.databinding.ActivityProfileBinding

class ProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        displayUserInfo()
        setupLogoutButton()

        // 🆕 YENİ: Menü tıklamalarını ayarla
        setupMenuListeners()
        binding.llDiscountsAndOffers.setOnClickListener {
            val intent = Intent(this, OffersActivity::class.java)
            startActivity(intent)
        }
    }

    private fun displayUserInfo() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            binding.tvUserEmail.text = currentUser.email ?: "E-posta bulunamadı"
        } else {
            Toast.makeText(this, "Oturum açık değil.", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }

    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            auth.signOut()

            Toast.makeText(this, "Başarıyla çıkış yapıldı.", Toast.LENGTH_SHORT).show()

            navigateToLogin()
        }
    }

    // 🆕 YENİ: Menü Tıklama Dinleyicilerini Ayarlama Metodu
    private fun setupMenuListeners() {
        // 1. ADRESLERİM Tıklaması
        binding.clAddresses.setOnClickListener {
            val intent = Intent(this, AddressesActivity::class.java)
            startActivity(intent)
        }

        // 2. SİPARİŞLERİM Tıklaması
        binding.clOrders.setOnClickListener {
            val intent = Intent(this, OrdersActivity::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}