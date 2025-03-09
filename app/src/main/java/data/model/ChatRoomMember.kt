package data.model

data class ChatRoomMember(
    val id : Int,
    val name : String,
    val image : String,
    var isOnline : Boolean,
    var latestActive : Long,
    var unSeenAmount : Int
)
