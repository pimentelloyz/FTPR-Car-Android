package com.example.myapitest.data.mapper

import com.example.myapitest.data.model.CarDto
import com.example.myapitest.data.model.PlaceDto
import com.example.myapitest.domain.model.Car
import com.example.myapitest.domain.model.Place

fun CarDto.toDomain(): Car = Car(
    id = id.orEmpty(),
    imageUrl = imageUrl,
    year = year,
    name = name,
    licence = licence,
    place = place.toDomain()
)

fun PlaceDto.toDomain(): Place = Place(lat = lat, long = long)

fun Car.toDto(): CarDto = CarDto(
    id = id.ifEmpty { null },
    imageUrl = imageUrl,
    year = year,
    name = name,
    licence = licence,
    place = PlaceDto(lat = place.lat, long = place.long)
)
