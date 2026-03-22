package com.example.myapitest

import com.example.myapitest.data.remote.RetrofitClient
import com.example.myapitest.data.repository.CarRepositoryImpl
import com.example.myapitest.domain.repository.CarRepository
import com.example.myapitest.domain.usecase.DeleteCarUseCase
import com.example.myapitest.domain.usecase.GetCarsUseCase
import com.example.myapitest.domain.usecase.SaveCarUseCase
import com.example.myapitest.domain.usecase.UploadImageUseCase

object ServiceLocator {

    private val carRepository: CarRepository by lazy {
        CarRepositoryImpl(RetrofitClient.instance)
    }

    val getCarsUseCase: GetCarsUseCase by lazy { GetCarsUseCase(carRepository) }
    val saveCarUseCase: SaveCarUseCase by lazy { SaveCarUseCase(carRepository) }
    val deleteCarUseCase: DeleteCarUseCase by lazy { DeleteCarUseCase(carRepository) }
    val uploadImageUseCase: UploadImageUseCase by lazy { UploadImageUseCase() }
}
