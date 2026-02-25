package com.levent.project2002.Activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels // viewModels delegasyonu için eklendi
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.levent.project2002.Adepter.ListItemsAdepter
import com.levent.project2002.databinding.ActivityListItemsBinding
import com.levent.project2002.viewmodel.MainViewModel

class ListItemsActivity : BaseActivity() {

    private lateinit var binding: ActivityListItemsBinding

    // ViewModel'i BaseActivity'den veya uygulama context'i ile başlatmak için 'by viewModels()' kullanılır.
    // Eğer ViewModel'iniz Application alıyorsa: private val viewModel: MainViewModel by viewModels() { MainViewModelFactory(application) }
    // Basit olması için by viewModels() varsayalım:
    private val viewModel: MainViewModel by viewModels()

    private var categoryId: String = "" // CategoryAdapter'dan gelen ID
    private var categoryTitle: String = "" // CategoryAdapter'dan gelen başlık

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getBundle()
        setupToolbar()
        initList()
    }

    private fun getBundle() {
        // CategoryAdapter'dan gelen verileri al
        categoryId = intent.getStringExtra("id") ?: ""
        categoryTitle = intent.getStringExtra("title") ?: "Ürün Listesi"

        // Başlık TextView'e (categoryTxt) başlığı ata
        binding.categoryTxt.text = categoryTitle

        if (categoryId.isEmpty()) {
            Toast.makeText(this, "Kategori ID bulunamadı.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupToolbar() {
        // Toolbar ID'nizin XML'de 'toolbarListItems' olduğunu varsayıyorum
        setSupportActionBar(binding.toolbarListItems)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = categoryTitle // Toolbar başlığını kategori adı yap
        binding.toolbarListItems.setNavigationOnClickListener { finish() }
    }


    private fun initList() {
        binding.progressBarList.visibility = View.VISIBLE

        // 1. Filtrelenmiş Ürün Listesini Gözlemle
        // ViewModel'in 'recommended' LiveData'sını (veya filtreleme için kullandığınız LiveData'yı) gözlemliyoruz.
        viewModel.recommended.observe(this, Observer { itemsList ->
            binding.progressBarList.visibility = View.GONE

            if (itemsList.isEmpty()) {
                // Ürün bulunamadıysa mesaj göster
                binding.tvEmptyMessage.visibility = View.VISIBLE
                binding.tvEmptyMessage.text = "${categoryTitle} kategorisinde ürün bulunmamaktadır."
                binding.viewList.visibility = View.GONE
            } else {
                binding.tvEmptyMessage.visibility = View.GONE
                binding.viewList.visibility = View.VISIBLE

                // 2. RecyclerView'ı Kur ve Adaptöre Veriyi Gönder
                binding.viewList.layoutManager = GridLayoutManager(this, 2)
                binding.viewList.adapter = ListItemsAdepter(itemsList)
            }
        })

        // 3. Filtreleme İşlemini Başlat
        // ViewModel'e category ID'sini göndererek Firebase sorgusunu tetikle
        if (categoryId.isNotEmpty()) {
            viewModel.loadFiltered(categoryId)
        }
    }
}