package com.example.engine3d

import androidx.compose.ui.graphics.Color
import kotlin.math.max

/**
 * Fuente de iluminación direccional y ambiental para el mundo 3D.
 */
data class Light3D(
    val direction: Vec3 = Vec3(0.55f, -0.55f, 0.75f).normalized(),
    val sunColor: Color = Color(0xFFFFF7ED),
    val ambientColor: Color = Color(0xFF94A3B8),
    val ambientIntensity: Float = 0.45f
)

/**
 * Motor de sombreado estilizado cel-shading / toon shading.
 */
object ShadingEngine {

    /**
     * Calcula el color resultante con sombreado plano en bandas (cel-shading).
     */
    fun computeCelShading(
        normal: Vec3,
        light: Light3D,
        baseColor: Color
    ): Color {
        val nDotL = max(0f, normal.dot(light.direction))

        return when {
            nDotL > 0.75f -> {
                // Iluminación directa brillante (Highlight)
                Color(
                    red = (baseColor.red * 1.15f).coerceAtMost(1f),
                    green = (baseColor.green * 1.15f).coerceAtMost(1f),
                    blue = (baseColor.blue * 1.15f).coerceAtMost(1f),
                    alpha = baseColor.alpha
                )
            }
            nDotL > 0.35f -> {
                // Tono base medio
                baseColor
            }
            else -> {
                // Tono en sombra con ligero tinte frío atmosférico
                Color(
                    red = (baseColor.red * 0.70f).coerceIn(0f, 1f),
                    green = (baseColor.green * 0.72f).coerceIn(0f, 1f),
                    blue = (baseColor.blue * 0.78f).coerceIn(0f, 1f),
                    alpha = baseColor.alpha
                )
            }
        }
    }

    /**
     * Verifica si una cara poligonal mira hacia la cámara o en dirección contraria (Backface Culling).
     */
    fun isBackface(normal: Vec3, viewDir: Vec3): Boolean {
        return normal.dot(viewDir) >= 0f
    }
}
