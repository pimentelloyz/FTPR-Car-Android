package com.example.myapitest.presentation.addcar

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapitest.ServiceLocator
import com.example.myapitest.databinding.ActivityAddCarBinding
import com.example.myapitest.domain.model.Car
import com.example.myapitest.domain.model.Place
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.util.UUID

class AddCarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCarBinding
    private var selectedImageUri: Uri? = null
    private var currentLat: Double = 0.0
    private var currentLong: Double = 0.0

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.carImageView.setImageURI(it)
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) getLastLocation()
        else Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Adicionar Carro"
        setupView()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupView() {
        binding.selectImageButton.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.useMyLocationButton.setOnClickListener {
            requestLocation()
        }

        binding.saveButton.setOnClickListener {
            saveCar()
        }
    }

    private fun requestLocation() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getLastLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getLastLocation() {
        val fusedClient = LocationServices.getFusedLocationProviderClient(this)
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLat = location.latitude
                currentLong = location.longitude
                binding.latInput.setText(currentLat.toString())
                binding.longInput.setText(currentLong.toString())
                Toast.makeText(this, "Localização obtida", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Não foi possível obter localização", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveCar() {
        val name = binding.nameInput.text?.toString().orEmpty().trim()
        val year = binding.yearInput.text?.toString().orEmpty().trim()
        val licence = binding.licenceInput.text?.toString().orEmpty().trim()
        val lat = binding.latInput.text?.toString().orEmpty().toDoubleOrNull() ?: 0.0
        val long = binding.longInput.text?.toString().orEmpty().toDoubleOrNull() ?: 0.0

        if (name.isBlank() || year.isBlank() || licence.isBlank()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Selecione uma imagem", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        lifecycleScope.launch {
            runCatching {
                val imageUrl = ServiceLocator.uploadImageUseCase(selectedImageUri!!)
                val car = Car(
                    id = UUID.randomUUID().toString(),
                    imageUrl = imageUrl,
                    year = year,
                    name = name,
                    licence = licence,
                    place = Place(lat = lat, long = long)
                )
                ServiceLocator.saveCarUseCase(car)
            }.onSuccess {
                setLoading(false)
                Toast.makeText(this@AddCarActivity, "Carro salvo com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure { e ->
                setLoading(false)
                Toast.makeText(this@AddCarActivity, "Erro ao salvar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.saveButton.isEnabled = !isLoading
        binding.selectImageButton.isEnabled = !isLoading
        binding.useMyLocationButton.isEnabled = !isLoading
    }
}
