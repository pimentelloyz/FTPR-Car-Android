package com.example.myapitest.domain.usecase

import com.example.myapitest.domain.model.Car
import com.example.myapitest.domain.repository.CarRepository

class GetCarsUseCase(private val repository: CarRepository) {
    suspend operator fun invoke(): List<Car> = repository.getCars()
}
