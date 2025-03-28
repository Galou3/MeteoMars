package com.example.meteomars.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.meteomars.api.MarsWeatherRepository
import com.example.meteomars.model.MarsWeatherData
import com.example.meteomars.ui.theme.DarkBackground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSolSelected: (MarsWeatherData) -> Unit = {},
    onWeatherDataLoaded: (List<MarsWeatherData>) -> Unit = {}
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var weatherDataList by remember { mutableStateOf<List<MarsWeatherData>>(emptyList()) }
    
    // Fetch weather data when the screen is first composed
    LaunchedEffect(key1 = true) {
        try {
            withContext(Dispatchers.IO) {
                val weatherRepository = MarsWeatherRepository()
                val weatherResponse = weatherRepository.getMarsWeather()
                val parsedData = weatherRepository.parseAllSolsData(weatherResponse)
                
                withContext(Dispatchers.Main) {
                    weatherDataList = parsedData
                    isLoading = false
                    // Notify data loaded
                    onWeatherDataLoaded(parsedData)
                    // Show success toast
                    Toast.makeText(
                        context,
                        "Données météo de Mars téléchargées avec succès !",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                isLoading = false
                errorMessage = e.message
                // Show error toast
                Toast.makeText(
                    context,
                    "Erreur: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = "Météo sur Mars",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF202020)
                )
            )
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (errorMessage != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Erreur lors du chargement",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "Erreur inconnue",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            } else if (weatherDataList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucune donnée météo disponible",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(weatherDataList) { weatherData ->
                        SolListItem(
                            weatherData = weatherData,
                            onClick = { onSolSelected(weatherData) }
                        )
                        Divider(color = Color(0xFF303030), thickness = 1.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun SolListItem(
    weatherData: MarsWeatherData,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sol number
        Text(
            text = "Sol n°${weatherData.sol}",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        
        Column(horizontalAlignment = Alignment.End) {
            // Temperature
            Text(
                text = "Température: ${String.format("%.1f°C", weatherData.temperature)}",
                color = Color.White,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Pressure
            Text(
                text = "Pression: ${String.format("%.2f Pa", weatherData.pressure)}",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
} 