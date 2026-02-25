// CartModel.kt (Varsayılan model yapısı)
data class CartModel(
    val id: Int, // veya Long/String (Bu alanın adının id olduğundan emin olun)
    val title: String,
    val fee: Double, // Fiyat alanı
    val numberInCart: Int,
    // ... diğer alanlar
)