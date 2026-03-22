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

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Localização do Carro"

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val lat = intent.getDoubleExtra(EXTRA_LAT, 0.0)
        val long = intent.getDoubleExtra(EXTRA_LONG, 0.0)
        val carName = intent.getStringExtra(EXTRA_TITLE) ?: "Carro"

        val position = LatLng(lat, long)
        map.addMarker(MarkerOptions().position(position).title(carName))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
    }

    companion object {
        const val EXTRA_LAT = "extra_lat"
        const val EXTRA_LONG = "extra_long"
        const val EXTRA_TITLE = "extra_car_name"
    }
}
