package ui.viewmodel.home
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import data.repo.OrderRepository
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    fun initialize(restaurantId : Int) {
        listenIncomingOrder(restaurantId)
    }

    private fun listenIncomingOrder(restaurantId : Int) {
        orderRepository.listenIncomingOrder(restaurantId)
    }
}