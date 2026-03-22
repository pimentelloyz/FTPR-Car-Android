package com.example.myapitest.domain.usecase

import com.example.myapitest.domain.model.Car
import com.example.myapitest.domain.repository.CarRepository

class SaveCarUseCase(private val repository: CarRepository) {
    suspend operator fun invoke(car: Car): Car = repository.saveCar(car)
}
