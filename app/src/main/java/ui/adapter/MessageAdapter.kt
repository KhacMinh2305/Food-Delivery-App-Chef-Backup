package ui.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.deliveryfoodchef.databinding.MessageLayoutBinding
import com.example.deliveryfoodchef.databinding.UserMessageLayoutBinding
import data.model.Message

private val messageComparator = object : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message) : Boolean {
        return oldItem.sendingTime == newItem.sendingTime
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        var isSame = true
        isSame = isSame && oldItem.senderId == newItem.senderId
        isSame = isSame && oldItem.senderName == newItem.senderName
        isSame = isSame && oldItem.senderImage == newItem.senderImage
        isSame = isSame && oldItem.senderRole == newItem.senderRole
        isSame = isSame && oldItem.content == newItem.content
        isSame = isSame && oldItem.sendingTime == newItem.sendingTime
        isSame = isSame && oldItem.isSeen == newItem.isSeen
        return isSame
    }
}

class MessageAdapter : PagingDataAdapter<Message, RecyclerView.ViewHolder>(messageComparator) {

    inner class ChefMessageViewHolder(private val binding : MessageLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item : Message) {
            binding.item = item
        }
    }

    inner class UserMessageViewHolder(private val binding : UserMessageLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item : Message) {
            binding.item = item
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position)?.senderRole ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        item?.let {
            if (holder is ChefMessageViewHolder) {
                holder.bind(it)
            } else if (holder is UserMessageViewHolder) {
                holder.bind(it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == 0) {
            val binding = UserMessageLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return UserMessageViewHolder(binding)
        }
        val binding = MessageLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChefMessageViewHolder(binding)
    }
}