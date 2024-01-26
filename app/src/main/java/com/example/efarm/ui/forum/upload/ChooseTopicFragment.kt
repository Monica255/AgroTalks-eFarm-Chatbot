package com.example.efarm.ui.forum.upload

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.eFarm.R
import com.example.eFarm.databinding.FragmentChooseTopicBinding
import com.example.eFarm.databinding.FragmentForumTopicBinding
import com.example.efarm.core.data.source.remote.model.Topic
import com.example.efarm.ui.forum.FilterTopicAdapter
import com.example.efarm.ui.forum.ForumViewModel
import com.example.efarm.ui.forum.OnGetDataTopic
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import dagger.hilt.android.AndroidEntryPoint


interface OnGetDataTopics {
    fun handleDataTopic(data: Set<Topic>)
}

@AndroidEntryPoint
class ChooseTopicFragment : DialogFragment() {
    private var _binding: FragmentChooseTopicBinding? = null
    private val binding get() = _binding!!
    private lateinit var onGetDataTopics: OnGetDataTopics
    private val viewModel: MakePostViewModel by activityViewModels()
    private val topics= mutableSetOf<Topic>()
    private lateinit var adapterCommonTopic: ChooseTopicAdapter
    private lateinit var adapterCommodityTopic: ChooseTopicAdapter

    val onClick:(topic:Topic,b:Boolean)->Unit={topic,b->
        if(b){
            if(topics.size<=10){
                topics.add(topic)
            }else{
                Toast.makeText(requireContext(),"Sudah mencapai batas maksimum topik",Toast.LENGTH_SHORT).show()
            }
            topics.add(topic)
        }else{
            Log.d("SENT","remocving "+topic.topic_name)
            topics.remove(topic)
        }
    }
    override fun onStart() {
        super.onStart()
        dialog?.getWindow()
            ?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            onGetDataTopics = activity as OnGetDataTopics
        } catch (e: ClassCastException) {
            Log.e(
                "TAG", "onAttach: ClassCastException: "
                        + e.message
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnClose.setOnClickListener {
            onGetDataTopics.handleDataTopic(topics)
            dismiss()
        }
        val layoutManagerCommonTopic = FlexboxLayoutManager(requireActivity())
        layoutManagerCommonTopic.flexDirection = FlexDirection.ROW
        binding.rvCommonTopic.layoutManager = layoutManagerCommonTopic

        val layoutManagerCommodityTopic = FlexboxLayoutManager(requireActivity())
        layoutManagerCommodityTopic.flexDirection = FlexDirection.ROW
        binding.rvCommoditiesTopic.layoutManager = layoutManagerCommodityTopic

        adapterCommonTopic = ChooseTopicAdapter(onClick,viewModel)

        adapterCommodityTopic = ChooseTopicAdapter(onClick,viewModel)

        binding.rvCommonTopic.adapter = adapterCommonTopic
        binding.rvCommoditiesTopic.adapter = adapterCommodityTopic

        viewModel.topicsCommon.observe(requireActivity()){
            if (!it.isEmpty()){
                adapterCommonTopic.submitList(it.toSet())
            }else{
                binding.tvLabelCommonTopic.visibility=View.GONE
            }
        }

        viewModel.topicsCommodity.observe(requireActivity()){
            if (!it.isEmpty()){
                adapterCommodityTopic.submitList(it.toSet())
            }else{
                binding.tvLabelCommoditiesTopic.visibility=View.GONE
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChooseTopicBinding.inflate(inflater, container, false)
        return binding.root
    }


}