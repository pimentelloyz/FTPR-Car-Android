package com.example.myapitest.presentation.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.myapitest.MainActivity
import com.example.myapitest.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val BRAZIL_DDI = "+55"
    }

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
        setupValidation()

        binding.sendCodeButton.setOnClickListener {
            val localPhone = extractDigits(binding.phoneInput.text?.toString().orEmpty())
            if (!isValidBrazilMobile(localPhone)) {
                Toast.makeText(this, "Informe um número de telefone", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendVerificationCode("$BRAZIL_DDI$localPhone")
        }

        binding.verifyCodeButton.setOnClickListener {
            val code = extractDigits(binding.codeInput.text?.toString().orEmpty())
            if (!isValidVerificationCode(code)) {
                Toast.makeText(this, "Informe o código de 6 dígitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            verifyCode(code)
        }
    }

    private fun setupValidation() {
        binding.phoneInput.doAfterTextChanged {
            updateActionButtons()
        }
        binding.codeInput.doAfterTextChanged {
            updateActionButtons()
        }
        updateActionButtons()
    }

    private fun updateActionButtons() {
        val isPhoneValid = isValidBrazilMobile(extractDigits(binding.phoneInput.text?.toString().orEmpty()))
        val isCodeValid = isValidVerificationCode(extractDigits(binding.codeInput.text?.toString().orEmpty()))

        binding.sendCodeButton.isEnabled = isPhoneValid
        binding.verifyCodeButton.isEnabled = isCodeValid
    }

    private fun isValidBrazilMobile(phoneDigits: String): Boolean {
        if (phoneDigits.length != 11) return false
        if (phoneDigits[2] != '9') return false
        return true
    }

    private fun isValidVerificationCode(codeDigits: String): Boolean {
        return codeDigits.length == 6
    }

    private fun extractDigits(value: String): String {
        return value.filter { it.isDigit() }
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
                        getAuthErrorMessage(e),
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
                    updateActionButtons()
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
                        getAuthErrorMessage(task.exception),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun getAuthErrorMessage(error: Throwable?): String {
        return when (error) {
            is FirebaseAuthInvalidCredentialsException -> "Código inválido. Confira os 6 dígitos e tente novamente."
            is FirebaseTooManyRequestsException -> "Muitas tentativas. Aguarde um momento e tente novamente."
            is FirebaseNetworkException -> "Sem conexão com a internet."
            is FirebaseException -> "Não foi possível autenticar agora. Tente novamente."
            else -> "Não foi possível concluir o login. Tente novamente."
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) {
            binding.sendCodeButton.isEnabled = false
            binding.verifyCodeButton.isEnabled = false
        } else {
            updateActionButtons()
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
