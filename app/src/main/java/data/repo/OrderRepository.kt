package data.repo

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

}