package com.example.meteomars.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meteomars.model.MarsWeatherData
import com.example.meteomars.ui.theme.DarkBackground
import com.example.meteomars.ui.theme.TealGrid
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolDetailScreen(
    marsWeatherData: MarsWeatherData,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(
                title = {
                    Text(
                        "Sol n°${marsWeatherData.sol}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF202020)
                )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Season
                marsWeatherData.season?.let { season ->
                    DetailCard(
                        title = "Saison",
                        content = "Mars: $season"
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Date info
                if (marsWeatherData.firstUtc != null && marsWeatherData.lastUtc != null) {
                    DetailCard(
                        title = "Période de mesure",
                        content = "Du: ${formatDate(marsWeatherData.firstUtc)}\nAu: ${formatDate(marsWeatherData.lastUtc)}"
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Temperature info
                DetailCard(
                    title = "Température",
                    content = buildString {
                        append("Moyenne: ${formatTemperature(marsWeatherData.temperature)}\n")
                        if (marsWeatherData.minTemperature != null) {
                            append("Min: ${formatTemperature(marsWeatherData.minTemperature)}\n")
                        }
                        if (marsWeatherData.maxTemperature != null) {
                            append("Max: ${formatTemperature(marsWeatherData.maxTemperature)}")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Wind info
                if (marsWeatherData.windSpeed != null) {
                    val windContent = buildString {
                        append("Vitesse moyenne: ${formatWindSpeed(marsWeatherData.windSpeed)}\n")
                        if (marsWeatherData.windDirection != null) {
                            append("Direction: ${marsWeatherData.windDirection}")
                        }
                    }
                    
                    DetailCard(
                        title = "Vent",
                        content = windContent
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Pressure info
                DetailCard(
                    title = "Pression Atmosphérique",
                    content = "Moyenne: ${formatPressure(marsWeatherData.pressure)}"
                )
            }
        }
    }
}

@Composable
fun DetailCard(title: String, content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Divider(
                color = Color.Gray,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Text(
                text = content,
                fontSize = 16.sp,
                color = Color.White,
                lineHeight = 24.sp
            )
        }
    }
}

// Helper functions to format data
private fun formatTemperature(value: Double?): String {
    return if (value != null) {
        String.format("%.1f°C", value)
    } else {
        "N/A"
    }
}

private fun formatWindSpeed(value: Double?): String {
    return if (value != null) {
        String.format("%.1f m/s", value)
    } else {
        "N/A"
    }
}

private fun formatPressure(value: Double?): String {
    return if (value != null) {
        String.format("%.2f Pa", value)
    } else {
        "N/A"
    }
}

private fun formatDate(dateStr: String?): String {
    if (dateStr.isNullOrEmpty()) return "N/A"
    
    try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)
        
        val date = inputFormat.parse(dateStr)
        return outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        return dateStr // Return original string if parsing fails
    }
} 