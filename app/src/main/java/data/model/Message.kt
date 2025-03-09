package data.model

data class Message(
    val senderId : Int,
    val senderName : String,
    val senderImage : String,
    val senderRole : Int,
    val content : String,
    val sendingTime : Long,
    var isSeen : Boolean
)
