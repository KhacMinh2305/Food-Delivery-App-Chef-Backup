package ui.model

data class RunningOrder(
    val orderId : String,
    val image : String,
    val tag : String = "#Breakfast",
    val name : String,
    val price : String
)
