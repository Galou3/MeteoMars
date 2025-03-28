package com.example.meteomars

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meteomars.ui.theme.DarkBackground
import com.example.meteomars.ui.theme.MeteoMarsTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class RobotControlActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeteoMarsTheme {
                RobotControlScreen(onBackClick = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RobotControlScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    var motorsStarted by remember { mutableStateOf(false) }
    var lastResponse by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
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
                        "Contrôle du robot",
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .background(Color(0xFF303030), shape = CircleShape)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (motorsStarted) "Moteurs: En marche" else "Moteurs: À l'arrêt",
                        color = if (motorsStarted) Color.Green else Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                
                // Motor control buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Start Motor button
                    Button(
                        onClick = {
                            if (!motorsStarted) {
                                sendCommand("START", context) { response ->
                                    lastResponse = response
                                    if (response.contains("SUCCESS")) {
                                        motorsStarted = true
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Green,
                            disabledContainerColor = Color.Gray
                        ),
                        enabled = !motorsStarted && !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = "Start Motors")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("DÉMARRER")
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Stop Motor button
                    Button(
                        onClick = {
                            if (motorsStarted) {
                                sendCommand("STOP", context) { response ->
                                    lastResponse = response
                                    if (response.contains("SUCCESS")) {
                                        motorsStarted = false
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            disabledContainerColor = Color.Gray
                        ),
                        enabled = motorsStarted && !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Close, contentDescription = "Stop Motors")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ARRÊTER")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Direction controls
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Contrôle de direction",
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Forward button
                    Button(
                        onClick = {
                            if (motorsStarted) {
                                sendCommand("DIRECT_FRONT", context) { response ->
                                    lastResponse = response
                                }
                            } else {
                                Toast.makeText(context, "Les moteurs doivent être démarrés", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = motorsStarted && !isLoading,
                        modifier = Modifier.size(width = 180.dp, height = 60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            disabledContainerColor = Color.Gray
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Forward",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    // Left and Right buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        // Left button
                        Button(
                            onClick = {
                                if (motorsStarted) {
                                    sendCommand("DIRECT_LEFT", context) { response ->
                                        lastResponse = response
                                    }
                                } else {
                                    Toast.makeText(context, "Les moteurs doivent être démarrés", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = motorsStarted && !isLoading,
                            modifier = Modifier.size(width = 80.dp, height = 80.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3),
                                disabledContainerColor = Color.Gray
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Left",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        // Right button
                        Button(
                            onClick = {
                                if (motorsStarted) {
                                    sendCommand("DIRECT_RIGHT", context) { response ->
                                        lastResponse = response
                                    }
                                } else {
                                    Toast.makeText(context, "Les moteurs doivent être démarrés", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = motorsStarted && !isLoading,
                            modifier = Modifier.size(width = 80.dp, height = 80.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3),
                                disabledContainerColor = Color.Gray
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Right",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Response from server
                if (lastResponse.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF303030), shape = CircleShape)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Réponse: $lastResponse",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

private fun sendCommand(command: String, context: android.content.Context, callback: (String) -> Unit) {
    // For demonstration, we'll use a fixed IP and port
    // In a real app, these would come from settings or configuration
    val serverIp = "10.0.2.2" // Default emulator loopback address
    val serverPort = 8080
    
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val socket = Socket(serverIp, serverPort)
            val output = PrintWriter(socket.getOutputStream(), true)
            val input = BufferedReader(InputStreamReader(socket.getInputStream()))
            
            // Send the command
            output.println(command)
            
            // Read the response
            val response = input.readLine() ?: "No response"
            
            // Update UI on main thread
            CoroutineScope(Dispatchers.Main).launch {
                callback(response)
            }
            
            // Close the socket
            socket.close()
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
                callback("Erreur: ${e.message}")
            }
        }
    }
} 