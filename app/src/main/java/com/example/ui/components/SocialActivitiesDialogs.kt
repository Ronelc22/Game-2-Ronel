package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AvatarConfig
import com.example.model.EmoteType
import com.example.model.FishCatch

@Composable
fun EmoteWheelDialog(
    onSelectEmote: (EmoteType) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xF50F172A),
            shadowElevation = 12.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rueda de Emoticones",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cuadrícula de emociones
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val emotes = EmoteType.values().toList()
                    emotes.chunked(2).forEach { rowEmotes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowEmotes.forEach { emote ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onSelectEmote(emote) }
                                        .testTag("emote_${emote.name.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(text = emote.icon, fontSize = 26.sp)
                                        Text(
                                            text = emote.label,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
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

@Composable
fun AvatarStudioDialog(
    initialConfig: AvatarConfig,
    onSaveConfig: (AvatarConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var nickname by remember { mutableStateOf(initialConfig.nickname) }
    var selectedSkin by remember { mutableStateOf(initialConfig.skinTone) }
    var selectedShirt by remember { mutableStateOf(initialConfig.shirtColor) }
    var selectedHair by remember { mutableStateOf(initialConfig.hairColor) }
    var selectedHeadset by remember { mutableStateOf(initialConfig.headsetColor) }
    var hairStyle by remember { mutableIntStateOf(initialConfig.hairStyleIndex) }
    var accessory by remember { mutableIntStateOf(initialConfig.accessoryIndex) }

    val skinPalette = listOf(Color(0xFFFFE0B2), Color(0xFFF1C27D), Color(0xFFE0AC69), Color(0xFF8D5524))
    val shirtPalette = listOf(Color(0xFF81BFBC), Color(0xFFC9D5C3), Color(0xFFE07A5F), Color(0xFFF28482), Color(0xFFF4A261), Color(0xFF3B82F6))
    val headsetPalette = listOf(Color(0xFF0D9488), Color(0xFF8B5CF6), Color(0xFF06B6D4), Color(0xFF10B981), Color(0xFFF43F5E), Color(0xFFFBBF24))

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xF50F172A),
            shadowElevation = 12.dp,
            modifier = Modifier.padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Estudio de Avatar",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it.take(16) },
                    label = { Text("Nombre del Personaje") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("avatar_nickname_field")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Color de Camiseta
                Text("Color de Camiseta", color = Color(0xFF94A3B8), fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    items(shirtPalette) { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selectedShirt == color) 3.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .clickable { selectedShirt = color }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Color de Auriculares (Headset)
                Text("Color de Headset Gamer", color = Color(0xFF94A3B8), fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    items(headsetPalette) { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selectedHeadset == color) 3.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .clickable { selectedHeadset = color }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Estilo de Peinado / Accesorios
                Text("Peinado / Orejitas", color = Color(0xFF94A3B8), fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    val styles = listOf("Corto", "Con Estilo", "Gorra", "🦊 Zorro")
                    styles.forEachIndexed { index, name ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (hairStyle == index) Color(0xFF3B82F6) else Color(0xFF1E293B)
                            ),
                            modifier = Modifier.clickable { hairStyle = index }
                        ) {
                            Text(
                                text = name,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onSaveConfig(
                            AvatarConfig(
                                nickname = if (nickname.isBlank()) "Explorador" else nickname,
                                skinTone = selectedSkin,
                                shirtColor = selectedShirt,
                                hairColor = selectedHair,
                                headsetColor = selectedHeadset,
                                hairStyleIndex = hairStyle,
                                accessoryIndex = accessory
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("avatar_save_btn")
                ) {
                    Text("Guardar Avatar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun CampfireGuitarDialog(
    onPlayChord: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val chords = listOf("Do (C)", "Sol (G)", "La menor (Am)", "Fa (F)", "Mi menor (Em)", "Re (D)")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xF50F172A),
            shadowElevation = 12.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🔥", fontSize = 22.sp)
                        Text(
                            text = "Guitarra en la Fogata",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
                    }
                }

                Text(
                    text = "Toca los acordes para interpretar melodías acústicas. Los amigos cercanos te escucharán con audio espacial.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    chords.chunked(2).forEach { rowChords ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowChords.forEach { chord ->
                                Button(
                                    onClick = { onPlayChord(chord) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .testTag("chord_${chord.take(2)}")
                                ) {
                                    Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(chord, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoomCodeDialog(
    roomCode: String,
    isPushToTalk: Boolean,
    onTogglePushToTalk: (Boolean) -> Unit,
    voiceThreshold: Float,
    onVoiceThresholdChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xF50F172A),
            shadowElevation = 12.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sala de Amigos",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Código de tu Isla:",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = roomCode,
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            letterSpacing = 2.sp
                        )
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copiar código", tint = Color(0xFF38BDF8))
                    }
                }

                Text(
                    text = "Tus amigos pueden entrar con este código para escucharse por proximidad en 3D.",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Ajuste de Modo de Voz
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Pulsar para hablar (PTT)", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(if (isPushToTalk) "Activo: mantén presionado el botón" else "Micrófono abierto con umbral", color = Color.Gray, fontSize = 11.sp)
                    }
                    Switch(
                        checked = isPushToTalk,
                        onCheckedChange = onTogglePushToTalk
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sensibilidad de micrófono
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Sensibilidad del micrófono: ${(voiceThreshold * 100).toInt()}%",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    )
                    Slider(
                        value = voiceThreshold,
                        onValueChange = onVoiceThresholdChange,
                        valueRange = 0.05f..0.35f
                    )
                }
            }
        }
    }
}

@Composable
fun FishResultDialog(
    fish: FishCatch,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xF50F172A),
            shadowElevation = 12.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🎣", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "¡Gran Captura!",
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                )
                Text(
                    text = fish.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Rareza: ${fish.rarity}", color = Color(0xFFFBBF24), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Peso: ${fish.weightKg} kg", color = Color(0xFF34D399), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("fish_result_ok_btn")
                ) {
                    Text("¡Genial!", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
