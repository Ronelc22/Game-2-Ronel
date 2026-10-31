package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * Representa la configuración cosmética del avatar de un jugador en estilo Messenger Abeto.
 */
data class AvatarConfig(
    val nickname: String = "Cartero",
    val skinTone: Color = Color(0xFFFFE0B2),
    val shirtColor: Color = Color(0xFF81BFBC), // Turquesa característico de Abeto
    val pantsColor: Color = Color(0xFF334155),
    val hairColor: Color = Color(0xFF5D4037),
    val bagColor: Color = Color(0xFFC2410C), // Cartera de mensajero de cuero
    val headsetColor: Color = Color(0xFF0D9488),
    val hairStyleIndex: Int = 1, // 0: Corto, 1: Con Copete, 2: Gorra de Cartero, 3: Orejitas
    val accessoryIndex: Int = 1  // 0: Ninguno, 1: Cartera de mensajero, 2: Lentes, 3: Corona
)

enum class EmoteType(val label: String, val icon: String, val particleEmoji: String) {
    WAVE("Saludar", "👋", "✨"),
    DANCE("Bailar", "💃", "🎵"),
    LAUGH("Reír", "😂", "🌸"),
    CHEER("Festejar", "🎉", "⭐"),
    SIT("Sentarse", "🧘", "💤"),
    HIGH_FIVE("Chocar", "✋", "💥"),
    MAIL("Entregar Carta", "💌", "🕊️")
}

data class DeliveryParcel(
    val id: String,
    val recipientName: String,
    val note: String,
    val rewardStars: Int = 1,
    var isDelivered: Boolean = false
)

data class Player(
    val id: String,
    val name: String,
    var x: Float,
    var y: Float,
    var z: Float = 0f,
    var headingAngle: Float = 0f, // Grados de rotación
    var isMoving: Boolean = false,
    var isJumping: Boolean = false,
    var jumpVelocity: Float = 0f,
    var currentEmote: EmoteType? = null,
    var emoteTimer: Float = 0f,
    var isSpeaking: Boolean = false,
    var voiceLevel: Float = 0f, // 0.0 a 1.0 (amplitud de micrófono)
    var isMuted: Boolean = false,
    var isBot: Boolean = false,
    var isSitting: Boolean = false,
    var avatar: AvatarConfig = AvatarConfig(nickname = name),
    var lastSpokenText: String = "",
    var speechBubbleTimer: Float = 0f,
    var isCarryingParcel: Boolean = false,
    var activeDelivery: DeliveryParcel? = null,
    var deliveredCount: Int = 0
)

data class SoccerBall(
    var x: Float = 42f,
    var y: Float = 40f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var vz: Float = 0f,
    var z: Float = 0f,
    val radius: Float = 1.2f
)

enum class ActivityZone(val title: String, val description: String, val icon: String) {
    POST_OFFICE("Oficina de Correo", "Recoge encomiendas y cartas para entregar a amigos", "📮"),
    FOUNTAIN("Plaza Central", "Punto de encuentro, fuentes y bancas para conversar", "⛲"),
    SOCCER("Cancha de Fútbol", "¡Patea el balón y anota goles con amigos!", "⚽"),
    FISHING_PIER("Muelle de Pesca", "Relájate pescando en el océano turquesa", "🎣"),
    CAMPFIRE("Fogata Acogedora", "Música de guitarra acústica y charlas entre amigos", "🔥"),
    DANCE_STAGE("Pista de Baile", "Luces de colores y música para bailar", "💃"),
    HILLS("Mirador de Colinas", "Vistas panorámicas del planeta y flores silvestres", "🌸")
}

data class CollectibleStar(
    val id: Int,
    val x: Float,
    val y: Float,
    val name: String,
    var isCollected: Boolean = false
)

data class FishCatch(
    val name: String,
    val rarity: String,
    val weightKg: Float,
    val emoji: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val senderName: String,
    val text: String,
    val isNearby: Boolean = true,
    val distanceMeters: Float = 0f,
    val timeFormatted: String = ""
)
