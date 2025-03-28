package com.example.meteomars.model

data class MarsWeatherData(
    val sol: String,
    val temperature: Double,
    val pressure: Double,
    val season: String? = null,
    val firstUtc: String? = null,
    val lastUtc: String? = null,
    val windSpeed: Double? = null,
    val windDirection: String? = null,
    val minTemperature: Double? = null, 
    val maxTemperature: Double? = null
) 