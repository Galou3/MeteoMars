package com.example.meteomars

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meteomars.model.MarsWeatherData
import com.example.meteomars.ui.theme.DarkBackground
import com.example.meteomars.ui.theme.HistoryButtonRed
import com.example.meteomars.ui.theme.MeteoMarsTheme
import com.example.meteomars.ui.screens.HomeScreen
import com.example.meteomars.ui.screens.SolDetailScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeteoMarsTheme {
                MainAppNavigation()
            }
        }
    }
}

@Composable
fun MainAppNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
    var selectedSolData by remember { mutableStateOf<MarsWeatherData?>(null) }
    var allWeatherData by remember { mutableStateOf<List<MarsWeatherData>>(emptyList()) }
    
    when (val screen = currentScreen) {
        is Screen.Splash -> SplashScreen(onStartClick = { currentScreen = Screen.Home })
        is Screen.Home -> HomeScreen(
            onSolSelected = { solData -> 
                selectedSolData = solData
                currentScreen = Screen.SolDetail 
            },
            onWeatherDataLoaded = { weatherDataList ->
                allWeatherData = weatherDataList
            }
        )
        is Screen.SolDetail -> {
            selectedSolData?.let { solData ->
                SolDetailScreen(
                    marsWeatherData = solData,
                    onBackClick = { currentScreen = Screen.Home },
                    onNextSol = { nextSolData ->
                        selectedSolData = nextSolData
                    },
                    onPreviousSol = { prevSolData ->
                        selectedSolData = prevSolData
                    },
                    allWeatherData = allWeatherData
                )
            } ?: run {
                // If no sol data, go back to home
                currentScreen = Screen.Home
            }
        }
    }
}

sealed class Screen {
    object Splash : Screen()
    object Home : Screen()
    object SolDetail : Screen()
}

@Composable
fun SplashScreen(onStartClick: () -> Unit = {}) {
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Android icon with green grid background
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Grid background
                Image(
                    painter = painterResource(id = R.drawable.grid_pattern),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Android logo
                Image(
                    painter = painterResource(id = R.drawable.ic_android_robot),
                    contentDescription = "Android Icon",
                    modifier = Modifier.size(60.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // "Quelle météo sur Mars ?" text
            Text(
                text = "Quelle météo sur Mars ?",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(100.dp))
            
            // "COMMENCER" button
            Button(
                onClick = onStartClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HistoryButtonRed
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .width(220.dp)
                    .height(50.dp)
            ) {
                Text(
                    text = "COMMENCER",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // "VOIR L'HISTORIQUE" button (can be implemented later)
            Button(
                onClick = { /* History functionality to be implemented */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = HistoryButtonRed
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .width(220.dp)
                    .height(50.dp)
            ) {
                Text(
                    text = "VOIR L'HISTORIQUE",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // "DIRIGER" button to open RobotControlActivity
            Button(
                onClick = { 
                    val intent = Intent(context, RobotControlActivity::class.java)
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = HistoryButtonRed
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .width(220.dp)
                    .height(50.dp)
            ) {
                Text(
                    text = "DIRIGER",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    MeteoMarsTheme {
        SplashScreen()
    }
}
