package com.example.myapitest.presentation.addcar

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapitest.R
import com.example.myapitest.ServiceLocator
import com.example.myapitest.databinding.ActivityAddCarBinding
import com.example.myapitest.domain.model.Car
import com.example.myapitest.domain.model.Place
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.net.SocketTimeoutException
import java.net.UnknownHostException
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
            val cachedImageUri = cacheImageLocally(it)
            if (cachedImageUri == null) {
                selectedImageUri = null
                Toast.makeText(this, "Não foi possível ler essa imagem. Selecione outra.", Toast.LENGTH_SHORT).show()
                return@let
            }

            selectedImageUri = cachedImageUri
            binding.carImageView.setImageURI(cachedImageUri)
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) getDeviceLocation()
        else Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
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
        if (!isLocationServiceEnabled()) {
            showEnableLocationDialog()
            return
        }

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getDeviceLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getDeviceLocation() {
        val fusedClient = LocationServices.getFusedLocationProviderClient(this)
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                fillLocation(location.latitude, location.longitude)
            } else {
                requestCurrentLocation(fusedClient)
            }
        }.addOnFailureListener {
            requestCurrentLocation(fusedClient)
        }
    }

    private fun requestCurrentLocation(
        fusedClient: com.google.android.gms.location.FusedLocationProviderClient
    ) {
        val cancellationTokenSource = CancellationTokenSource()
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    fillLocation(location.latitude, location.longitude)
                } else {
                    Toast.makeText(this, "Não foi possível obter localização. Verifique o GPS do emulador.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Não foi possível obter localização. Verifique o GPS do emulador.", Toast.LENGTH_LONG).show()
            }
    }

    private fun fillLocation(lat: Double, long: Double) {
        currentLat = lat
        currentLong = long
        binding.latInput.setText(currentLat.toString())
        binding.longInput.setText(currentLong.toString())
        Toast.makeText(this, "Localização obtida", Toast.LENGTH_SHORT).show()
    }

    private fun isLocationServiceEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showEnableLocationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.location_disabled_title))
            .setMessage(getString(R.string.location_disabled_message))
            .setPositiveButton(getString(R.string.location_open_settings)) { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(getString(R.string.location_cancel), null)
            .show()
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
                Toast.makeText(this@AddCarActivity, getSaveCarErrorMessage(e), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getSaveCarErrorMessage(error: Throwable): String {
        return when (error) {
            is UnknownHostException, is SocketTimeoutException -> "Sem conexão com o servidor. Verifique a internet."
            is StorageException -> "Não foi possível enviar a imagem agora. Tente novamente."
            is HttpException -> "Não foi possível salvar o carro agora."
            else -> "Erro ao salvar o carro. Tente novamente."
        }
    }

    private fun cacheImageLocally(sourceUri: Uri): Uri? {
        return runCatching {
            val sourceStream = contentResolver.openInputStream(sourceUri)
                ?: return null

            sourceStream.use { input ->
                val destination = File(cacheDir, "car_${UUID.randomUUID()}.jpg")
                FileOutputStream(destination).use { output ->
                    input.copyTo(output)
                }
                Uri.fromFile(destination)
            }
        }.getOrNull()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.saveButton.isEnabled = !isLoading
        binding.selectImageButton.isEnabled = !isLoading
        binding.useMyLocationButton.isEnabled = !isLoading
    }
}
