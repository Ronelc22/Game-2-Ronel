package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.components.AvatarStudioDialog
import com.example.ui.components.CampfireGuitarDialog
import com.example.ui.components.EmoteWheelDialog
import com.example.ui.components.FishResultDialog
import com.example.ui.components.GameControlsOverlay
import com.example.ui.components.QuickChatBar
import com.example.ui.components.RoomCodeDialog
import com.example.ui.components.TopGameHud
import com.example.ui.components.WorldRenderer
import com.example.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // 1. Renderizado en 3D / Tercera Persona del Mundo Abierto
        WorldRenderer(
            uiState = uiState,
            world = viewModel.world,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Controles de Juego táctiles (Joystick, Salto, Interacción, Giro de Cámara)
        GameControlsOverlay(
            uiState = uiState,
            onJoystickMove = viewModel::onJoystickMove,
            onRotateCamera = viewModel::onRotateCamera,
            onPitchCamera = viewModel::onPitchCamera,
            onZoomCamera = viewModel::onZoomCamera,
            onJump = viewModel::jump,
            onContextAction = {
                if (uiState.fishingState.isFishing && uiState.fishingState.hasBite) {
                    viewModel.reelFishingRod()
                } else {
                    viewModel.triggerContextAction()
                }
            },
            onToggleEmoteWheel = { viewModel.toggleEmoteWheel(true) },
            onToggleMute = viewModel::toggleMute,
            onToggleDeafen = viewModel::toggleDeafen,
            onPttPressChange = viewModel::setPttPressed,
            modifier = Modifier.fillMaxSize()
        )

        // 3. Barra Superior (HUD, Zona, Radar de Voz, Botones de Menú)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            TopGameHud(
                uiState = uiState,
                onOpenAvatarStudio = { viewModel.toggleAvatarStudio(true) },
                onOpenRoomDialog = { viewModel.toggleRoomDialog(true) },
                onOpenFishLog = { viewModel.toggleFishLogDialog(true) }
            )

            // Barra rápida de chat de texto
            QuickChatBar(
                onSendMessage = viewModel::sendChatMessage
            )
        }

        // --- DIÁLOGOS Y MODALES INTERACTIVOS ---

        // Rueda de Emoticones
        if (uiState.showEmoteWheel) {
            EmoteWheelDialog(
                onSelectEmote = { emote ->
                    viewModel.triggerEmote(emote)
                },
                onDismiss = { viewModel.toggleEmoteWheel(false) }
            )
        }

        // Estudio de Personalización de Avatar
        if (uiState.showAvatarStudio) {
            AvatarStudioDialog(
                initialConfig = uiState.localPlayer.avatar,
                onSaveConfig = { newConfig ->
                    viewModel.updateAvatar(newConfig)
                },
                onDismiss = { viewModel.toggleAvatarStudio(false) }
            )
        }

        // Guitarra Acústica en la Fogata
        if (uiState.showGuitarDialog) {
            CampfireGuitarDialog(
                onPlayChord = { chord ->
                    viewModel.playGuitarChord(chord)
                },
                onDismiss = { viewModel.toggleGuitarDialog(false) }
            )
        }

        // Código de Sala y Opciones de Voz por Proximidad
        if (uiState.showRoomDialog) {
            RoomCodeDialog(
                roomCode = uiState.roomCode,
                isPushToTalk = uiState.isPushToTalk,
                onTogglePushToTalk = viewModel::togglePushToTalk,
                voiceThreshold = uiState.voiceThreshold,
                onVoiceThresholdChange = viewModel::setVoiceThreshold,
                onDismiss = { viewModel.toggleRoomDialog(false) }
            )
        }

        // Resultado de Trofeo de Pesca
        if (uiState.fishingState.showResultDialog && uiState.fishingState.lastCatch != null) {
            FishResultDialog(
                fish = uiState.fishingState.lastCatch!!,
                onDismiss = viewModel::closeFishResultDialog
            )
        }
    }
}
