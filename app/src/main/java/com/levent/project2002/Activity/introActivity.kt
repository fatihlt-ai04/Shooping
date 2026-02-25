package com.levent.project2002.Activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.levent.project2002.R
import com.levent.project2002.databinding.ActivityIntroBinding

class IntroActivity : BaseActivity() {
    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {

            // ✔ Let's Get Started → RegisterActivity
            startBtn.setOnClickListener {
                val intent = Intent(this@IntroActivity, RegisterActivity::class.java)
                startActivity(intent)
            }

            // ✔ Sign In → LoginActivity
            signInTxt.setOnClickListener {
                val intent = Intent(this@IntroActivity, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        enableEdgeToEdge()
    }
}
