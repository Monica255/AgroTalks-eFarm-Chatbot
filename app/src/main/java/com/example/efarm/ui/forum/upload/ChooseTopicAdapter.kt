package com.example.efarm.ui.forum.upload

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.eFarm.databinding.ItemFilterTopicBinding
import com.example.eFarm.databinding.ItemPilikTopikBinding
import com.example.efarm.core.data.source.remote.model.Topic
import com.google.firebase.auth.FirebaseAuth


class ChooseTopicAdapter(
    private val onClick: ((Topic,Boolean) -> Unit),
    private val viewModel: MakePostViewModel
) :
    RecyclerView.Adapter<ChooseTopicAdapter.FillterTopicVH>() {

    var list = setOf<Topic>()
    private val uid= FirebaseAuth.getInstance().currentUser?.uid

    fun submitList(mList: Set<Topic>) {
        list = mList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FillterTopicVH {
        val binding =
            ItemPilikTopikBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FillterTopicVH(binding)
    }

    override fun onBindViewHolder(holder: FillterTopicVH, position: Int) {
        val data = list.elementAt(position)
        holder.bind(data)
    }

    override fun getItemCount(): Int = list.size

    inner class FillterTopicVH(private val binding: ItemPilikTopikBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(topic: Topic) {

            binding.root.setOnCheckedChangeListener { _, b ->
                onClick(topic,b)
            }
            val x=viewModel.topics.value?.find { it.topic_id==topic.topic_id }
            binding.root.isChecked= x!=null
            binding.root.text=topic.topic_name
        }
    }
}