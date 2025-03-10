package ui.model

data class RunningOrder(
    val orderId : String,
    val image : String,
    val tag : String = "#Breakfast",
    val totalItem : String,
    val price : String
)
