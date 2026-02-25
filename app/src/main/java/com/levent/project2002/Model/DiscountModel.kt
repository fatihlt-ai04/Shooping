package com.levent.project2002.Model

import java.io.Serializable // ❗ Bu satırı ekleyin

data class DiscountModel(
    val title: String,          // Kupon başlığı (Örn: Powertec Kişisel Bakım Aletlerinde S...)
    val discountValue: String,  // İndirim değeri (Örn: %5 TRY, 100 TL)
    val altLimit: Double,       // Alt Limit (Örn: 500 TL)
    val maxDiscount: Double?,   // Maksimum İndirim Tutarı (Opsiyonel)
    val expiryDate: String,     // Son Kullanma Tarihi (Örn: 16.12.2025)
    val products: List<String>, // Kuponun geçerli olduğu ürünlerin görsel URL'leri
    val isLimited: Boolean = false ,// Süreli teklif mi (Örn: Son 3 gün)
    // 🔥 EKLENEN KRİTİK ALANLAR
    val targetId: String,       // Filtreleme için kullanılacak ID (Kategori ID'si veya Kampanya Adı)
    val targetType: String      // "CATEGORY", "PRODUCT", "DEAL" gibi tipler
) : Serializable // ❗ Bu arayüzü uygulayın