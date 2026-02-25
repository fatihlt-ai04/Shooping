package com.levent.project2002.Activity

import android.content.Intent
import android.os.Bundle
import com.levent.project2002.databinding.ActivityOrderSuccessBinding

class OrderSuccessActivity : BaseActivity() {

    private lateinit var binding: ActivityOrderSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ana sayfaya git butonu
        binding.btnGoHome.setOnClickListener {
            val intent = Intent(this@OrderSuccessActivity, MainActivity::class.java)
            startActivity(intent)
            finish() // OrderSuccessActivity kapanacak
        }

        // Siparişlerim sayfasına git butonu
        binding.btnViewOrders.setOnClickListener {
            val intent = Intent(this@OrderSuccessActivity, OrdersActivity::class.java)
            startActivity(intent)
            finish() // OrderSuccessActivity kapanacak
        }
    }
}
