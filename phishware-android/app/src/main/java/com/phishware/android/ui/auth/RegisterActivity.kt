package com.phishware.android.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.phishware.android.databinding.ActivityRegisterBinding
import com.phishware.android.ui.MainActivity
import com.phishware.android.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeState()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnRegister.setOnClickListener {
            val username  = binding.etUsername.text.toString()
            val email     = binding.etEmail.text.toString()
            val password  = binding.etPassword.text.toString()
            val firstName = binding.etFirstName.text.toString().ifBlank { null }
            val lastName  = binding.etLastName.text.toString().ifBlank { null }

            viewModel.register(username, email, password, firstName, lastName)
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthViewModel.AuthState.Idle    -> setLoading(false)
                    is AuthViewModel.AuthState.Loading -> setLoading(true)
                    is AuthViewModel.AuthState.Success -> {
                        setLoading(false)
                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    }
                    is AuthViewModel.AuthState.Error -> {
                        setLoading(false)
                        Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                        viewModel.resetState()
                    }
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !loading
    }
}
