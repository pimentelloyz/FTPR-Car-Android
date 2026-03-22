package com.example.myapitest.domain.repository

import com.example.myapitest.domain.model.Car

interface CarRepository {
    suspend fun getCars(): List<Car>
    suspend fun saveCar(car: Car): Car
    suspend fun deleteCar(id: String)
    suspend fun updateCar(id: String, car: Car): Car
}
