package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhonelinkRing
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActivityZone
import com.example.viewmodel.GameUiState
import kotlin.math.roundToInt

@Composable
fun TopGameHud(
    uiState: GameUiState,
    onOpenAvatarStudio: () -> Unit,
    onOpenRoomDialog: () -> Unit,
    onOpenFishLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showNearbyList by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Barra Superior
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Insignia de Zona Actual & Marcador
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xCC0F172A),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = uiState.currentZone.icon, fontSize = 16.sp)
                        Text(
                            text = uiState.currentZone.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Marcador si está en la cancha de fútbol
                if (uiState.currentZone == ActivityZone.SOCCER) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xDD1E293B)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔴 ${uiState.redScore} - ${uiState.blueScore} 🔵", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }

                // Estrellas recolectadas
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xDD1E293B)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⭐ ${uiState.collectedStarCount}/${uiState.totalStars}", color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // Encomienda activa de Mensajero o contador de entregas
                if (uiState.activeDelivery != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFC2410C)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📦 Para: ${uiState.activeDelivery.recipientName}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                } else if (uiState.deliveredCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF0F766E)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📬 ${uiState.deliveredCount} entregas", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Botones de acción del HUD: Sala, Avatar, Pescados, Lista de Amigos
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicador de Voz en Vivo
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (uiState.localPlayer.isSpeaking) Color(0xFF059669) else Color(0xCC1E293B),
                    modifier = Modifier.clickable { showNearbyList = !showNearbyList }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Estado de voz",
                            tint = if (uiState.isMicMuted) Color(0xFFEF4444) else Color(0xFF34D399),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${uiState.nearbyPlayers.count { it.isWithinVoiceRange }} cerca",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Botón Personalizar Avatar
                IconButton(
                    onClick = onOpenAvatarStudio,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xCC1E293B), CircleShape)
                        .testTag("avatar_studio_btn")
                ) {
                    Icon(Icons.Default.Palette, contentDescription = "Personalizar Avatar", tint = Color(0xFF60A5FA), modifier = Modifier.size(18.dp))
                }

                // Botón Código de Sala
                IconButton(
                    onClick = onOpenRoomDialog,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xCC1E293B), CircleShape)
                        .testTag("room_dialog_btn")
                ) {
                    Icon(Icons.Default.Group, contentDescription = "Compartir Sala con Amigos", tint = Color(0xFFA78BFA), modifier = Modifier.size(18.dp))
                }
            }
        }

        // Radar desplegable de amigos cercanos y nivel de volumen por proximidad
        AnimatedVisibility(visible = showNearbyList) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xF00F172A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📻 Radar de Voz por Proximidad (18m)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        IconButton(onClick = { showNearbyList = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (uiState.nearbyPlayers.isEmpty()) {
                        Text(
                            text = "Buscando amigos en la isla...",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.height(130.dp)
                        ) {
                            items(uiState.nearbyPlayers) { info ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0x331E293B), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(if (info.isWithinVoiceRange) Color(0xFF10B981) else Color(0xFF64748B))
                                        )
                                        Column {
                                            Text(
                                                text = info.player.name,
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = "${(info.distanceMeters * 10).roundToInt() / 10f} m ${if (info.isWithinVoiceRange) "• En rango" else "• Muy lejos"}",
                                                color = if (info.isWithinVoiceRange) Color(0xFF34D399) else Color(0xFF94A3B8),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    // Indicador de volumen espacial
                                    if (info.isWithinVoiceRange) {
                                        val vol = ((info.leftGain + info.rightGain) * 50f).coerceIn(5f, 100f).roundToInt()
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VolumeUp,
                                                contentDescription = null,
                                                tint = Color(0xFF38BDF8),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(text = "$vol%", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Campo rápido para enviar mensajes de chat de texto con bocadillo sobre el avatar
 */
@Composable
fun QuickChatBar(
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xCC0F172A),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Escribe para decir algo en voz alta...", color = Color.Gray, fontSize = 13.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_text_input")
            )

            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onSendMessage(text)
                        text = ""
                    }
                },
                modifier = Modifier.testTag("chat_send_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color(0xFF38BDF8))
            }
        }
    }
}
