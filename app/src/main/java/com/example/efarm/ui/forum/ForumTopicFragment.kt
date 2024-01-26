package com.example.efarm.ui.forum

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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.eFarm.R
import com.example.eFarm.databinding.FragmentForumTopicBinding
import com.example.efarm.core.data.Resource
import com.example.efarm.core.util.KategoriTopik
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ForumTopicFragment : DialogFragment() {
    private var _binding: FragmentForumTopicBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ForumViewModel by activityViewModels()
    private lateinit var adapterCommonTopic: FilterTopicAdapter
    private lateinit var adapterCommodityTopic: FilterTopicAdapter
    private lateinit var onGetDataTopic: OnGetDataTopic

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            onGetDataTopic = activity as OnGetDataTopic
        } catch (e: ClassCastException) {
            Log.e(
                "TAG", "onAttach: ClassCastException: "
                        + e.message
            )
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.getWindow()
            ?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        val layoutManagerCommonTopic = FlexboxLayoutManager(requireActivity())
        layoutManagerCommonTopic.flexDirection = FlexDirection.ROW
        binding.rvCommonTopic.layoutManager = layoutManagerCommonTopic

        val layoutManagerCommodityTopic = FlexboxLayoutManager(requireActivity())
        layoutManagerCommodityTopic.flexDirection = FlexDirection.ROW
        binding.rvCommoditiesTopic.layoutManager = layoutManagerCommodityTopic

        binding.btnAllTopics.setOnClickListener {
            onGetDataTopic.handleDataTopic(null)
            dismiss()
        }

        adapterCommonTopic = FilterTopicAdapter(true) { topic ->
            onGetDataTopic.handleDataTopic(topic)
            dismiss()
        }

        adapterCommodityTopic = FilterTopicAdapter(true) { topic ->
            onGetDataTopic.handleDataTopic(topic)
            dismiss()
        }

        binding.rvCommonTopic.adapter = adapterCommonTopic
        binding.rvCommoditiesTopic.adapter = adapterCommodityTopic

        viewModel.topicsCommon.observe(requireActivity()){
            if (!it.isEmpty()){
                adapterCommonTopic.submitList(it)
            }else{
                binding.tvLabelCommonTopic.visibility=View.GONE
            }
        }

        viewModel.topicsCommodity.observe(requireActivity()){
            if (!it.isEmpty()){
                adapterCommodityTopic.submitList(it)
            }else{
                binding.tvLabelCommoditiesTopic.visibility=View.GONE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForumTopicBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}