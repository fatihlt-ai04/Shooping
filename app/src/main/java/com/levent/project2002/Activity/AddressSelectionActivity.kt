package com.levent.project2002.Activity

import android.content.Intent
import android.os.Bundle
import com.levent.project2002.databinding.ActivityAddressSelectionBinding

class AddressSelectionActivity : BaseActivity() {

    private lateinit var binding: ActivityAddressSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddressSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        // 1. Haritadan Konum Seç
        binding.btnMapSelection.setOnClickListener {
            val intent = Intent(this, MapSelectionActivity::class.java)
            startActivity(intent)
            finish() // Seçim yapıldıktan sonra bu ekranı kapatmak isteyebilirsin
        }

        // 2. Adresi Elle Gir
        binding.btnManualEntry.setOnClickListener {
            // ManualAddressEntryActivity adlı yeni bir aktiviteye yönlendiririz
            // Bu aktivitede kullanıcı form doldurur.
            val intent = Intent(this, ManualAddressEntryActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}