package com.example.myapitest.data.model

import com.google.gson.annotations.SerializedName

data class PlaceDto(
    @SerializedName("lat") val lat: Double,
    @SerializedName("long") val long: Double
)

data class CarDto(
    @SerializedName("id") val id: String?,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("year") val year: String,
    @SerializedName("name") val name: String,
    @SerializedName("licence") val licence: String,
    @SerializedName("place") val place: PlaceDto
)
