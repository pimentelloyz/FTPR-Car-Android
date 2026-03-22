package com.example.myapitest.data.remote

import com.example.myapitest.data.model.CarDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CarApiService {

    @GET("car")
    suspend fun getCars(): List<CarDto>

    @GET("car/{id}")
    suspend fun getCarById(@Path("id") id: String): CarDto

    @POST("car")
    suspend fun saveCar(@Body car: CarDto): CarDto

    @DELETE("car/{id}")
    suspend fun deleteCar(@Path("id") id: String)

    @PATCH("car/{id}")
    suspend fun updateCar(@Path("id") id: String, @Body car: CarDto): CarDto
}
