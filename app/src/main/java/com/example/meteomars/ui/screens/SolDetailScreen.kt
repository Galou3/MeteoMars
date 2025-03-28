package com.example.meteomars.ui.screens

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meteomars.model.MarsWeatherData
import com.example.meteomars.ui.components.WindRoseChart
import com.example.meteomars.ui.theme.DarkBackground
import com.example.meteomars.ui.theme.TealGrid
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SolDetailScreen(
    marsWeatherData: MarsWeatherData,
    onBackClick: () -> Unit,
    onNextSol: (MarsWeatherData) -> Unit = {},
    onPreviousSol: (MarsWeatherData) -> Unit = {},
    allWeatherData: List<MarsWeatherData> = emptyList()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Debug vars to show scroll values
    var lastScrollValue by remember { mutableStateOf(0) }
    var lastWheelValue by remember { mutableStateOf(0f) }
    
    // Find current sol index in the list
    val currentSolIndex = allWeatherData.indexOfFirst { it.sol == marsWeatherData.sol }
    val hasPreviousSol = currentSolIndex > 0
    val hasNextSol = currentSolIndex < allWeatherData.size - 1 && currentSolIndex >= 0
    
    // Track when reaching scroll boundaries
    var atStart by remember { mutableStateOf(true) }
    var atEnd by remember { mutableStateOf(false) }
    
    // Monitor scroll position
    LaunchedEffect(scrollState.value) {
        lastScrollValue = scrollState.value
        atStart = scrollState.value <= 5
        atEnd = scrollState.value >= scrollState.maxValue - 5
    }
    
    // Reset scroll position when changing sols
    LaunchedEffect(marsWeatherData.sol) {
        scrollState.scrollTo(0)
    }

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
                ),
                actions = {
                    // Navigation indicators
                    if (hasPreviousSol) {
                        IconButton(onClick = { 
                            if (currentSolIndex > 0) {
                                onPreviousSol(allWeatherData[currentSolIndex - 1])
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Previous Sol",
                                tint = Color.White
                            )
                        }
                    }
                    
                    if (hasNextSol) {
                        IconButton(onClick = { 
                            if (currentSolIndex >= 0 && currentSolIndex < allWeatherData.size - 1) {
                                onNextSol(allWeatherData[currentSolIndex + 1])
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Next Sol",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
            
            // Instruction for scrolling navigation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF303030))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Utilisez la molette pour naviguer entre les sols",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
            
            // Main content with mouse wheel interceptor
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInteropFilter { event ->
                        if (event.action == MotionEvent.ACTION_SCROLL) {
                            val scrollY = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
                            lastWheelValue = scrollY
                            
                            // Sur un émulateur, les valeurs peuvent être inversées par rapport
                            // à un appareil physique ou PC
                            
                            // scrollY > 0 = scroll vers le haut (précédent)
                            if (atStart && scrollY > 0 && hasPreviousSol) {
                                coroutineScope.launch {
                                    onPreviousSol(allWeatherData[currentSolIndex - 1])
                                }
                                return@pointerInteropFilter true
                            }
                            
                            // scrollY < 0 = scroll vers le bas (suivant)
                            if (atEnd && scrollY < 0 && hasNextSol) {
                                coroutineScope.launch {
                                    onNextSol(allWeatherData[currentSolIndex + 1])
                                }
                                return@pointerInteropFilter true
                            }
                        }
                        false
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Concise Temperature display
                    Text(
                        text = "Température : avg: ${formatTemperature(marsWeatherData.temperature)} min: ${formatTemperature(marsWeatherData.minTemperature)} max: ${formatTemperature(marsWeatherData.maxTemperature)}",
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    // Concise Pressure display
                    Text(
                        text = "Pression : avg: ${formatPressure(marsWeatherData.pressure)} min: ${formatPressureMinMax(marsWeatherData.pressure * 0.95)} max: ${formatPressureMinMax(marsWeatherData.pressure * 1.05)}",
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Wind Rose Chart
                    if (marsWeatherData.windDirectionMap.isNotEmpty()) {
                        WindRoseChart(
                            windDirectionMap = marsWeatherData.windDirectionMap,
                            maxValue = marsWeatherData.maxWindDirectionValue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(350.dp),
                            circleColor = Color(0xFFD8D0CE),
                            triangleColor = Color(0xFF82B3D6)
                        )
                    } else {
                        // Generate sample data for the wind rose chart for demo purposes
                        val sampleWindData = generateSampleWindData()
                        WindRoseChart(
                            windDirectionMap = sampleWindData.first,
                            maxValue = sampleWindData.second,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(350.dp),
                            circleColor = Color(0xFFD8D0CE),
                            triangleColor = Color(0xFF82B3D6)
                        )
                    }
                    
                    // Indicateurs de navigation en bas pour rappeler qu'on peut scroller
                    if (hasNextSol && atEnd) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "⬇️ Sol suivant disponible (scroll vers le bas)",
                            color = Color.Yellow,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                    
                    // Extra space to ensure we can scroll fully
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}

/**
 * Generate sample wind direction data for demo purposes
 * Returns a Pair of (windDirectionMap, maxValue)
 */
private fun generateSampleWindData(): Pair<Map<Int, Double>, Double> {
    val windMap = mutableMapOf<Int, Double>()
    var maxValue = 0.0
    
    // Sample data similar to the image
    // Direction 0 (North) has a small value
    windMap[0] = 500.0
    
    // Directions 12-15 (NW quadrant) have large values
    windMap[12] = 2000.0
    windMap[13] = 3000.0
    windMap[14] = 3500.0
    windMap[15] = 800.0
    
    // Direction 9-11 (SW quadrant) have medium values
    windMap[9] = 400.0
    windMap[10] = 800.0
    windMap[11] = 1200.0
    
    // Find max value
    windMap.values.forEach { value ->
        if (value > maxValue) maxValue = value
    }
    
    return Pair(windMap, maxValue)
}

// Helper functions to format data
private fun formatTemperature(value: Double?): String {
    return if (value != null) {
        String.format("%.2f", value)
    } else {
        "N/A"
    }
}

private fun formatPressure(value: Double?): String {
    return if (value != null) {
        String.format("%.2f", value)
    } else {
        "N/A"
    }
}

private fun formatPressureMinMax(value: Double?): String {
    return if (value != null) {
        String.format("%.2f", value)
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