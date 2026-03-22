package com.example.myapitest.data.repository

import com.example.myapitest.data.mapper.toDomain
import com.example.myapitest.data.mapper.toDto
import com.example.myapitest.data.remote.CarApiService
import com.example.myapitest.domain.model.Car
import com.example.myapitest.domain.repository.CarRepository

class CarRepositoryImpl(private val apiService: CarApiService) : CarRepository {

    override suspend fun getCars(): List<Car> =
        apiService.getCars().map { it.toDomain() }

    override suspend fun saveCar(car: Car): Car =
        apiService.saveCar(car.toDto()).toDomain()

    override suspend fun deleteCar(id: String) =
        apiService.deleteCar(id)

    override suspend fun updateCar(id: String, car: Car): Car =
        apiService.updateCar(id, car.toDto()).toDomain()
}
