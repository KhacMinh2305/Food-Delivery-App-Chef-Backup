package data.source.remote
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import data.model.Order
import data.model.OrderProduct
import kotlinx.coroutines.tasks.await

class OrderPagingSource(private val query : Query) : PagingSource<DocumentSnapshot, Order>() {

    private var currentKey : DocumentSnapshot? = null

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

    override fun getRefreshKey(state: PagingState<DocumentSnapshot, Order>): DocumentSnapshot? {
        return currentKey
    }

    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, Order> {
        val rawData = if(currentKey == null) query.limit(20).get().await() else query.endBefore(currentKey).limit(20).get().await()
        currentKey = if(rawData.isEmpty) null else rawData.documents.last()
        return LoadResult.Page(rawData.documents.map { doc ->
            mappingOrder(doc.data as Map<String, Any>)
        }, null, currentKey, LoadResult.Page.COUNT_UNDEFINED, LoadResult.Page.COUNT_UNDEFINED)
    }
}