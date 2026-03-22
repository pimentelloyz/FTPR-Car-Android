package com.example.myapitest.domain.usecase

import com.example.myapitest.domain.repository.CarRepository

class DeleteCarUseCase(private val repository: CarRepository) {
    suspend operator fun invoke(id: String) = repository.deleteCar(id)
}
