package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GameUiState
import kotlin.math.hypot
import kotlin.math.roundToInt

@Composable
fun GameControlsOverlay(
    uiState: GameUiState,
    onJoystickMove: (Float, Float) -> Unit,
    onRotateCamera: (Float) -> Unit,
    onPitchCamera: (Float) -> Unit,
    onZoomCamera: (Float) -> Unit,
    onJump: () -> Unit,
    onContextAction: () -> Unit,
    onToggleEmoteWheel: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleDeafen: () -> Unit,
    onPttPressChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // 1. Zona de arrastre para rotar la cámara 360° en 3ra persona (Mitad superior y derecha)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onRotateCamera(dragAmount.x)
                        onPitchCamera(dragAmount.y)
                    }
                }
        )

        // 2. Banner de ¡Gooool! o Celebración
        AnimatedVisibility(
            visible = uiState.goalBannerText != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFE11D48),
                shadowElevation = 8.dp,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = uiState.goalBannerText ?: "",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }

        // 3. Minijuego de Pesca activo (Barra de tensión y carrete)
        if (uiState.fishingState.isFishing) {
            FishingOverlay(
                hasBite = uiState.fishingState.hasBite,
                progress = uiState.fishingState.reelProgress,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 60.dp)
            )
        }

        // 4. Panel de controles inferiores
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Joystick Virtual en esquina inferior izquierda
            VirtualJoystick(
                onMove = onJoystickMove,
                modifier = Modifier.testTag("virtual_joystick")
            )

            // Panel de Botones de Acción en esquina inferior derecha
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Controles de Zoom 3D (Cámara Cerca / Lejos)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = { onZoomCamera(-2.5f) },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xAA1E293B),
                            contentColor = Color(0xFF38BDF8)
                        ),
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("zoom_in_btn")
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In Cámara 3D", modifier = Modifier.size(20.dp))
                    }

                    FilledIconButton(
                        onClick = { onZoomCamera(2.5f) },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xAA1E293B),
                            contentColor = Color(0xFF38BDF8)
                        ),
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("zoom_out_btn")
                    ) {
                        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out Cámara 3D", modifier = Modifier.size(20.dp))
                    }
                }

                // Fila de botones secundarios: Emotes y Micrófono
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón de Emotes
                    FilledIconButton(
                        onClick = onToggleEmoteWheel,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xFF334155).copy(alpha = 0.9f),
                            contentColor = Color(0xFFFBBF24)
                        ),
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("emote_wheel_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "Rueda de Emoticones",
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Botón de Mute de Micrófono
                    FilledIconButton(
                        onClick = onToggleMute,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (uiState.isMicMuted) Color(0xFFEF4444) else Color(0xFF10B981),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("mic_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = if (uiState.isMicMuted) "Desmutear micrófono" else "Mutear micrófono",
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Botón para ensordecer (Deafen)
                    FilledIconButton(
                        onClick = onToggleDeafen,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (uiState.isDeafened) Color(0xFFDC2626) else Color(0xFF475569).copy(alpha = 0.85f),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("deafen_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isDeafened) Icons.Default.VolumeOff else Icons.Default.Headphones,
                            contentDescription = "Ensordecer audio",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Botón de Acción Contextual Grande ("Patear balón", "Pescar", "Sentarse")
                Button(
                    onClick = onContextAction,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .height(56.dp)
                        .testTag("context_action_button")
                ) {
                    Text(
                        text = uiState.contextActionLabel ?: "Interactuar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // Botón de Salto
                Button(
                    onClick = onJump,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .size(62.dp)
                        .testTag("jump_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowUpward,
                        contentDescription = "Saltar",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

/**
 * Joystick analógico virtual con arrastre táctil suave y recentrado automático.
 */
@Composable
fun VirtualJoystick(
    onMove: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxRadiusPx = 110f
    var knobOffsetX by remember { mutableFloatStateOf(0f) }
    var knobOffsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .size(130.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x661E293B), Color(0x990F172A))
                )
            )
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { },
                    onDragEnd = {
                        knobOffsetX = 0f
                        knobOffsetY = 0f
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        knobOffsetX = 0f
                        knobOffsetY = 0f
                        onMove(0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newX = knobOffsetX + dragAmount.x
                        val newY = knobOffsetY + dragAmount.y
                        val dist = hypot(newX, newY)

                        if (dist <= maxRadiusPx) {
                            knobOffsetX = newX
                            knobOffsetY = newY
                        } else {
                            knobOffsetX = (newX / dist) * maxRadiusPx
                            knobOffsetY = (newY / dist) * maxRadiusPx
                        }

                        val normX = (knobOffsetX / maxRadiusPx).coerceIn(-1f, 1f)
                        val normY = (knobOffsetY / maxRadiusPx).coerceIn(-1f, 1f)
                        onMove(normX, normY)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Círculo central decorativo
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0x33FFFFFF))
        )

        // Perilla móvil del joystick
        Box(
            modifier = Modifier
                .offset { IntOffset(knobOffsetX.roundToInt(), knobOffsetY.roundToInt()) }
                .size(54.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF60A5FA), Color(0xFF2563EB))
                    )
                )
        )
    }
}

@Composable
fun FishingOverlay(
    hasBite: Boolean,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xEE0F172A),
        shadowElevation = 8.dp,
        modifier = modifier.padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (hasBite) "🎣 ¡PICÓ EL ANZUELO! ¡JALA RÁPIDO!" else "🌊 Esperando que pique el pez...",
                color = if (hasBite) Color(0xFF38BDF8) else Color(0xFFCBD5E1),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            // Barra de progreso de captura
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF334155))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0.05f, 1f))
                        .height(12.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF38BDF8), Color(0xFF10B981))
                            )
                        )
                )
            }
        }
    }
}
