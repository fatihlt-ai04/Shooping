package com.levent.project2002.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.levent.project2002.databinding.ActivityAddProductBinding
import java.util.UUID

class AddProductActivity : BaseActivity() {

    private lateinit var binding: ActivityAddProductBinding
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var selectedImageUri: Uri? = null // Seçilen görselin URI'sini tutar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupListeners()
    }

    private fun setupToolbar() {
        // XML'de toolbar ID'nizin 'toolbarAddProduct' olduğunu varsayıyoruz
        setSupportActionBar(binding.toolbarAddProduct)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Yeni Ürün Ekle"
        binding.toolbarAddProduct.setNavigationOnClickListener { finish() }
    }

    private fun setupListeners() {
        // Görsel Seçme
        binding.btnSelectImage.setOnClickListener {
            pickImage.launch("image/*") // Resim seçme işlemini başlat
        }

        // Ürün Ekleme
        binding.btnAddProduct.setOnClickListener {
            validateAndUploadProduct()
        }
    }

    // Galeriye gidip resim seçme sonucu
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            binding.imgProductPreview.setImageURI(uri)
            binding.imgProductPreview.visibility = View.VISIBLE
        }
    }

    // 🔥 GÜNCELLENDİ: Stok verisi kontrolü eklendi
    private fun validateAndUploadProduct() {
        val title = binding.etTitle.text.toString().trim()
        val price = binding.etPrice.text.toString().toDoubleOrNull()
        val categoryId = binding.etCategoryId.text.toString().toIntOrNull()
        val description = binding.etDescription.text.toString().trim()
        // 🔥 YENİ: Stok miktarını al
        val stock = binding.etStock.text.toString().toIntOrNull()

        if (title.isEmpty() || price == null || categoryId == null || description.isEmpty() || selectedImageUri == null || stock == null) {
            Toast.makeText(this, "Lütfen tüm alanları (Stok dahil) doldurun ve bir görsel seçin.", Toast.LENGTH_LONG).show()
            return
        }

        // 1. Görseli Firebase Storage'a yükle
        // 🔥 Metot imzası güncellendi
        uploadImageToStorage(title, price, categoryId, description, stock)
    }

    // 🔥 GÜNCELLENDİ: Stok parametresi eklendi
    private fun uploadImageToStorage(title: String, price: Double, categoryId: Int, description: String, stock: Int) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnAddProduct.isEnabled = false

        val fileName = "images/${UUID.randomUUID()}" // Benzersiz dosya adı oluştur
        val imageRef = storage.reference.child(fileName)

        imageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                // Görsel yüklendikten sonra URL'yi al
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    // 2. Ürün verilerini Firestore'a kaydet
                    // 🔥 Metot imzası güncellendi
                    saveProductToFirestore(title, price, categoryId, description, imageUrl, stock)
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnAddProduct.isEnabled = true
                Toast.makeText(this, "Görsel yüklenirken hata: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // 🔥 GÜNCELLENDİ: Stok parametresi eklendi ve veriye dahil edildi
    private fun saveProductToFirestore(title: String, price: Double, categoryId: Int, description: String, imageUrl: String, stock: Int) {
        val productData = hashMapOf(
            "title" to title,
            "description" to description,
            "price" to price,
            "categoryId" to categoryId,
            "rating" to 5.0, // Varsayılan puan
            "showRecommended" to true, // Varsayılan
            "picUrl" to listOf(imageUrl), // Görseli tek elemanlı liste olarak kaydet
            "model" to listOf("Standart"), // Varsayılan model
            // 🔥 YENİ: Stok miktarını kaydet
            "stock" to stock
        )

        db.collection("Items")
            .add(productData)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                binding.btnAddProduct.isEnabled = true
                Toast.makeText(this, "Ürün başarıyla eklendi!", Toast.LENGTH_LONG).show()
                finish() // İşlem bitince aktiviteyi kapat
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnAddProduct.isEnabled = true
                Toast.makeText(this, "Ürün Firestore'a kaydedilirken hata: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}