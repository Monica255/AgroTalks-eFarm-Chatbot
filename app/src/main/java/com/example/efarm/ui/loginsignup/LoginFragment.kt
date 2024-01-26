package com.example.efarm.ui.loginsignup

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.eFarm.R
import com.example.eFarm.databinding.FragmentLoginBinding
import com.example.efarm.core.data.Resource
import com.example.efarm.ui.forum.HomeForumActivity
import com.jakewharton.rxbinding2.widget.RxTextView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginSignupViewModel by viewModels()
    private var email = ""
    private var password = ""
    private var isDataValid=false

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btBlmPunyaAkun.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }

        val emailStream = RxTextView.textChanges(binding.etMasukEmail)
            .skipInitialValue()
            .map { email ->
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }
        emailStream.subscribe {
//            showEmailExistAlert(it)
        }

        val passwordStream = RxTextView.textChanges(binding.etMasukPassword)
            .skipInitialValue()
            .map { password ->
                password.length < 6
            }
        passwordStream.subscribe {
//            showPasswordMinimalAlert(it)
        }

        val invalidFieldsStream = Observable.combineLatest(
            emailStream,
            passwordStream,
            io.reactivex.functions.BiFunction {emailInvalid: Boolean, passwordInvalid: Boolean ->
                !emailInvalid && !passwordInvalid
            }
        )

        invalidFieldsStream.subscribe { isValid ->
            isDataValid = isValid
            if(isValid){
                email=binding.etMasukEmail.text.toString().trim()
                password=binding.etMasukPassword.text.toString().trim()
            }
        }

        binding.btMasuk.setOnClickListener {
            if (isDataValid) {
                lifecycleScope.launch {
                    viewModel.login(email, password).observe(viewLifecycleOwner) {
                        when (it) {
                            is Resource.Loading -> {
                                showLoading(true)
                            }
                            is Resource.Success -> {
                                showLoading(false)
                                startActivity(
                                    Intent(
                                        activity,
                                        HomeForumActivity::class.java
                                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                            is Resource.Error -> {
                                showLoading(false)
                                it.message?.let { it ->
                                    Toast.makeText(requireContext(),it,Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility=if(isLoading) View.VISIBLE else View.GONE
    }
}