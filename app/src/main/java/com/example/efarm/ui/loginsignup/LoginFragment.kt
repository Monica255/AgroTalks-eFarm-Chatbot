package com.example.efarm.ui.loginsignup

import android.annotation.SuppressLint
import android.app.Instrumentation
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.eFarm.R
import com.example.eFarm.databinding.FragmentLoginBinding
import com.example.efarm.core.data.Resource
import com.example.efarm.ui.forum.HomeForumActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.jakewharton.rxbinding2.widget.RxTextView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private lateinit var googleSignInClient: GoogleSignInClient
    private val binding get() = _binding!!
    private val viewModel: LoginSignupViewModel by viewModels()
    private var email = ""
    private var password = ""
    private var isDataValid=false

    private val resultCOntract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result?.data)
                val exception = task.exception
                if (task.isSuccessful) {
                    val account = task.getResult(ApiException::class.java)
                    lifecycleScope.launch {
                        viewModel.firebaseAuthWithGoogle(account.idToken.toString())
                            .observe(viewLifecycleOwner) {
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

                } else {
                    showLoading(false)
                    Toast.makeText(requireContext(),"Gagal login",Toast.LENGTH_SHORT).show()

                }
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(requireContext(),"Gagal login",Toast.LENGTH_SHORT).show()
            }
        }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id_2))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        binding.btMasukGoogle.setOnClickListener {
            signIn()
        }
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

        binding.cbShowPass.setOnClickListener {
            if (binding.cbShowPass.isChecked) {
                binding.etMasukPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            } else {
                binding.etMasukPassword.transformationMethod =
                    PasswordTransformationMethod.getInstance()
            }
        }

    }
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        resultCOntract.launch(signInIntent)
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