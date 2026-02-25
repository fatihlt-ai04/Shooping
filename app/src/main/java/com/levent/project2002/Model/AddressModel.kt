package com.levent.project2002.Model // Paketinizi kontrol edin

data class AddressModel(
    val id: String = "",
    val title: String = "",      // Adres Başlığı (Ev/İş vb.)
    val city: String = "",       // İl
    val district: String = "",   // İlçe
    val neighborhood: String = "", // Mahalle
    val street: String = "",     // Sokak Adı
    val buildingNo: String = "", // Bina Numarası
    val floor: String = "",      // Kat
    val apartmentNo: String = "",// Daire Numarası
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isDefault: Boolean = false
)