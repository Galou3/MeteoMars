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
    val maxTemperature: Double? = null,
    val windDirectionMap: Map<Int, Double> = emptyMap(), // Map of direction index to count/value
    val maxWindDirectionValue: Double = 0.0 // Maximum value in the wind direction map
) 