package data.repo

import data.model.Result
import data.source.remote.RemoteOrderDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val remoteOrderDataSource: RemoteOrderDataSource,
) {

    fun listenIncomingOrder(restaurantId: Int) {
        remoteOrderDataSource.listenIncomingOrder(restaurantId)
    }

    suspend fun loadRestaurantOrder(restaurantId : Int) : Result {
        remoteOrderDataSource.loadRestaurantOrder(restaurantId)?.let {
            return Result.Success(it)
        }
        return Result.Error(Exception("Load order failed"))
    }
}