package data.model

data class OrderProduct(
    val imageUrl: String,
    val price: Double,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val size: Double,
    val sizeUnit: String,
    val skuId: Int
)
