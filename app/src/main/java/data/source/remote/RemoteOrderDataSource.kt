package data.source.remote
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import data.model.Order
import data.model.OrderProduct
import domain.convertTimeIntoLocalTime
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteOrderDataSource @Inject constructor(
    private val authDataSource: AuthenticationDataSource
) {

    companion object {
        private const val ORDER_NODE = "order"
        private const val RESTAURANT_ORDER_NODE = "orders"
    }

    private val firebaseDb = Firebase.firestore
    private var isChefAction = false

    fun rejectOrder(orderId : String) {
        isChefAction = true
        // TODO : Update status here
    }

    fun listenIncomingOrder(restaurantId: Int) {
        val startObservingTime = LocalDateTime.now()
        firebaseDb.collection(ORDER_NODE).document(restaurantId.toString()).collection(RESTAURANT_ORDER_NODE).addSnapshotListener { snapshots, error ->
            if(error != null) {
                Log.e("OrderDataSource_receiveOrderFromUser", error.toString())
                return@addSnapshotListener
            }
            for(document in snapshots!!.documentChanges) {
                when(document.type) {
                    DocumentChange.Type.ADDED -> {
                        val orderTime = convertTimeIntoLocalTime((document.document.data["ordered_date"] as String))
                        if(orderTime!!.isBefore(startObservingTime)) {
                            return@addSnapshotListener
                        }
                        // TODO : Emit new order to UI here
                    }
                    DocumentChange.Type.MODIFIED -> {
                        if(isChefAction) { // prevent updating when action come from chef app
                            isChefAction = false
                            return@addSnapshotListener
                        }
                        // TODO : Update if this action come from user app
                        Log.d("TAG", "Modified state ${document.document.data["status"]}")
                    }
                    DocumentChange.Type.REMOVED -> Log.d("TAG", "Deleted order id : ${document.document.data["order_id"]}")
                }
            }
        }
    }

    private fun createOrderProductFromMap(data: Map<String, Any>): OrderProduct {
        return OrderProduct(
            imageUrl = data["image_url"] as String,
            price = (data["price"] as Double).toDouble(),
            productId = (data["product_id"] as Long).toInt(),
            productName = data["product_name"] as String,
            quantity = (data["quantity"] as Long).toInt(),
            size = (data["size"] as Double).toDouble(),
            sizeUnit = data["size_unit"] as String,
            skuId = (data["sku_id"] as Long).toInt()
        )
    }

    private fun mappingOrder(data : Map<String, Any>) : Order {
        val rawProduct = data["products"] as Map<String, Map<String,Any>>
        val orderProducts = rawProduct.map { product ->
            createOrderProductFromMap(product.value)
        }
        return Order(
            addressId = (data["address_id"] as Long).toInt(),
            amount = data["amount"].toString().toDouble(),
            deliveryVehicle = data["delivery_vehicle"] as String,
            orderId = data["order_id"] as String,
            orderedDate = data["ordered_date"] as String,
            paymentMethod = data["payment_method"] as String,
            products = orderProducts,
            restaurantId = (data["restaurant_id"] as Long).toInt(),
            shipperId = (data["shipper_id"] as Long).toInt(),
            status = (data["status"] as Long).toInt(),
            userId = (data["user_id"] as Long).toInt()
        )
    }

    suspend fun loadRestaurantOrder(restaurantId: Int) : List<Order>? {
        try {
            val rawData = firebaseDb.collection(ORDER_NODE).document(restaurantId.toString()).collection(RESTAURANT_ORDER_NODE).get().await()
            return rawData.map { doc ->
                mappingOrder(doc.data)
            }
        } catch (e : Exception) {
            Log.e("RemoteOrderDataSource_loadRestaurantOrder", e.toString())
            return null
        }
    }
}