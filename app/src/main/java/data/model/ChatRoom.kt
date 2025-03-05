package data.model

data class ChatRoom(
    val roomId : String,
    val chef : ChatRoomMember,
    val user : ChatRoomMember,
    var latestMessage : String,
    var latestMessageTime : Long,
    var senderId : Int,
    var role : Int
)
