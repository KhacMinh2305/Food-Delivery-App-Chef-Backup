package data.model

data class Order(
    val addressId: Int,
    val amount: Double,
    val deliveryVehicle: String,
    val orderId: String,
    val orderedDate: String,
    val paymentMethod: String,
    val products: List<OrderProduct>, // List of OrderProduct objects
    val restaurantId: Int,
    val shipperId: Int,
    val status: Int,
    val userId: Int
)
