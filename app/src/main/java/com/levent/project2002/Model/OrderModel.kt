package com.levent.project2002.Model

// OrderModel, Firestore'dan çekilen her bir sipariş dökümanını temsil eder
data class OrderModel(
    // Firestore Döküman ID'si
    val id: String,

    // Temel Sipariş Bilgileri
    val userId: String = "",
    val addressId: String = "",
    val orderDate: Long = 0L, // Zaman damgası
    val totalAmount: Double = 0.0,
    val paymentMethod: String = "",
    val status: String = "", // Örn: "Sipariş Hazırlanıyor", "Kargoda", "Teslim Edildi"

    // Sepet içeriği (Firestore'da HashMap listesi olarak kaydedildi)
    // Bu, her bir ürünün detaylarını içeren bir Map listesidir.
    val cartItems: List<Map<String, Any>> = emptyList()
)