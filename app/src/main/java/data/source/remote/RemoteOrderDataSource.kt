package data.source.remote
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.firestore
import data.model.Order
import data.model.OrderProduct
import kotlinx.coroutines.tasks.await
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

    private var eventTriggered = false
    private val handler = Handler(Looper.getMainLooper())
    private fun triggerEvent(addingEvent : () -> Unit) {
        handler.postDelayed({
            addingEvent()
            eventTriggered = false
        }, 10000)
    }

    private var firstObservation = true
    fun listenIncomingOrder(restaurantId: Int, onEventTriggered : () -> Unit) {
        firebaseDb.collection(ORDER_NODE).document(restaurantId.toString()).collection(RESTAURANT_ORDER_NODE).addSnapshotListener { snapshots, error ->
            if(firstObservation) {
                firstObservation = false
                return@addSnapshotListener
            }
            if(error != null) {
                Log.e("OrderDataSource_receiveOrderFromUser", error.toString())
                return@addSnapshotListener
            }
            var shouldUpdate = false
            for(document in snapshots!!.documentChanges) {
                if(document.type == DocumentChange.Type.ADDED || document.type == DocumentChange.Type.MODIFIED) {
                    shouldUpdate = true
                    break
                }
            }
            if(shouldUpdate && !eventTriggered) {
                eventTriggered = true
                triggerEvent(onEventTriggered)
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