package com.example.meteomars

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meteomars.ui.theme.DarkBackground
import com.example.meteomars.ui.theme.MeteoMarsTheme

// Objet singleton pour gérer l'historique des commandes
object CommandHistoryManager {
    private const val PREFS_NAME = "robot_command_history"
    private const val KEY_HISTORY = "command_history"
    
    // Ajouter une commande à l'historique
    fun addCommand(context: Context, command: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val history = getCommandHistory(context).toMutableList()
        history.add(command)
        
        // Sauvegarder l'historique mis à jour
        prefs.edit().putString(KEY_HISTORY, history.joinToString(",")).apply()
    }
    
    // Obtenir l'historique des commandes
    fun getCommandHistory(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val historyString = prefs.getString(KEY_HISTORY, "") ?: ""
        return if (historyString.isEmpty()) emptyList() else historyString.split(",")
    }
    
    // Effacer l'historique des commandes
    fun clearHistory(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_HISTORY).apply()
    }
}

class CommandHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Récupérer l'historique des commandes depuis les SharedPreferences
        val commandHistory = CommandHistoryManager.getCommandHistory(this)
        
        // Vérifier également si des données ont été passées par intent (pour la compatibilité)
        val extraCommandHistory = intent.getStringArrayExtra("COMMAND_HISTORY")?.toList() ?: emptyList()
        
        // Utiliser l'historique de l'intent s'il n'est pas vide, sinon utiliser celui des SharedPreferences
        val finalCommandHistory = if (extraCommandHistory.isNotEmpty()) extraCommandHistory else commandHistory
        
        setContent {
            MeteoMarsTheme {
                CommandHistoryScreen(
                    commandHistory = finalCommandHistory,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandHistoryScreen(
    commandHistory: List<String>,
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
            // Barre de navigation supérieure
            TopAppBar(
                title = {
                    Text(
                        "Historique des commandes",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF202020)
                )
            )
            
            // Contenu principal avec défilement
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (commandHistory.isEmpty()) {
                    Text(
                        text = "Aucune commande n'a encore été envoyée.",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 32.dp)
                    )
                } else {
                    Text(
                        text = "${commandHistory.size} commandes enregistrées",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Afficher toutes les commandes
                    val displayHistory = commandHistory.reversed()
                    for ((index, command) in displayHistory.withIndex()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when (command) {
                                    "START" -> Color(0xFF4CAF50).copy(alpha = 0.3f)
                                    "STOP" -> Color(0xFFF44336).copy(alpha = 0.3f)
                                    "DIRECT_LEFT" -> Color(0xFF2196F3).copy(alpha = 0.3f)
                                    "DIRECT_RIGHT" -> Color(0xFF2196F3).copy(alpha = 0.3f)
                                    "DIRECT_FRONT" -> Color(0xFF4CAF50).copy(alpha = 0.3f)
                                    else -> Color(0xFF424242).copy(alpha = 0.3f)
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Commande #${displayHistory.size - index}",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                                
                                Text(
                                    text = when (command) {
                                        "START" -> "DÉMARRER"
                                        "STOP" -> "ARRÊTER"
                                        "DIRECT_LEFT" -> "TOURNER À GAUCHE"
                                        "DIRECT_RIGHT" -> "TOURNER À DROITE"
                                        "DIRECT_FRONT" -> "AVANCER TOUT DROIT"
                                        else -> command
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 