package com.example.myapitest.presentation.maps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.R
import com.example.myapitest.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var map: GoogleMap
    private var lat: Double = 0.0
    private var long: Double = 0.0
    private var carName: String = "Carro"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_maps)

        lat = intent.getDoubleExtra(EXTRA_LAT, 0.0)
        long = intent.getDoubleExtra(EXTRA_LONG, 0.0)
        carName = intent.getStringExtra(EXTRA_TITLE) ?: getString(R.string.map_default_car_name)

        supportActionBar?.title = carName
        supportActionBar?.subtitle = getString(R.string.title_maps)

        binding.mapTitle.text = carName
        binding.mapCoords.text = String.format(Locale.US, "%.6f, %.6f", lat, long)
        animateMapInfoCard()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.setPadding(0, 320, 0, 0)

        val position = LatLng(lat, long)
        map.addMarker(MarkerOptions().position(position).title(carName))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
    }

    private fun animateMapInfoCard() {
        binding.mapInfoCard.alpha = 0f
        binding.mapInfoCard.translationY = -24f
        binding.mapInfoCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(360)
            .setStartDelay(80)
            .start()
    }

    companion object {
        const val EXTRA_LAT = "extra_lat"
        const val EXTRA_LONG = "extra_long"
        const val EXTRA_TITLE = "extra_car_name"
    }
}
