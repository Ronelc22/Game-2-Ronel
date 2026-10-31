package com.example.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.VoiceChatEngine
import com.example.model.ActivityZone
import com.example.model.AvatarConfig
import com.example.model.ChatMessage
import com.example.model.DeliveryParcel
import com.example.model.EmoteType
import com.example.model.FishCatch
import com.example.model.Player
import com.example.world.GameWorld
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

data class NearbyFriendInfo(
    val player: Player,
    val distanceMeters: Float,
    val isWithinVoiceRange: Boolean,
    val leftGain: Float,
    val rightGain: Float
)

data class FishingState(
    val isFishing: Boolean = false,
    val hasBite: Boolean = false,
    val tension: Float = 0.5f,
    val reelProgress: Float = 0f,
    val lastCatch: FishCatch? = null,
    val showResultDialog: Boolean = false
)

data class GameUiState(
    val localPlayer: Player = Player(
        id = "player_me",
        name = "Tú",
        x = 40f,
        y = 44f,
        headingAngle = 0f,
        avatar = AvatarConfig(nickname = "Tú", shirtColor = Color(0xFF81BFBC))
    ),
    val companionPlayers: List<Player> = emptyList(),
    val cameraAngle: Float = 0f, // Grados de órbita de cámara alrededor del jugador
    val cameraPitch: Float = 42f, // Ángulo de inclinación 3D
    val cameraDistance: Float = 14f,
    val currentZone: ActivityZone = ActivityZone.FOUNTAIN,
    val contextActionLabel: String? = "Sentarse",
    val nearbyPlayers: List<NearbyFriendInfo> = emptyList(),
    val chatMessages: List<ChatMessage> = emptyList(),
    val collectedStarCount: Int = 0,
    val totalStars: Int = 8,
    val redScore: Int = 0,
    val blueScore: Int = 0,
    val goalBannerText: String? = null,
    val isMicMuted: Boolean = false,
    val isDeafened: Boolean = false,
    val isPushToTalk: Boolean = false,
    val isPttActive: Boolean = false,
    val voiceThreshold: Float = 0.12f,
    val activeEmote: EmoteType? = null,
    val showEmoteWheel: Boolean = false,
    val showAvatarStudio: Boolean = false,
    val showRoomDialog: Boolean = false,
    val showGuitarDialog: Boolean = false,
    val showFishLogDialog: Boolean = false,
    val roomCode: String = "ISLA-7842",
    val fishingState: FishingState = FishingState(),
    val caughtFishList: List<FishCatch> = emptyList(),
    val activeDelivery: DeliveryParcel? = null,
    val deliveredCount: Int = 0
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val voiceEngine = VoiceChatEngine(application.applicationContext)
    val world = GameWorld()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // Control de joystick
    private var joystickDeltaX = 0f
    private var joystickDeltaY = 0f

    init {
        // Inicializar compañeros con posiciones y atuendos únicos
        val companions = listOf(
            Player(
                id = "bot_sofia",
                name = "Sofía",
                x = 42.5f,
                y = 41.5f,
                headingAngle = 180f,
                isBot = true,
                avatar = AvatarConfig(
                    nickname = "Sofía",
                    shirtColor = Color(0xFFEC4899),
                    hairColor = Color(0xFF92400E),
                    headsetColor = Color(0xFFF472B6),
                    hairStyleIndex = 3,
                    accessoryIndex = 1
                ),
                lastSpokenText = "¡Hola! Bienvenidos a Plaza Amistad 🌸",
                speechBubbleTimer = 6f
            ),
            Player(
                id = "bot_lucas",
                name = "Lucas",
                x = 22f,
                y = 38f,
                headingAngle = 45f,
                isBot = true,
                avatar = AvatarConfig(
                    nickname = "Lucas",
                    shirtColor = Color(0xFF10B981),
                    hairColor = Color(0xFF1E293B),
                    headsetColor = Color(0xFF059669),
                    hairStyleIndex = 2
                ),
                lastSpokenText = "¡Ven a la cancha a patear unos penales! ⚽",
                speechBubbleTimer = 5f
            ),
            Player(
                id = "bot_mateo",
                name = "Mateo",
                x = 42f,
                y = 70f,
                headingAngle = 0f,
                isBot = true,
                avatar = AvatarConfig(
                    nickname = "Mateo",
                    shirtColor = Color(0xFFF59E0B),
                    hairColor = Color(0xFFD97706),
                    headsetColor = Color(0xFFD97706),
                    hairStyleIndex = 0
                ),
                lastSpokenText = "El agua está calmada hoy para pescar 🎣",
                speechBubbleTimer = 4f
            ),
            Player(
                id = "bot_valen",
                name = "Valentina",
                x = 64f,
                y = 52f,
                headingAngle = 270f,
                isBot = true,
                avatar = AvatarConfig(
                    nickname = "Valentina",
                    shirtColor = Color(0xFF8B5CF6),
                    hairColor = Color(0xFF4C1D95),
                    headsetColor = Color(0xFFA78BFA),
                    hairStyleIndex = 1,
                    accessoryIndex = 2
                ),
                lastSpokenText = "¡Tengo la guitarra lista en la fogata! 🔥🎶",
                speechBubbleTimer = 5f
            )
        )

        _uiState.update {
            it.copy(
                companionPlayers = companions,
                chatMessages = listOf(
                    ChatMessage(
                        senderName = "Mundo Amigos",
                        text = "¡Bienvenidos a la isla! Usa el joystick para explorar y el micrófono para hablar por proximidad.",
                        isNearby = true,
                        timeFormatted = getCurrentTime()
                    ),
                    ChatMessage(
                        senderName = "Sofía",
                        text = "¡Hola! Puedes oírme más fuerte si te acercas a mí.",
                        isNearby = true,
                        distanceMeters = 3.2f,
                        timeFormatted = getCurrentTime()
                    )
                )
            )
        }

        startGameLoop()
        startCompanionSocialLoop()
        startVoiceStateSync()
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    /**
     * Bucle principal de físicas, movimiento del jugador y el balón a 50 FPS
     */
    private fun startGameLoop() {
        viewModelScope.launch {
            while (isActive) {
                updatePhysics(0.02f)
                delay(20)
            }
        }
    }

    private fun updatePhysics(dt: Float) {
        val state = _uiState.value
        val me = state.localPlayer

        // Movimiento con Joystick
        if (joystickDeltaX != 0f || joystickDeltaY != 0f) {
            val speed = 9.0f * dt
            // Convertir input del joystick relativo a la rotación de cámara
            val camRad = Math.toRadians(state.cameraAngle.toDouble())
            val worldMoveX = (joystickDeltaX * cos(camRad) - joystickDeltaY * sin(camRad)).toFloat()
            val worldMoveY = (joystickDeltaX * sin(camRad) + joystickDeltaY * cos(camRad)).toFloat()

            var newX = me.x + worldMoveX * speed
            var newY = me.y + worldMoveY * speed

            // Colisión con límites del mapa de la isla
            newX = newX.coerceIn(6f, 74f)
            newY = newY.coerceIn(6f, 74f)

            // Colisión con el océano al sur (salvo en el muelle [39..46])
            if (newY > 60f && (newX < 39f || newX > 46f)) {
                newY = 60f
            }

            // Calcular ángulo de orientación del personaje
            val targetAngle = Math.toDegrees(atan2(worldMoveX.toDouble(), -worldMoveY.toDouble())).toFloat()
            me.headingAngle = targetAngle
            me.x = newX
            me.y = newY
            me.isMoving = true
            me.isSitting = false
        } else {
            me.isMoving = false
        }

        // Salto
        if (me.isJumping) {
            me.z += me.jumpVelocity * dt
            me.jumpVelocity -= 22f * dt // Gravedad
            if (me.z <= 0f) {
                me.z = 0f
                me.isJumping = false
                me.jumpVelocity = 0f
            }
        }

        // Temporizador de emociones
        if (me.currentEmote != null) {
            me.emoteTimer -= dt
            if (me.emoteTimer <= 0f) {
                me.currentEmote = null
            }
        }

        // Temporizador de bocadillos de diálogo de compañeros
        for (bot in state.companionPlayers) {
            if (bot.speechBubbleTimer > 0f) {
                bot.speechBubbleTimer -= dt
            }
        }

        // Actualizar física del balón de fútbol
        val goalEvent = world.updateSoccerPhysics()
        var newGoalBanner = state.goalBannerText
        if (goalEvent != null) {
            newGoalBanner = if (goalEvent == "RED_GOAL") "⚽ ¡GOOOL DEL EQUIPO ROJO! ⚽" else "⚽ ¡GOOOL DEL EQUIPO AZUL! ⚽"
            viewModelScope.launch {
                delay(3000)
                _uiState.update { it.copy(goalBannerText = null) }
            }
        }

        // Verificar estrellas coleccionables cercanas
        var starCollectedCount = 0
        for (star in world.stars) {
            if (!star.isCollected) {
                val d = hypot(me.x - star.x, me.y - star.y)
                if (d < 2.5f) {
                    star.isCollected = true
                    // Reproducir tono estelar
                    voiceEngine.playSpatialTone(listOf(523.25f, 659.25f, 783.99f), 400, 0.8f, 0.8f)
                    addChatMessage("Mundo Amigos", "⭐ ¡Encontraste la ${star.name}!")
                }
            }
            if (star.isCollected) starCollectedCount++
        }

        // Determinar zona actual y botón contextual de acción
        val currentZone = world.getZoneForPosition(me.x, me.y)
        val activeTarget = state.activeDelivery?.recipientName
        val nearbyRecipient = if (activeTarget != null) {
            state.companionPlayers.firstOrNull { it.name == activeTarget && hypot(me.x - it.x, me.y - it.y) < 3.8f }
        } else null

        val actionLabel = when {
            nearbyRecipient != null -> "💌 Entregar encomienda a ${nearbyRecipient.name}"
            currentZone == ActivityZone.POST_OFFICE -> {
                if (state.activeDelivery == null) "📮 Recoger encomienda" else "📦 Llevas paquete para ${state.activeDelivery.recipientName}"
            }
            currentZone == ActivityZone.SOCCER -> {
                val distBall = hypot(me.x - world.soccerBall.x, me.y - world.soccerBall.y)
                if (distBall < 3.5f) "⚽ Patear balón" else "🏃 Ir al balón"
            }
            currentZone == ActivityZone.FISHING_PIER -> if (me.y > 64f) "🎣 Pescar" else "🚶 Acercarse al muelle"
            currentZone == ActivityZone.CAMPFIRE -> "🔥 Tocar Guitarra"
            currentZone == ActivityZone.DANCE_STAGE -> "💃 Bailar"
            currentZone == ActivityZone.FOUNTAIN -> if (!me.isSitting) "🧘 Sentarse en banca" else "Levantarse"
            currentZone == ActivityZone.HILLS -> "✨ Mirar horizonte"
            else -> "Explorar isla"
        }

        // Calcular distancias de proximidad para chat de voz
        val nearbyList = mutableListOf<NearbyFriendInfo>()
        for (bot in state.companionPlayers) {
            val dist = hypot(me.x - bot.x, me.y - bot.y)
            val isWithinVoice = dist <= voiceEngine.maxHearingDistance
            val (leftGain, rightGain) = voiceEngine.calculateSpatialGain(
                listenerX = me.x,
                listenerY = me.y,
                listenerHeading = Math.toRadians(state.cameraAngle.toDouble()).toFloat(),
                sourceX = bot.x,
                sourceY = bot.y
            )
            nearbyList.add(NearbyFriendInfo(bot, dist, isWithinVoice, leftGain, rightGain))
        }
        nearbyList.sortBy { it.distanceMeters }

        _uiState.update {
            it.copy(
                localPlayer = me.copy(),
                currentZone = currentZone,
                contextActionLabel = actionLabel,
                nearbyPlayers = nearbyList,
                collectedStarCount = starCollectedCount,
                redScore = world.redTeamScore,
                blueScore = world.blueTeamScore,
                goalBannerText = newGoalBanner ?: it.goalBannerText
            )
        }
    }

    /**
     * Sincroniza el estado del micrófono real con el avatar local
     */
    private fun startVoiceStateSync() {
        viewModelScope.launch {
            voiceEngine.micAmplitude.collect { amp ->
                val isSpeaking = voiceEngine.isUserSpeaking.value
                _uiState.update {
                    val p = it.localPlayer.copy(
                        voiceLevel = amp,
                        isSpeaking = isSpeaking,
                        isMuted = it.isMicMuted
                    )
                    it.copy(localPlayer = p)
                }
            }
        }
    }

    /**
     * Ciclo de vida social de los compañeros virtuales para simular un mundo vivo:
     * caminan hacia actividades, patean el balón, reaccionan con emotes y hablan por proximidad.
     */
    private fun startCompanionSocialLoop() {
        viewModelScope.launch {
            val phrases = listOf(
                Pair("Sofía", "¡Qué hermoso día en la plaza! ¿Alguien quiere dar una vuelta?"),
                Pair("Lucas", "¡Pásame el balón, tiro colocado al ángulo! ⚽"),
                Pair("Mateo", "¡Acabo de picar un pez payaso gigante en el muelle!"),
                Pair("Valentina", "¡Reúnanse junto a la fogata, está sonando una balada acústica! 🎶"),
                Pair("Sofía", "¡Miren allá arriba, en la colina brilla una estrella! ✨"),
                Pair("Lucas", "¡Golazo! ¡Vamos equipo rojo!")
            )

            while (isActive) {
                delay(7000 + Random.nextLong(6000))
                val randomPhrase = phrases.random()
                val companions = _uiState.value.companionPlayers.toMutableList()
                val botIndex = companions.indexOfFirst { it.name == randomPhrase.first }

                if (botIndex >= 0) {
                    val bot = companions[botIndex]
                    bot.lastSpokenText = randomPhrase.second
                    bot.speechBubbleTimer = 6f
                    bot.isSpeaking = true

                    // Calcular ganancia espacial hacia el jugador para reproducir murmullo sonoro
                    val me = _uiState.value.localPlayer
                    val (l, r) = voiceEngine.calculateSpatialGain(
                        me.x, me.y,
                        Math.toRadians(_uiState.value.cameraAngle.toDouble()).toFloat(),
                        bot.x, bot.y
                    )

                    // Reproducir tono de voz espacial (frecuencias vocales lúdicas)
                    val baseFreq = when (bot.name) {
                        "Sofía" -> listOf(440f, 660f)
                        "Valentina" -> listOf(520f, 780f)
                        "Lucas" -> listOf(260f, 390f)
                        else -> listOf(220f, 330f)
                    }
                    voiceEngine.playSpatialTone(baseFreq, 320, l, r)

                    // Emote aleatorio
                    bot.currentEmote = EmoteType.values().random()
                    bot.emoteTimer = 4f

                    // Si Lucas está cerca del balón, lo patea
                    if (bot.name == "Lucas") {
                        val dBall = hypot(bot.x - world.soccerBall.x, bot.y - world.soccerBall.y)
                        if (dBall < 4.0f) {
                            world.kickBall(bot.x, bot.y, bot.headingAngle, 1.1f)
                        }
                    }

                    _uiState.update { it.copy(companionPlayers = companions) }

                    // Registrar mensaje en el historial del chat
                    val dist = hypot(me.x - bot.x, me.y - bot.y)
                    addChatMessage(bot.name, randomPhrase.second, isNearby = dist <= voiceEngine.maxHearingDistance, distanceMeters = dist)

                    delay(2000)
                    bot.isSpeaking = false
                    _uiState.update { it.copy(companionPlayers = companions) }
                }
            }
        }
    }

    // --- ACCIONES DE CONTROL ---

    fun onJoystickMove(dx: Float, dy: Float) {
        joystickDeltaX = dx.coerceIn(-1f, 1f)
        joystickDeltaY = dy.coerceIn(-1f, 1f)
    }

    fun onRotateCamera(dragDeltaX: Float) {
        _uiState.update {
            val newAngle = (it.cameraAngle + dragDeltaX * 0.35f) % 360f
            it.copy(cameraAngle = if (newAngle < 0) newAngle + 360f else newAngle)
        }
    }

    fun onPitchCamera(dragDeltaY: Float) {
        _uiState.update {
            val newPitch = (it.cameraPitch - dragDeltaY * 0.22f).coerceIn(12f, 72f)
            it.copy(cameraPitch = newPitch)
        }
    }

    fun onZoomCamera(zoomDelta: Float) {
        _uiState.update {
            val newDist = (it.cameraDistance + zoomDelta).coerceIn(6f, 22f)
            it.copy(cameraDistance = newDist)
        }
    }

    fun jump() {
        val me = _uiState.value.localPlayer
        if (!me.isJumping) {
            me.isJumping = true
            me.jumpVelocity = 8.5f
            _uiState.update { it.copy(localPlayer = me.copy()) }
        }
    }

    fun triggerContextAction() {
        val state = _uiState.value
        val me = state.localPlayer

        // Prioridad 1: Entregar encomienda si está cerca del destinatario
        val activeTarget = state.activeDelivery?.recipientName
        val nearbyRecipient = if (activeTarget != null) {
            state.companionPlayers.firstOrNull { it.name == activeTarget && hypot(me.x - it.x, me.y - it.y) < 3.8f }
        } else null

        if (nearbyRecipient != null) {
            deliverActiveParcel(nearbyRecipient)
            return
        }

        // Prioridad 2: Recoger paquete en la Oficina Postal
        if (state.currentZone == ActivityZone.POST_OFFICE) {
            if (state.activeDelivery == null) {
                pickupNewParcel()
            } else {
                addChatMessage("Oficina Postal", "📮 Ya llevas una encomienda para ${state.activeDelivery.recipientName}. ¡Búscale en el planeta!")
            }
            return
        }

        when (state.currentZone) {
            ActivityZone.SOCCER -> {
                val kicked = world.kickBall(me.x, me.y, me.headingAngle, power = 1.3f)
                if (kicked) {
                    voiceEngine.playSpatialTone(listOf(180f, 240f), 150, 0.9f, 0.9f)
                    triggerEmote(EmoteType.CHEER)
                }
            }
            ActivityZone.FISHING_PIER -> {
                startFishing()
            }
            ActivityZone.CAMPFIRE -> {
                _uiState.update { it.copy(showGuitarDialog = true) }
            }
            ActivityZone.DANCE_STAGE -> {
                triggerEmote(EmoteType.DANCE)
                voiceEngine.playSpatialTone(listOf(440f, 554.37f, 659.25f), 500, 0.8f, 0.8f)
            }
            ActivityZone.FOUNTAIN -> {
                me.isSitting = !me.isSitting
                _uiState.update { it.copy(localPlayer = me.copy()) }
            }
            ActivityZone.HILLS -> {
                triggerEmote(EmoteType.WAVE)
            }
            else -> {
                triggerEmote(EmoteType.WAVE)
            }
        }
    }

    fun pickupNewParcel() {
        val undelivered = world.availableParcels.filter { !it.isDelivered }
        val parcel = undelivered.randomOrNull() ?: world.availableParcels.random()
        val me = _uiState.value.localPlayer
        me.isCarryingParcel = true
        me.activeDelivery = parcel

        voiceEngine.playSpatialTone(listOf(523.25f, 659.25f, 783.99f), 300, 0.9f, 0.9f)
        addChatMessage("Oficina Postal", "📮 Recogiste un paquete para ${parcel.recipientName}: \"${parcel.note}\"")

        _uiState.update {
            it.copy(
                activeDelivery = parcel,
                localPlayer = me.copy()
            )
        }
    }

    fun deliverActiveParcel(recipient: Player) {
        val parcel = _uiState.value.activeDelivery ?: return
        if (parcel.recipientName != recipient.name) return

        parcel.isDelivered = true
        val me = _uiState.value.localPlayer
        me.isCarryingParcel = false
        me.activeDelivery = null
        me.currentEmote = EmoteType.MAIL
        me.emoteTimer = 4f

        val newDeliveredCount = _uiState.value.deliveredCount + 1
        voiceEngine.playSpatialTone(listOf(523.25f, 659.25f, 783.99f, 1046.50f), 600, 1.0f, 1.0f)

        // Respuesta cariñosa del amigo al recibir su paquete
        val thanksMessages = mapOf(
            "Sofía" to "¡Muchísimas gracias por las flores de manzanilla! 🌸☕",
            "Lucas" to "¡Uff, justo a tiempo para el partido de fútbol! ¡Gracias amigo! ⚽",
            "Mateo" to "¡Qué anzuelos tan brillantes! ¡A pescar se ha dicho! 🎣",
            "Valentina" to "¡Genial, ahora la guitarra sonará increíble en la fogata! 🎶🔥"
        )
        val reply = thanksMessages[recipient.name] ?: "¡Muchas gracias por la entrega! ✨"

        recipient.lastSpokenText = reply
        recipient.speechBubbleTimer = 6f
        recipient.currentEmote = EmoteType.CHEER
        recipient.emoteTimer = 4f

        addChatMessage(recipient.name, reply, isNearby = true)
        addChatMessage("Mundo Amigos", "⭐ ¡Entrega completada! Has entregado $newDeliveredCount encomiendas en el planeta.")

        _uiState.update {
            it.copy(
                activeDelivery = null,
                deliveredCount = newDeliveredCount,
                localPlayer = me.copy()
            )
        }
    }

    fun triggerEmote(emote: EmoteType) {
        val me = _uiState.value.localPlayer
        me.currentEmote = emote
        me.emoteTimer = 4f
        _uiState.update { it.copy(localPlayer = me.copy(), showEmoteWheel = false) }

        // Si es baile o risa, emitir sonido lúdico
        if (emote == EmoteType.DANCE) {
            voiceEngine.playSpatialTone(listOf(523.25f, 659.25f, 783.99f, 1046.50f), 450, 0.9f, 0.9f)
        }
    }

    // --- MINIJUEGO DE PESCA ---

    private fun startFishing() {
        _uiState.update {
            it.copy(
                fishingState = FishingState(isFishing = true, hasBite = false, reelProgress = 0f)
            )
        }
        viewModelScope.launch {
            // Esperar que muerda el anzuelo
            delay(2500 + Random.nextLong(2000))
            if (_uiState.value.fishingState.isFishing) {
                // ¡Picó!
                voiceEngine.playSpatialTone(listOf(880f, 1100f), 200, 0.9f, 0.9f)
                _uiState.update {
                    it.copy(
                        fishingState = it.fishingState.copy(hasBite = true)
                    )
                }
            }
        }
    }

    fun reelFishingRod() {
        val fs = _uiState.value.fishingState
        if (!fs.isFishing || !fs.hasBite) return

        val newProgress = fs.reelProgress + 0.35f
        if (newProgress >= 1.0f) {
            // ¡Captura exitosa!
            val fishCatalog = listOf(
                FishCatch("Trucha Dorada", "Rara", 3.4f, "🐟"),
                FishCatch("Pez Globo Espinoso", "Épica", 2.1f, "🐡"),
                FishCatch("Pez Payaso Rayado", "Común", 0.6f, "🐠"),
                FishCatch("Lubina Plateada", "Común", 1.8f, "🐟"),
                FishCatch("Calamar Luminoso", "Legendaria", 5.2f, "🦑")
            )
            val caught = fishCatalog.random()
            val updatedList = _uiState.value.caughtFishList + caught

            voiceEngine.playSpatialTone(listOf(523.25f, 659.25f, 783.99f, 1046.5f), 500, 1.0f, 1.0f)
            addChatMessage("Tú", "🎣 ¡Acabo de pescar un ${caught.name} (${caught.weightKg} kg)!")

            _uiState.update {
                it.copy(
                    caughtFishList = updatedList,
                    fishingState = FishingState(
                        isFishing = false,
                        lastCatch = caught,
                        showResultDialog = true
                    )
                )
            }
        } else {
            voiceEngine.playSpatialTone(listOf(400f), 100, 0.6f, 0.6f)
            _uiState.update {
                it.copy(fishingState = fs.copy(reelProgress = newProgress))
            }
        }
    }

    fun cancelFishing() {
        _uiState.update { it.copy(fishingState = FishingState()) }
    }

    fun closeFishResultDialog() {
        _uiState.update {
            it.copy(fishingState = it.fishingState.copy(showResultDialog = false))
        }
    }

    // --- GUITARRA ACÚSTICA EN LA FOGATA ---

    fun playGuitarChord(chordName: String) {
        val frequencies = when (chordName) {
            "Do (C)" -> listOf(261.63f, 329.63f, 392.00f, 523.25f)
            "Sol (G)" -> listOf(196.00f, 246.94f, 293.66f, 392.00f)
            "La menor (Am)" -> listOf(220.00f, 261.63f, 329.63f, 440.00f)
            "Fa (F)" -> listOf(174.61f, 220.00f, 261.63f, 349.23f)
            "Mi menor (Em)" -> listOf(164.81f, 246.94f, 329.63f, 493.88f)
            "Re (D)" -> listOf(293.66f, 369.99f, 440.00f, 587.33f)
            else -> listOf(261.63f, 329.63f, 392.00f)
        }
        val me = _uiState.value.localPlayer
        // Reproducir arpegio cálido de guitarra
        voiceEngine.playSpatialTone(frequencies, 600, 0.9f, 0.9f)
        addChatMessage("Fogata", "🎸 Tocando acorde de $chordName en la fogata...")
    }

    // --- CHAT Y MENSAJES ---

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val me = _uiState.value.localPlayer
        me.lastSpokenText = text
        me.speechBubbleTimer = 5f

        addChatMessage(me.name, text, isNearby = true, distanceMeters = 0f)
        _uiState.update { it.copy(localPlayer = me.copy()) }
    }

    private fun addChatMessage(sender: String, text: String, isNearby: Boolean = true, distanceMeters: Float = 0f) {
        val newMsg = ChatMessage(
            senderName = sender,
            text = text,
            isNearby = isNearby,
            distanceMeters = distanceMeters,
            timeFormatted = getCurrentTime()
        )
        _uiState.update {
            it.copy(chatMessages = (it.chatMessages + newMsg).takeLast(40))
        }
    }

    // --- CONTROLES DE VOZ ---

    fun toggleMute() {
        val newMuted = !_uiState.value.isMicMuted
        voiceEngine.isMuted = newMuted
        _uiState.update { it.copy(isMicMuted = newMuted) }
    }

    fun toggleDeafen() {
        val newDeafen = !_uiState.value.isDeafened
        voiceEngine.isDeafened = newDeafen
        _uiState.update { it.copy(isDeafened = newDeafen) }
    }

    fun togglePushToTalk(enabled: Boolean) {
        voiceEngine.isPushToTalk = enabled
        _uiState.update { it.copy(isPushToTalk = enabled) }
    }

    fun setPttPressed(pressed: Boolean) {
        voiceEngine.isPttPressed = pressed
        _uiState.update { it.copy(isPttActive = pressed) }
    }

    fun setVoiceThreshold(threshold: Float) {
        voiceEngine.voiceThreshold = threshold
        _uiState.update { it.copy(voiceThreshold = threshold) }
    }

    // --- PERSONALIZACIÓN DE AVATAR ---

    fun updateAvatar(newConfig: AvatarConfig) {
        val me = _uiState.value.localPlayer.copy(
            name = newConfig.nickname,
            avatar = newConfig
        )
        _uiState.update {
            it.copy(
                localPlayer = me,
                showAvatarStudio = false
            )
        }
    }

    // --- DIÁLOGOS Y MODALES ---

    fun toggleEmoteWheel(show: Boolean) {
        _uiState.update { it.copy(showEmoteWheel = show) }
    }

    fun toggleAvatarStudio(show: Boolean) {
        _uiState.update { it.copy(showAvatarStudio = show) }
    }

    fun toggleRoomDialog(show: Boolean) {
        _uiState.update { it.copy(showRoomDialog = show) }
    }

    fun toggleGuitarDialog(show: Boolean) {
        _uiState.update { it.copy(showGuitarDialog = show) }
    }

    fun toggleFishLogDialog(show: Boolean) {
        _uiState.update { it.copy(showFishLogDialog = show) }
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.release()
    }
}
