package com.example.world

import com.example.model.ActivityZone
import com.example.model.CollectibleStar
import com.example.model.DeliveryParcel
import com.example.model.SoccerBall
import kotlin.math.hypot

data class WorldTree(
    val x: Float,
    val y: Float,
    val type: Int, // 0: Pino cónico Abeto, 1: Árbol redondeado lollipop
    val scale: Float = 1.0f
)

data class WorldHouse(
    val x: Float,
    val y: Float,
    val width: Float,
    val depth: Float,
    val roofColorHex: Long,
    val name: String
)

data class WorldBench(
    val x: Float,
    val y: Float,
    val rotation: Float
)

data class WorldFlower(
    val x: Float,
    val y: Float,
    val colorHex: Long
)

data class WorldPole(
    val x: Float,
    val y: Float
)

class GameWorld {
    val mapWidth = 80f
    val mapHeight = 80f

    // Árboles decorativos estilo Abeto Messenger (tonos salvia y eucalipto)
    val trees = listOf(
        WorldTree(12f, 15f, 0, 1.2f),
        WorldTree(16f, 12f, 1, 1.1f),
        WorldTree(22f, 8f, 0, 1.3f),
        WorldTree(28f, 14f, 1, 1.2f),
        WorldTree(35f, 10f, 0, 1.4f),
        WorldTree(50f, 12f, 1, 1.2f),
        WorldTree(58f, 15f, 0, 1.1f),
        WorldTree(68f, 18f, 1, 1.3f),
        WorldTree(72f, 25f, 0, 1.2f),
        WorldTree(70f, 38f, 1, 1.1f),
        WorldTree(74f, 52f, 0, 1.4f),
        WorldTree(68f, 65f, 1, 1.1f),
        WorldTree(52f, 68f, 0, 1.0f),
        WorldTree(30f, 66f, 1, 1.3f),
        WorldTree(14f, 62f, 0, 1.2f),
        WorldTree(8f, 48f, 1, 1.1f),
        WorldTree(8f, 32f, 0, 1.3f),
        WorldTree(8f, 20f, 1, 1.1f)
    )

    // Casitas pintorescas con paleta suave terracota, turquesa y salvia de Abeto
    val houses = listOf(
        WorldHouse(40f, 23f, 6.5f, 5.5f, 0xFFE07A5F, "Oficina Postal"),
        WorldHouse(29f, 26f, 6.5f, 5.5f, 0xFF81BFBC, "Cabaña de Sofía"),
        WorldHouse(51f, 26f, 6.5f, 5.5f, 0xFFF28482, "Taller de Lucas"),
        WorldHouse(54f, 40f, 6f, 5.5f, 0xFFF4A261, "Tienda de Té"),
        WorldHouse(25f, 42f, 6f, 5.5f, 0xFF84A59D, "Casa de Huéspedes")
    )

    // Postes de tendido eléctrico estilo pueblo costero japonés de Messenger
    val poles = listOf(
        WorldPole(34f, 22f),
        WorldPole(46f, 22f),
        WorldPole(48f, 36f),
        WorldPole(32f, 36f)
    )

    // Flores silvestres en el césped estilo Abeto
    val flowers = listOf(
        WorldFlower(35f, 36f, 0xFFF472B6),
        WorldFlower(38f, 45f, 0xFFFBBF24),
        WorldFlower(44f, 44f, 0xFFF87171),
        WorldFlower(46f, 36f, 0xFF60A5FA),
        WorldFlower(20f, 24f, 0xFFF472B6),
        WorldFlower(60f, 48f, 0xFFFBBF24),
        WorldFlower(62f, 26f, 0xFF34D399),
        WorldFlower(26f, 12f, 0xFFF87171),
        WorldFlower(42f, 62f, 0xFFFBBF24),
        WorldFlower(33f, 48f, 0xFFF472B6),
        WorldFlower(48f, 52f, 0xFF60A5FA),
        WorldFlower(27f, 34f, 0xFFFBBF24)
    )

    // Bancas de madera en la plaza
    val benches = listOf(
        WorldBench(36f, 38f, 0f),
        WorldBench(44f, 38f, 0f),
        WorldBench(36f, 42f, 180f),
        WorldBench(44f, 42f, 180f),
        WorldBench(24f, 28f, 90f),
        WorldBench(65f, 53f, 45f)
    )

    // 8 Estrellas coleccionables escondidas
    val stars = listOf(
        CollectibleStar(1, 40f, 40f, "Estrella de la Fuente"),
        CollectibleStar(2, 22f, 29f, "Estrella del Travesaño"),
        CollectibleStar(3, 42.5f, 74f, "Estrella del Pescador"),
        CollectibleStar(4, 64f, 50f, "Estrella de la Fogata"),
        CollectibleStar(5, 61.5f, 30f, "Estrella de la Pista"),
        CollectibleStar(6, 26f, 14f, "Estrella del Mirador"),
        CollectibleStar(7, 72f, 22f, "Estrella del Bosque"),
        CollectibleStar(8, 40f, 21f, "Estrella de la Oficina Postal")
    )

    // Catálogo de paquetes/cartas de mensajero disponibles en la Oficina Postal
    val availableParcels = listOf(
        DeliveryParcel("p_sofia", "Sofía", "Flores aromáticas de manzanilla para el té", 1),
        DeliveryParcel("p_lucas", "Lucas", "Un nuevo silbato de árbitro para el fútbol", 1),
        DeliveryParcel("p_mateo", "Mateo", "Caja con anzuelos dorados para el muelle", 1),
        DeliveryParcel("p_valen", "Valentina", "Cuerdas nuevas de nylon para la guitarra", 1)
    )

    // Balón de fútbol en la cancha
    val soccerBall = SoccerBall(x = 22f, y = 40f)

    // Marcador de fútbol
    var redTeamScore = 0
    var blueTeamScore = 0

    fun getZoneForPosition(x: Float, y: Float): ActivityZone {
        return when {
            // Oficina Postal de Mensajería: X [36..44], Y [20..26]
            x in 36f..44f && y in 20f..26f -> ActivityZone.POST_OFFICE
            // Cancha de fútbol: X [14..30], Y [28..52]
            x in 14f..30f && y in 28f..52f -> ActivityZone.SOCCER
            // Muelle de pesca: X [38..48], Y [60..78]
            x in 38f..48f && y in 60f..78f -> ActivityZone.FISHING_PIER
            // Fogata: X [58..72], Y [44..58]
            x in 58f..72f && y in 44f..58f -> ActivityZone.CAMPFIRE
            // Pista de baile: X [55..68], Y [24..36]
            x in 55f..68f && y in 24f..36f -> ActivityZone.DANCE_STAGE
            // Mirador colinas: X [15..36], Y [8..22]
            x in 15f..36f && y in 8f..22f -> ActivityZone.HILLS
            // Plaza central
            else -> ActivityZone.FOUNTAIN
        }
    }

    /**
     * Actualiza la física del balón de fútbol cuando es pateado.
     * Retorna "RED_GOAL", "BLUE_GOAL", o null si no hubo gol.
     */
    fun updateSoccerPhysics(): String? {
        soccerBall.vx *= 0.94f
        soccerBall.vy *= 0.94f

        if (soccerBall.z > 0f || soccerBall.vz != 0f) {
            soccerBall.vz -= 0.08f // gravedad
            soccerBall.z += soccerBall.vz
            if (soccerBall.z <= 0f) {
                soccerBall.z = 0f
                soccerBall.vz = -soccerBall.vz * 0.6f
                if (kotlin.math.abs(soccerBall.vz) < 0.05f) soccerBall.vz = 0f
            }
        }

        soccerBall.x += soccerBall.vx
        soccerBall.y += soccerBall.vy

        val minX = 15.5f
        val maxX = 28.5f
        val minY = 30.5f
        val maxY = 49.5f

        // Rebotes en paredes laterales
        if (soccerBall.x < minX) {
            soccerBall.x = minX
            soccerBall.vx = -soccerBall.vx * 0.7f
        } else if (soccerBall.x > maxX) {
            soccerBall.x = maxX
            soccerBall.vx = -soccerBall.vx * 0.7f
        }

        // Revisar portería Norte
        if (soccerBall.y <= minY) {
            if (soccerBall.x in 19.5f..24.5f) {
                blueTeamScore++
                resetBall()
                return "BLUE_GOAL"
            } else {
                soccerBall.y = minY
                soccerBall.vy = -soccerBall.vy * 0.7f
            }
        }

        // Revisar portería Sur
        if (soccerBall.y >= maxY) {
            if (soccerBall.x in 19.5f..24.5f) {
                redTeamScore++
                resetBall()
                return "RED_GOAL"
            } else {
                soccerBall.y = maxY
                soccerBall.vy = -soccerBall.vy * 0.7f
            }
        }

        return null
    }

    fun resetBall() {
        soccerBall.x = 22f
        soccerBall.y = 40f
        soccerBall.vx = 0f
        soccerBall.vy = 0f
        soccerBall.z = 0f
        soccerBall.vz = 0f
    }

    fun kickBall(playerX: Float, playerY: Float, playerHeading: Float, power: Float = 1.2f): Boolean {
        val dx = soccerBall.x - playerX
        val dy = soccerBall.y - playerY
        val dist = hypot(dx, dy)

        if (dist < 3.2f) {
            val angleRad = Math.toRadians(playerHeading.toDouble())
            val forwardX = kotlin.math.sin(angleRad).toFloat()
            val forwardY = -kotlin.math.cos(angleRad).toFloat()

            soccerBall.vx = forwardX * (power * 1.5f) + (dx / dist) * 0.4f
            soccerBall.vy = forwardY * (power * 1.5f) + (dy / dist) * 0.4f
            soccerBall.vz = 0.45f
            soccerBall.z = 0.2f
            return true
        }
        return false
    }
}
