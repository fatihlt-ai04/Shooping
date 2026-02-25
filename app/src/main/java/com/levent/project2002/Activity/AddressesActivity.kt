package com.levent.project2002.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.levent.project2002.Adepter.AddressAdapter
import com.levent.project2002.Adepter.AddressActionsListener // 🔥 DÜZELTME: Listener'ı doğru yoldan import ediyoruz
import com.levent.project2002.Model.AddressModel
import com.levent.project2002.databinding.ActivityAddressesBinding

// 🔥 SINIF TANIMI DÜZELTİLDİ: Artık hata vermemeli
class AddressesActivity : BaseActivity(), AddressActionsListener {

    private lateinit var binding: ActivityAddressesBinding
    private lateinit var adapter: AddressAdapter
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddressesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar ayarları
        setSupportActionBar(binding.toolbarAddresses)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.toolbarAddresses.setNavigationOnClickListener {
            finish()
        }

        setupRecyclerView()
        setupAddAddressButton()
    }

    // Activity tekrar görünür olduğunda listeyi yenile
    override fun onResume() {
        super.onResume()
        loadAddresses()
    }

    // ------------------------------------
    // METOTLAR
    // ------------------------------------
    private fun setupRecyclerView() {
        binding.rvAddresses.layoutManager = LinearLayoutManager(this)
        // Adaptörü başlatırken 'this' (aktivite) listener olarak gönderildi
        adapter = AddressAdapter(mutableListOf(), this)
        binding.rvAddresses.adapter = adapter
    }

    private fun loadAddresses() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Adresleri yüklemek için giriş yapılmalı.", Toast.LENGTH_SHORT).show()
            return
        }

        // Firestore'dan adresleri çekme
        db.collection("users")
            .document(userId)
            .collection("addresses")
            .get()
            .addOnSuccessListener { result ->
                val addressList = result.documents.map { document ->
                    AddressModel(
                        id = document.id,
                        title = document.getString("title") ?: "",
                        city = document.getString("city") ?: "",
                        district = document.getString("district") ?: "",
                        street = document.getString("street") ?: "",
                        buildingNo = document.getString("buildingNo") ?: "",
                        floor = document.getString("floor") ?: "",
                        apartmentNo = document.getString("apartmentNo") ?: ""
                    )
                }.toMutableList()

                adapter.updateList(addressList)
                if (addressList.isEmpty()) {
                    Toast.makeText(this, "Kayıtlı adres bulunmamaktadır.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Adresler yüklenemedi: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // ----------------------------------------------------
    // ADDRESSACTIONS LISTENER METOTLARI (ZORUNLU IMPLEMENTASYONLAR)
    // ----------------------------------------------------
    override fun onDeleteClicked(addressId: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("addresses")
            .document(addressId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Adres başarıyla silindi.", Toast.LENGTH_SHORT).show()

                // 🔥 KRİTİK DEĞİŞİKLİK: Adaptördeki anlık kaldırma metodunu çağır
                adapter.removeItemById(addressId)

                // Artık loadAddresses() çağırmaya gerek yok, bu sayede hemen silinmiş gibi görünür.
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Silme başarısız: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
    override fun onEditClicked(address: AddressModel) {
        // Düzenleme aktivitesine yönlendirme
        val intent = Intent(this, ManualAddressEntryActivity::class.java).apply {
            // Düzenleme modunu ve adresi belirtmek için ID'yi gönderiyoruz
            putExtra("EXTRA_EDIT_MODE", true)
            putExtra("EXTRA_ADDRESS_ID", address.id)
        }
        startActivity(intent)
    }
    // ----------------------------------------------------

    private fun setupAddAddressButton() {
        binding.btnAddAddress.setOnClickListener {
            navigateToAddressSelection()
        }
    }
// AddressesActivity.kt içine eklenecek metot:

    fun onAddressSelected(address: AddressModel) {
        Toast.makeText(this, "${address.title} seçildi.", Toast.LENGTH_SHORT).show()

        val resultIntent = Intent().apply {
            putExtra("EXTRA_SELECTED_ADDRESS_ID", address.id)
        }
        setResult(RESULT_OK, resultIntent)

        finish()
    }
    private fun navigateToAddressSelection() {
        val intent = Intent(this, AddressSelectionActivity::class.java)
        startActivity(intent)
    }
}