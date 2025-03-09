package ui.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.deliveryfoodchef.databinding.ChatRoomItemBinding
import data.model.ChatRoom

private val chatRoomComparator = object : DiffUtil.ItemCallback<ChatRoom>() {
    override fun areItemsTheSame(oldItem: ChatRoom, newItem: ChatRoom) : Boolean {
        return oldItem.roomId == newItem.roomId && oldItem.user.id == newItem.user.id && oldItem.chef.id == newItem.chef.id
    }

    override fun areContentsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
        var isSame = true
        isSame = isSame && oldItem.latestMessage == newItem.latestMessage
        isSame = isSame && oldItem.latestMessageTime == newItem.latestMessageTime
        isSame = isSame && oldItem.senderId == newItem.senderId
        isSame = isSame && oldItem.role == newItem.role
        isSame = isSame && oldItem.user.id == newItem.user.id
        isSame = isSame && oldItem.user.name == newItem.user.name
        isSame = isSame && oldItem.user.image == newItem.user.image
        isSame = isSame && oldItem.user.isOnline == newItem.user.isOnline
        isSame = isSame && oldItem.user.latestActive == newItem.user.latestActive
        isSame = isSame && oldItem.user.unSeenAmount == newItem.user.unSeenAmount
        isSame = isSame && oldItem.chef.id == newItem.chef.id
        isSame = isSame && oldItem.chef.name == newItem.chef.name
        isSame = isSame && oldItem.chef.image == newItem.chef.image
        isSame = isSame && oldItem.chef.isOnline == newItem.chef.isOnline
        isSame = isSame && oldItem.chef.latestActive == newItem.chef.latestActive
        isSame = isSame && oldItem.chef.unSeenAmount == newItem.chef.unSeenAmount
        return isSame
    }
}

class ChattingRoomAdapter(private val onClickEvent : (String) -> Unit) : PagingDataAdapter<ChatRoom, ChattingRoomAdapter.ChattingRoomViewHolder>(chatRoomComparator) {

    inner class ChattingRoomViewHolder(private val binding : ChatRoomItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item : ChatRoom) {
            binding.item = item
        }

        fun listenEvent(conversationId : String) {
            binding.root.setOnClickListener {
                onClickEvent(conversationId)
            }
        }
    }

    override fun onBindViewHolder(holder: ChattingRoomViewHolder, position: Int) {
        val item = getItem(position)
        item?.let {
            holder.bind(it)
            holder.listenEvent(it.roomId)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChattingRoomViewHolder {
        val binding = ChatRoomItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChattingRoomViewHolder(binding)
    }
}