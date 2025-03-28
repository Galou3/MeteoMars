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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import androidx.compose.material3.AlertDialog
import android.content.Intent
import com.example.meteomars.CommandHistoryManager

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
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Pour les tests - simuler une connexion réussie au serveur
    var isSimulationMode by remember { mutableStateOf(false) }
    var commandHistory by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // État pour l'affichage de l'historique
    var showHistory by remember { mutableStateOf(false) }
    
    // Vérifier la connexion au démarrage
    LaunchedEffect(Unit) {
        isLoading = true
        delay(1000) // Simulation d'un délai de connexion
        
        try {
            // Essayer de se connecter au serveur
            testConnection(context, 
                onSuccess = {
                    isLoading = false
                    lastResponse = "Connexion au serveur réussie"
                    isSimulationMode = false
                },
                onError = { error ->
                    isLoading = false
                    errorMessage = error
                    // Mode simulation si la connexion échoue
                    isSimulationMode = true
                    lastResponse = "Mode simulation activé (pas de connexion au serveur)"
                }
            )
        } catch (e: Exception) {
            isLoading = false
            errorMessage = e.message
            isSimulationMode = true
            lastResponse = "Mode simulation activé (erreur: ${e.message})"
        }
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
                // Indicateur de mode simulation si actif
                if (isSimulationMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF5D4037), shape = CircleShape)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Mode simulation (sans serveur)",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
                
                // Status indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .background(Color(0xFF303030), shape = CircleShape)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White, 
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = if (motorsStarted) "Moteurs: En marche" else "Moteurs: À l'arrêt",
                            color = if (motorsStarted) Color.Green else Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
                
                // Motor control buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Start Motor button
                    Button(
                        onClick = {
                            isLoading = true
                            if (isSimulationMode) {
                                // Simulation locale
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(500) // Simuler un délai de réseau
                                    lastResponse = "SUCCESS: Moteurs démarrés"
                                    commandHistory = commandHistory + "START"
                                    motorsStarted = true
                                    isLoading = false
                                }
                            } else {
                                // Envoyer la commande réelle
                                sendCommand("START", context) { response ->
                                    lastResponse = response
                                    // Considérer toute réponse qui ne contient pas "ERREUR" comme un succès
                                    if (!response.contains("ERREUR", ignoreCase = true) && 
                                        !response.contains("ERROR", ignoreCase = true)) {
                                        motorsStarted = true
                                    }
                                    // Ajouter à l'historique des commandes
                                    commandHistory = commandHistory + "START"
                                    // Ajouter également à l'historique global via le gestionnaire
                                    CommandHistoryManager.addCommand(context, "START")
                                    // Afficher dans le Toast pour déboggage
                                    Toast.makeText(context, "Commande: START, Réponse: $response", Toast.LENGTH_SHORT).show()
                                    isLoading = false
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
                            isLoading = true
                            if (isSimulationMode) {
                                // Simulation locale
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(500) // Simuler un délai de réseau
                                    lastResponse = "SUCCESS: Moteurs arrêtés"
                                    commandHistory = commandHistory + "STOP"
                                    motorsStarted = false
                                    isLoading = false
                                }
                            } else {
                                // Envoyer la commande réelle
                                sendCommand("STOP", context) { response ->
                                    lastResponse = response
                                    // Considérer toute réponse qui ne contient pas "ERREUR" comme un succès
                                    if (!response.contains("ERREUR", ignoreCase = true) && 
                                        !response.contains("ERROR", ignoreCase = true)) {
                                        motorsStarted = false
                                    }
                                    // Ajouter à l'historique des commandes
                                    commandHistory = commandHistory + "STOP"
                                    // Ajouter également à l'historique global via le gestionnaire
                                    CommandHistoryManager.addCommand(context, "STOP")
                                    // Afficher dans le Toast pour déboggage
                                    Toast.makeText(context, "Commande: STOP, Réponse: $response", Toast.LENGTH_SHORT).show()
                                    isLoading = false
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
                            isLoading = true
                            if (isSimulationMode) {
                                // Simulation locale
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(500) // Simuler un délai de réseau
                                    lastResponse = "SUCCESS: Avance tout droit"
                                    commandHistory = commandHistory + "DIRECT_FRONT"
                                    isLoading = false
                                }
                            } else {
                                // Envoyer la commande directement
                                sendCommand("DIRECT_FRONT", context) { response ->
                                    lastResponse = response
                                    // Vérifier si les moteurs sont coupés dans la réponse
                                    if (response.contains("MOTEURS SONT COUPES", ignoreCase = true)) {
                                        motorsStarted = false
                                    }
                                    // Ajouter à l'historique des commandes
                                    commandHistory = commandHistory + "DIRECT_FRONT"
                                    // Ajouter également à l'historique global via le gestionnaire
                                    CommandHistoryManager.addCommand(context, "DIRECT_FRONT")
                                    // Afficher dans le Toast pour déboggage
                                    Toast.makeText(context, "Commande: DIRECT_FRONT, Réponse: $response", Toast.LENGTH_SHORT).show()
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading,
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
                                isLoading = true
                                if (isSimulationMode) {
                                    // Simulation locale
                                    CoroutineScope(Dispatchers.Main).launch {
                                        delay(500) // Simuler un délai de réseau
                                        lastResponse = "SUCCESS: Tourne à gauche"
                                        commandHistory = commandHistory + "DIRECT_LEFT"
                                        isLoading = false
                                    }
                                } else {
                                    // Envoyer la commande directement
                                    sendCommand("DIRECT_LEFT", context) { response ->
                                        lastResponse = response
                                        // Vérifier si les moteurs sont coupés dans la réponse
                                        if (response.contains("MOTEURS SONT COUPES", ignoreCase = true)) {
                                            motorsStarted = false
                                        }
                                        // Ajouter à l'historique des commandes
                                        commandHistory = commandHistory + "DIRECT_LEFT"
                                        // Ajouter également à l'historique global via le gestionnaire
                                        CommandHistoryManager.addCommand(context, "DIRECT_LEFT")
                                        // Afficher dans le Toast pour déboggage
                                        Toast.makeText(context, "Commande: DIRECT_LEFT, Réponse: $response", Toast.LENGTH_SHORT).show()
                                        isLoading = false
                                    }
                                }
                            },
                            enabled = !isLoading,
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
                                isLoading = true
                                if (isSimulationMode) {
                                    // Simulation locale
                                    CoroutineScope(Dispatchers.Main).launch {
                                        delay(500) // Simuler un délai de réseau
                                        lastResponse = "SUCCESS: Tourne à droite"
                                        commandHistory = commandHistory + "DIRECT_RIGHT"
                                        isLoading = false
                                    }
                                } else {
                                    // Envoyer la commande directement
                                    sendCommand("DIRECT_RIGHT", context) { response ->
                                        lastResponse = response
                                        // Vérifier si les moteurs sont coupés dans la réponse
                                        if (response.contains("MOTEURS SONT COUPES", ignoreCase = true)) {
                                            motorsStarted = false
                                        }
                                        // Ajouter à l'historique des commandes
                                        commandHistory = commandHistory + "DIRECT_RIGHT"
                                        // Ajouter également à l'historique global via le gestionnaire
                                        CommandHistoryManager.addCommand(context, "DIRECT_RIGHT")
                                        // Afficher dans le Toast pour déboggage
                                        Toast.makeText(context, "Commande: DIRECT_RIGHT, Réponse: $response", Toast.LENGTH_SHORT).show()
                                        isLoading = false
                                    }
                                }
                            },
                            enabled = !isLoading,
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
                
                // Bouton pour voir l'historique
                Button(
                    onClick = { 
                        // Lancer l'activité d'historique
                        val intent = Intent(context, CommandHistoryActivity::class.java)
                        intent.putExtra("COMMAND_HISTORY", commandHistory.toTypedArray())
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF673AB7),
                        disabledContainerColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("VOIR L'HISTORIQUE (${commandHistory.size})")
                }
            }
        }
        
        // Indicateur de chargement global
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun testConnection(
    context: android.content.Context,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    // Adresse spéciale qui pointe vers le localhost de la machine hôte depuis l'émulateur
    val serverIp = "10.0.2.2"
    val serverPort = 1056
    
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Tenter de créer une connexion
            val socket = Socket(serverIp, serverPort)
            
            // Si la connexion réussit, on ferme la socket
            socket.close()
            
            // Notifier du succès sur le thread principal
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, "Connexion réussie à $serverIp:$serverPort", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
        } catch (e: Exception) {
            // Essayer avec une autre adresse IP
            try {
                val alternativeIp = "127.0.0.1"
                val socket = Socket(alternativeIp, serverPort)
                socket.close()
                
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Connexion réussie à $alternativeIp:$serverPort", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }
            } catch (e2: Exception) {
                // Notifier de l'erreur sur le thread principal
                CoroutineScope(Dispatchers.Main).launch {
                    onError("Erreur de connexion: impossible de se connecter au serveur")
                }
            }
        }
    }
}

private fun sendCommand(command: String, context: android.content.Context, callback: (String) -> Unit) {
    // Adresse spéciale qui pointe vers le localhost de la machine hôte depuis l'émulateur
    val serverIp = "10.0.2.2" 
    val serverPort = 1056
    
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
                // Nous ne gérons plus l'état motorsStarted ici - il sera géré par le Composable
                callback(response)
            }
            
            // Close the socket
            socket.close()
        } catch (e: Exception) {
            // Essayer avec une autre adresse IP
            try {
                val alternativeIp = "127.0.0.1"
                val socket = Socket(alternativeIp, serverPort)
                val output = PrintWriter(socket.getOutputStream(), true)
                val input = BufferedReader(InputStreamReader(socket.getInputStream()))
                
                output.println(command)
                val response = input.readLine() ?: "No response"
                
                CoroutineScope(Dispatchers.Main).launch {
                    // Nous ne gérons plus l'état motorsStarted ici - il sera géré par le Composable
                    callback(response)
                }
                
                socket.close()
            } catch (e2: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Erreur: impossible de se connecter au serveur", Toast.LENGTH_LONG).show()
                    callback("Erreur: impossible de se connecter au serveur")
                }
            }
        }
    }
} 