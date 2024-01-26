package com.example.efarm.ui.loginsignup

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.eFarm.R
import com.example.eFarm.databinding.FragmentSignupBinding
import com.example.efarm.core.data.Resource
import com.example.efarm.ui.forum.HomeForumActivity
import com.jakewharton.rxbinding2.widget.RxTextView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignupFragment : Fragment() {
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private var isDataValid=false
    private val viewModel: LoginSignupViewModel by viewModels()
    private var nama = ""
    private var email = ""
    private var telepon = ""
    private var password = ""
    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btSudahPunyaAkun.setOnClickListener {
            findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
        }
        val nameStream = RxTextView.textChanges(binding.etDaftarNama)
            .skipInitialValue()
            .map { name ->
                name.isEmpty()
            }
        nameStream.subscribe {

        }

        val teleponStream = RxTextView.textChanges(binding.etDaftarTelepon)
            .skipInitialValue()
            .map { telp ->
                telp.length < 9 || telp.isEmpty()
            }
        teleponStream.subscribe {

        }

        val emailStream = RxTextView.textChanges(binding.etDaftarEmail)
            .skipInitialValue()
            .map { email ->
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }
        emailStream.subscribe {

        }

        val passwordStream = RxTextView.textChanges(binding.etDaftarPassword)
            .skipInitialValue()
            .map { password ->
                password.length < 6
            }
        passwordStream.subscribe {

        }
        val passwordConfirmationStream = io.reactivex.Observable.merge(
            RxTextView.textChanges(binding.etDaftarPassword)
                .map { password ->
                    password.toString() != binding.etDaftarPassword.text.toString()
                },
            RxTextView.textChanges(binding.etDaftarCpassword)
                .map { confirmPassword ->
                    confirmPassword.toString() != binding.etDaftarPassword.text.toString()
                }
        )
        passwordConfirmationStream.subscribe {
            showPasswordConfirmationAlert(it)
        }

        val validFieldsStream = io.reactivex.Observable.combineLatest(
            nameStream,
            teleponStream,
            emailStream,
            passwordStream,
            passwordConfirmationStream,
            io.reactivex.functions.Function5 { nameInvalid: Boolean, teleponInvalid: Boolean, emailInvalid: Boolean, passwordInvalid: Boolean, passwordConfirmationInvalid: Boolean ->
                !emailInvalid && !passwordInvalid && !passwordConfirmationInvalid && !nameInvalid && ! teleponInvalid
            }
        )

        validFieldsStream.subscribe { isValid ->
            isDataValid = isValid
            if(isValid){
                nama=binding.etDaftarNama.text.toString().trim()
                email=binding.etDaftarEmail.text.toString().trim()
                telepon=binding.etDaftarTelepon.text.toString().trim()
                password=binding.etDaftarPassword.text.toString().trim()
            }
        }

        binding.btDaftar.setOnClickListener {
            if (isDataValid) {
                lifecycleScope.launch{
                    viewModel.registerAccount(
                        email = email,
                        pass = password,
                        name = nama,
                        telepon = telepon
                    ).observe(requireActivity()){it->
                        when(it){
                            is Resource.Loading->{
                                showLoading(true)
                            }
                            is Resource.Error->{
                                showLoading(false)
                                it.message?.let{
                                    Log.d("TAG",it)
                                    Toast.makeText(requireContext(),it,Toast.LENGTH_LONG).show()
                                }
                            }
                            is Resource.Success->{
                                showLoading(false)
                                Toast.makeText(requireActivity(),it.data.toString(),Toast.LENGTH_LONG).show()
                                startActivity(
                                    Intent(
                                        activity,
                                        HomeForumActivity::class.java
                                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        }
                    }
                }
            }else{
                Toast.makeText(requireContext(),"Data tidak valid",Toast.LENGTH_SHORT).show()
            }
        }


        binding.cbShowPass.setOnClickListener {
            if (binding.cbShowPass.isChecked) {
                binding.etDaftarPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                binding.etDaftarCpassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            } else {
                binding.etDaftarPassword.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                binding.etDaftarCpassword.transformationMethod =
                    PasswordTransformationMethod.getInstance()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility=if(isLoading) View.VISIBLE else View.GONE
    }

    private fun showPasswordConfirmationAlert(isNotValid: Boolean) {
        binding.etDaftarCpassword.error = if (isNotValid) "Password tidak sesuai" else null
//        binding.ilDaftarCpassword.boxStrokeColor= R.color.red
//        binding.ilDaftarCpassword.helperTextCurrentTextColor=R.color.red
    }
}