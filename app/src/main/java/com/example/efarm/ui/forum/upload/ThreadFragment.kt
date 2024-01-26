package com.example.efarm.ui.forum.upload

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.eFarm.R
import com.example.eFarm.databinding.FragmentChooseTopicBinding
import com.example.eFarm.databinding.FragmentThreadBinding
import com.example.efarm.core.data.source.remote.model.Topic
import dagger.hilt.android.AndroidEntryPoint

interface OnGetDataThread{
    fun handleDataThreadc(data: String)
}

@AndroidEntryPoint
class ThreadFragment : DialogFragment() {
    private var _binding: FragmentThreadBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MakePostViewModel by activityViewModels()
    private lateinit var onGetDataThread: OnGetDataThread
    override fun onStart() {
        super.onStart()
        dialog?.getWindow()
            ?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            onGetDataThread = activity as OnGetDataThread
        } catch (e: ClassCastException) {
            Log.e(
                "TAG", "onAttach: ClassCastException: "
                        + e.message
            )
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.etThread.setText(viewModel.tempThread)
        binding.btnClose.setOnClickListener {
            viewModel.tempThread=binding.etThread.text.toString().trim()
            onGetDataThread.handleDataThreadc(viewModel.tempThread)
            dismiss()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentThreadBinding.inflate(inflater, container, false)
        return binding.root    }

}