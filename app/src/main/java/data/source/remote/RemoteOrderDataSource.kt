package data.source.remote
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.firestore
import domain.convertTimeIntoLocalTime
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
}