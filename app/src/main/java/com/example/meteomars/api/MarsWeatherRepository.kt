package com.example.meteomars.api

import com.example.meteomars.model.MarsWeatherData
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MarsWeatherRepository {
    
    companion object {
        private const val API_KEY = "PpSGaMoq3yCF0VnJsS2Fprei3etfcsn33FvPA9yQ"
        private const val BASE_URL = "https://api.nasa.gov/insight_weather/"
    }
    
    /**
     * Fetches Mars weather data from NASA's API
     * @return JSON string containing the weather data
     */
    fun getMarsWeather(): String {
        val url = URL("$BASE_URL?api_key=$API_KEY&feedtype=json&ver=1.0")
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                
                return response.toString()
            } else {
                throw Exception("Erreur de connexion avec le code: $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }
    
    /**
     * Parse JSON response to extract weather data for all sols
     * @param jsonResponse The JSON response from the API
     * @return List of MarsWeatherData objects
     */
    fun parseAllSolsData(jsonResponse: String): List<MarsWeatherData> {
        val weatherDataList = mutableListOf<MarsWeatherData>()
        val jsonObject = JSONObject(jsonResponse)
        
        try {
            // Get all sol keys either from sol_keys array or by finding numeric keys
            val solKeys = mutableListOf<String>()
            
            if (jsonObject.has("sol_keys")) {
                val solKeysArray = jsonObject.getJSONArray("sol_keys")
                for (i in 0 until solKeysArray.length()) {
                    solKeys.add(solKeysArray.getString(i))
                }
            } else {
                // Fallback to numeric sol keys as in the example
                val keys = jsonObject.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    if (key.matches(Regex("\\d+"))) {
                        solKeys.add(key)
                    }
                }
            }
            
            // Sort the sol keys by their numeric value (descending - newest first)
            solKeys.sortByDescending { it.toIntOrNull() ?: 0 }
            
            // Process each sol
            for (solKey in solKeys) {
                if (jsonObject.has(solKey)) {
                    val solData = jsonObject.getJSONObject(solKey)
                    val marsWeatherData = processSolDataToModel(solData, solKey)
                    weatherDataList.add(marsWeatherData)
                }
            }
            
        } catch (e: Exception) {
            // Return an empty list if there's an error
            // Alternatively, we could throw the exception up
        }
        
        return weatherDataList
    }
    
    /**
     * Process data for a specific sol (Martian day) into a MarsWeatherData object
     */
    private fun processSolDataToModel(solData: JSONObject, solKey: String): MarsWeatherData {
        // Default values
        var temperature = 0.0
        var pressure = 0.0
        var windSpeed: Double? = null
        var windDirection: String? = null
        var minTemperature: Double? = null
        var maxTemperature: Double? = null
        val windDirectionMap = mutableMapOf<Int, Double>()
        var maxWindDirectionValue = 0.0
        
        // Get average temperature
        if (solData.has("AT")) {
            val atData = solData.getJSONObject("AT")
            temperature = atData.optDouble("av", 0.0)
            minTemperature = atData.optDouble("mn", 0.0)
            maxTemperature = atData.optDouble("mx", 0.0)
        }
        
        // Get wind speed
        if (solData.has("HWS")) {
            val hwsData = solData.getJSONObject("HWS")
            windSpeed = hwsData.optDouble("av", 0.0)
        }
        
        // Get pressure
        if (solData.has("PRE")) {
            val preData = solData.getJSONObject("PRE")
            pressure = preData.optDouble("av", 0.0)
        }
        
        // Get wind direction
        if (solData.has("WD")) {
            val wdData = solData.getJSONObject("WD")
            
            // Get most common direction
            if (wdData.has("most_common")) {
                val mostCommon = wdData.getJSONObject("most_common")
                windDirection = mostCommon.optString("compass_point", "N/A")
            }
            
            // Extract all 16 wind directions
            for (i in 0..15) {
                if (wdData.has(i.toString())) {
                    val directionData = wdData.getJSONObject(i.toString())
                    val count = directionData.optDouble("ct", 0.0)
                    windDirectionMap[i] = count
                    
                    // Update max value if needed
                    if (count > maxWindDirectionValue) {
                        maxWindDirectionValue = count
                    }
                }
            }
        }
        
        return MarsWeatherData(
            sol = solKey,
            temperature = temperature,
            pressure = pressure,
            season = solData.optString("Season", null),
            firstUtc = solData.optString("First_UTC", null),
            lastUtc = solData.optString("Last_UTC", null),
            windSpeed = windSpeed,
            windDirection = windDirection,
            minTemperature = minTemperature,
            maxTemperature = maxTemperature,
            windDirectionMap = windDirectionMap,
            maxWindDirectionValue = maxWindDirectionValue
        )
    }
    
    /**
     * Parse JSON response to extract weather data
     * @param jsonResponse The JSON response from the API
     * @return Map of key weather data
     */
    fun parseWeatherData(jsonResponse: String): Map<String, Any> {
        val weatherMap = mutableMapOf<String, Any>()
        val jsonObject = JSONObject(jsonResponse)
        
        try {
            // First try with sol_keys if present
            if (jsonObject.has("sol_keys")) {
                val solKeys = jsonObject.getJSONArray("sol_keys")
                if (solKeys.length() > 0) {
                    val firstSolKey = solKeys.getString(0)
                    processSolData(jsonObject.getJSONObject(firstSolKey), weatherMap, firstSolKey)
                }
            } else {
                // Fallback to numeric sol keys as in the example
                val keys = jsonObject.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    if (key.matches(Regex("\\d+"))) {
                        // Found a numeric key - likely a sol
                        processSolData(jsonObject.getJSONObject(key), weatherMap, key)
                        break // Process only the first sol found
                    }
                }
            }
            
            // If we couldn't find any weather data, add a placeholder
            if (weatherMap.isEmpty()) {
                weatherMap["message"] = "Aucune donnée météo n'a pu être extraite"
            }
        } catch (e: Exception) {
            weatherMap["error"] = "Erreur lors de l'analyse des données: ${e.message}"
        }
        
        return weatherMap
    }
    
    /**
     * Process data for a specific sol (Martian day)
     */
    private fun processSolData(solData: JSONObject, weatherMap: MutableMap<String, Any>, solKey: String) {
        weatherMap["sol"] = solKey
        
        // Get average temperature
        if (solData.has("AT")) {
            val atData = solData.getJSONObject("AT")
            weatherMap["averageTemperature"] = atData.optDouble("av", 0.0)
            weatherMap["minTemperature"] = atData.optDouble("mn", 0.0)
            weatherMap["maxTemperature"] = atData.optDouble("mx", 0.0)
        }
        
        // Get wind speed
        if (solData.has("HWS")) {
            val hwsData = solData.getJSONObject("HWS")
            weatherMap["averageWindSpeed"] = hwsData.optDouble("av", 0.0)
            weatherMap["maxWindSpeed"] = hwsData.optDouble("mx", 0.0)
        }
        
        // Get pressure
        if (solData.has("PRE")) {
            val preData = solData.getJSONObject("PRE")
            weatherMap["averagePressure"] = preData.optDouble("av", 0.0)
        }
        
        // Get wind direction
        if (solData.has("WD")) {
            val wdData = solData.getJSONObject("WD")
            if (wdData.has("most_common")) {
                val mostCommon = wdData.getJSONObject("most_common")
                weatherMap["windDirection"] = mostCommon.optString("compass_point", "N/A")
            }
        }
        
        // Get season
        weatherMap["season"] = solData.optString("Season", "Unknown")
        weatherMap["northernSeason"] = solData.optString("Northern_season", "Unknown")
        weatherMap["southernSeason"] = solData.optString("Southern_season", "Unknown")
        
        // Get first and last UTC dates
        weatherMap["firstUTC"] = solData.optString("First_UTC", "")
        weatherMap["lastUTC"] = solData.optString("Last_UTC", "")
    }
} 