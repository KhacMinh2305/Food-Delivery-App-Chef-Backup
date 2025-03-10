package ui.viewmodel.home
import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import data.model.Order
import data.model.Result
import data.repo.AuthenticationRepository
import data.repo.OrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ui.model.RunningOrder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepo : AuthenticationRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    // MutableLiveData for internal updates
    private val _runningOrders = MutableLiveData<Int>()
    private val _requestOrders = MutableLiveData<Int>()
    private val _totalRevenue = MutableLiveData<Float>()
    private val _graphData = MutableLiveData<List<Double>>()
    private val _rating = MutableLiveData<Float>()
    private val _totalReviews = MutableLiveData<String>()
    private val _runningOrdersData = MutableLiveData<List<RunningOrder>>()

    // Expose LiveData for the UI to observe
    val runningOrders: LiveData<Int> = _runningOrders
    val requestOrders: LiveData<Int> = _requestOrders
    val totalRevenue: LiveData<Float> = _totalRevenue
    val graphData: LiveData<List<Double>> = _graphData
    val rating: LiveData<Float> = _rating
    val totalReviews: LiveData<String> = _totalReviews
    val runningOrdersData: LiveData<List<RunningOrder>> = _runningOrdersData

    fun initialize(restaurantId: Int) {
        loadRestaurantOrders(restaurantId)
        listenIncomingOrder(restaurantId)
    }

    private fun listenIncomingOrder(restaurantId: Int) {
        orderRepository.listenIncomingOrder(restaurantId) {
            loadRestaurantOrders(restaurantId)
            listenIncomingOrder(restaurantId)
        }
    }

    private fun loadRestaurantOrders(restaurantId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = orderRepository.loadRestaurantOrder(restaurantId)) {
                is Result.Success -> {
                    val data = result.data as List<Order>
                    processOrders(filterTodayOrders(data))
                }
                else -> {
                    // TODO : Notify error here
                }
            }
        }
    }

    private fun filterTodayOrders(dataSet : List<Order>) : List<Order> {
        val today = LocalDate.now()
        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        // Filter orders to include only those from today
        val todayOrders = dataSet.filter { order ->
            try {
                val orderDate = LocalDateTime.parse(order.orderedDate, dateFormat)
                orderDate.toLocalDate().isEqual(today)
            } catch (e: Exception) {
                Log.e("HomeViewModel_filterTodayOrders", e.toString())
                false
            }
        }
        return todayOrders
    }

    private fun calculateGraphData(orders: List<Order>): List<Double> {
        val hourBuckets = MutableList(24) { 0.0 }
        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        orders.forEach { order ->
            try {
                val date = LocalDateTime.parse(order.orderedDate, dateFormat)
                date?.let {
                    val hour = date.hour
                    hourBuckets[hour] = hourBuckets[hour] + order.amount
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel_calculateGraphData", e.toString())
            }
        }
        return hourBuckets
    }

    private suspend fun processOrders(orders: List<Order>) {
        val runningOrdersCount = orders.count { it.status == 1 || it.status == 2 }
        val requestOrdersCount = orders.count { it.status == 0 }
        val totalRevenueValue = orders.sumOf { it.amount }
        val graphData = calculateGraphData(orders)
        val runningOrderData = mapToRunningOrders(orders)
        withContext(Dispatchers.Main) {
            _runningOrders.value = runningOrdersCount
            _requestOrders.value = requestOrdersCount
            _totalRevenue.value = totalRevenueValue.toFloat()
            _graphData.value = graphData
            _rating.value = 4.8f
            _totalReviews.value = "Total 23 reviews"
            _runningOrdersData.value = runningOrderData
        }
    }

    @SuppressLint("DefaultLocale")
    private fun mapToRunningOrders(orders: List<Order>): List<RunningOrder> {
        return orders.filter { it.status == 1 || it.status == 2 }
            .map { order ->
                RunningOrder(
                    orderId = order.orderId,
                    image = order.products.firstOrNull()?.imageUrl ?: "",
                    tag = "#Breakfast",
                    totalItem = "${order.products.size} items",
                    price = "$${String.format("%.2f", order.amount)}"
                )
            }
    }

    private suspend fun removeOrderOnChangeState(orderId : String) {
        val orders = _runningOrdersData.value?.toMutableList()
        orders?.apply {
            removeIf { it.orderId == orderId }
        }
        withContext(Dispatchers.Main) {
            _runningOrdersData.value = orders?.toList()
        }
    }

    fun onOrderDone(orderId : String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = orderRepository.changeOrderState(authRepo.restaurantId!!, orderId, 3)
            if(result) {
                removeOrderOnChangeState(orderId)
            }
        }
    }

    fun onOrderCanceled(orderId : String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = orderRepository.changeOrderState(authRepo.restaurantId!!, orderId, 4)
            if(result) {
                removeOrderOnChangeState(orderId)
            }
        }
    }
}