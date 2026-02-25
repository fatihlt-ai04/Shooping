package com.levent.project2002.Activity

import android.os.Bundle
import android.widget.Toast
import com.levent.project2002.databinding.ActivityManualAddressEntryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
// 🔥 Artık Google Geocoder'a ihtiyacımız yok

class ManualAddressEntryActivity : BaseActivity() {

    private lateinit var binding: ActivityManualAddressEntryBinding

    // Firebase bağlantıları
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    companion object {
        const val EXTRA_LATITUDE = "EXTRA_LATITUDE"
        const val EXTRA_LONGITUDE = "EXTRA_LONGITUDE"
        const val EXTRA_ADDRESS_TEXT = "EXTRA_ADDRESS_TEXT"
    }

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var fullAddressText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManualAddressEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar ayarları
        setSupportActionBar(binding.toolbarManualAddress)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbarManualAddress.setNavigationOnClickListener { finish() }

        // Haritadan gelen veri var mı kontrol et ve alanları doldur
        checkForMapData()

        // Kaydet butonu
        setupSaveButton()
    }

    // ----------------------------------------------------
    // HARİTADAN GELEN VERİYİ İŞLEME (GÜNCELLENDİ)
    // ----------------------------------------------------
    private fun checkForMapData() {
        // Double'lar için varsayılan değer 0.0
        latitude = intent.getDoubleExtra(EXTRA_LATITUDE, 0.0)
        longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0)
        fullAddressText = intent.getStringExtra(EXTRA_ADDRESS_TEXT)

        // Geçerli koordinatlar varsa ve adres metni gelmişse
        if (latitude != 0.0 && longitude != 0.0 && !fullAddressText.isNullOrEmpty()) {

            // Adres metnini (Nominatim'dan gelen display_name) Sokak/Cadde alanına yerleştir.
            // Kullanıcıdan İl/İlçe gibi diğer detayları düzeltmesi istenir.
            binding.etStreet.setText(fullAddressText)

            Toast.makeText(this, "Konumdan Adres Alındı. Detayları Düzeltin.", Toast.LENGTH_LONG).show()

            // Kullanıcının dikkatini başlık alanına çekebiliriz
            binding.etAddressTitle.requestFocus()

        } else if (latitude != 0.0 && longitude != 0.0) {
            // Koordinat geldi ama adres metni gelmediyse (Nadir olabilir)
            Toast.makeText(this, "Konumdan adres metni alınamadı. Lütfen manuel doldurun.", Toast.LENGTH_LONG).show()
        }

        // Düzenleme modu için gerekliyse burada kontrol edilebilir
        // Örneğin: intent.getBooleanExtra("EXTRA_EDIT_MODE", false)
    }

    // ----------------------------------------------------
    // KAYIT VE DOĞRULAMA
    // ----------------------------------------------------
    private fun setupSaveButton() {
        binding.btnSaveAddress.setOnClickListener {
            if (validateForm()) {
                saveAddressToFirebase()
            } else {
                Toast.makeText(this, "Lütfen tüm gerekli alanları doldurun.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateForm(): Boolean {
        // Basit alan kontrolü
        return binding.etCity.text?.isNotEmpty() == true &&
                binding.etDistrict.text?.isNotEmpty() == true &&
                binding.etStreet.text?.isNotEmpty() == true &&
                binding.etBuildingNo.text?.isNotEmpty() == true &&
                binding.etAddressTitle.text?.isNotEmpty() == true
    }

    private fun saveAddressToFirebase() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Kullanıcı oturumu açık değil. Giriş yapın.", Toast.LENGTH_LONG).show()
            return
        }

        val addressData = hashMapOf(
            "title" to binding.etAddressTitle.text.toString(),
            "city" to binding.etCity.text.toString(),
            "district" to binding.etDistrict.text.toString(),
            "neighborhood" to binding.etNeighborhood.text.toString(), // Mahalle
            "street" to binding.etStreet.text.toString(), // Sokak/Cadde
            "buildingNo" to binding.etBuildingNo.text.toString(),
            "floor" to binding.etFloor.text.toString(),
            "apartmentNo" to binding.etApartmentNo.text.toString(),
            "latitude" to latitude, // Haritadan gelen son değer
            "longitude" to longitude, // Haritadan gelen son değer
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(userId)
            .collection("addresses")
            .add(addressData)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Adresiniz Başarıyla Kaydedildi!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Kaydetme Başarısız: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}