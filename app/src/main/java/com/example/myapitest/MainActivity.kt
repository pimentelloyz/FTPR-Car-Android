package com.example.myapitest

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapitest.databinding.ActivityMainBinding
import com.example.myapitest.domain.model.Car
import com.example.myapitest.presentation.addcar.AddCarActivity
import com.example.myapitest.presentation.login.LoginActivity
import com.example.myapitest.presentation.maps.MapsActivity
import com.example.myapitest.presentation.main.CarAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: CarAdapter
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        setupRecyclerView()
        setupView()
    }

    override fun onResume() {
        super.onResume()
        if (auth.currentUser != null) {
            fetchItems()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                confirmLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        adapter = CarAdapter(
            onItemClick = { car -> openMaps(car) },
            onItemLongClick = { car -> confirmDelete(car) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupView() {
        binding.addCta.setOnClickListener {
            startActivity(Intent(this, AddCarActivity::class.java))
        }

        binding.swipeRefreshLayout.setOnRefreshListener { fetchItems() }
    }

    private fun fetchItems() {
        binding.swipeRefreshLayout.isRefreshing = true
        lifecycleScope.launch {
            runCatching { ServiceLocator.getCarsUseCase() }
                .onSuccess { cars ->
                    adapter.submitList(cars)
                }
                .onFailure { error ->
                    Toast.makeText(
                        this@MainActivity,
                        "Erro ao carregar: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun openMaps(car: Car) {
        val intent = Intent(this, MapsActivity::class.java).apply {
            putExtra(MapsActivity.EXTRA_LAT, car.place.lat)
            putExtra(MapsActivity.EXTRA_LONG, car.place.long)
            putExtra(MapsActivity.EXTRA_TITLE, car.name)
        }
        startActivity(intent)
    }

    private fun confirmDelete(car: Car) {
        AlertDialog.Builder(this)
            .setTitle("Excluir carro")
            .setMessage("Deseja excluir ${car.name}?")
            .setPositiveButton("Excluir") { _, _ -> deleteCar(car) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteCar(car: Car) {
        lifecycleScope.launch {
            runCatching { ServiceLocator.deleteCarUseCase(car.id) }
                .onSuccess {
                    Toast.makeText(this@MainActivity, "Carro excluído", Toast.LENGTH_SHORT).show()
                    fetchItems()
                }
                .onFailure { error ->
                    Toast.makeText(
                        this@MainActivity,
                        "Erro ao excluir: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Deseja sair da conta?")
            .setPositiveButton("Sair") { _, _ ->
                auth.signOut()
                navigateToLogin()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}

