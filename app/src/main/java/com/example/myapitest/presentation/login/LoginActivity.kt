package com.example.myapitest.presentation.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.MainActivity
import com.example.myapitest.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            navigateToMain()
            return
        }

        setupView()
    }

    private fun setupView() {
        binding.sendCodeButton.setOnClickListener {
            val phone = binding.phoneInput.text?.toString().orEmpty()
            if (phone.isBlank()) {
                Toast.makeText(this, "Informe um número de telefone", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendVerificationCode(phone)
        }

        binding.verifyCodeButton.setOnClickListener {
            val code = binding.codeInput.text?.toString().orEmpty()
            if (code.length != 6) {
                Toast.makeText(this, "Informe o código de 6 dígitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            verifyCode(code)
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        setLoading(true)
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    setLoading(false)
                    Toast.makeText(
                        this@LoginActivity,
                        "Falha ao enviar código: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onCodeSent(
                    id: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    setLoading(false)
                    verificationId = id
                    binding.phoneContainer.visibility = View.GONE
                    binding.codeContainer.visibility = View.VISIBLE
                    Toast.makeText(
                        this@LoginActivity,
                        "Código enviado. Use 123456 para teste.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyCode(code: String) {
        val id = verificationId ?: run {
            Toast.makeText(this, "Solicite o código novamente", Toast.LENGTH_SHORT).show()
            return
        }
        setLoading(true)
        val credential = PhoneAuthProvider.getCredential(id, code)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    navigateToMain()
                } else {
                    Toast.makeText(
                        this,
                        "Autenticação falhou: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.sendCodeButton.isEnabled = !isLoading
        binding.verifyCodeButton.isEnabled = !isLoading
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
