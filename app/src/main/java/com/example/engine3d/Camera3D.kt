package com.example.engine3d

import kotlin.math.cos
import kotlin.math.sin

/**
 * Resultado de la proyección de un punto 3D en la pantalla 2D.
 */
data class ProjectedPoint(
    val screenX: Float,
    val screenY: Float,
    val depth: Float,
    val scale: Float,
    val isVisible: Boolean
)

/**
 * Cámara tridimensional orbital en tercera persona con soporte para deformación de mundo esférico.
 */
class EngineCamera3D(
    val eye: Vec3,
    val target: Vec3,
    val yawRad: Float,
    val pitchRad: Float,
    val focal: Float,
    val viewportWidth: Float,
    val viewportHeight: Float,
    val planetRadius: Float = 55.0f,
    val nearPlane: Float = 0.5f,
    val farPlane: Float = 250.0f
) {
    val centerX = viewportWidth / 2f
    val centerY = viewportHeight / 2f

    val cosYaw = cos(yawRad)
    val sinYaw = sin(yawRad)
    val cosPitch = cos(pitchRad)
    val sinPitch = sin(pitchRad)

    /**
     * Proyecta un punto en coordenadas del mundo 3D al espacio de pantalla de Compose (2D).
     */
    fun project(worldPoint: Vec3, applyCurvature: Boolean = true): ProjectedPoint {
        val curved = if (applyCurvature) {
            WorldCurvature.applyPlanetCurvature(worldPoint, target, planetRadius)
        } else {
            worldPoint
        }

        val dx = curved.x - eye.x
        val dy = curved.y - eye.y
        val dz = curved.z - eye.z

        // Rotación Yaw (alrededor del eje Z)
        val x1 = dx * cosYaw - dy * sinYaw
        val y1 = dx * sinYaw + dy * cosYaw
        val z1 = dz

        // Rotación Pitch (eje X de la cámara)
        val camRight = x1
        val camUp = z1 * cosPitch - y1 * sinPitch
        val camForward = y1 * cosPitch + z1 * sinPitch

        if (camForward < nearPlane || camForward > farPlane) {
            return ProjectedPoint(0f, 0f, camForward, 0f, false)
        }

        val scale = focal / camForward
        val sx = centerX + camRight * scale
        val sy = centerY - camUp * scale

        // Test de márgenes de pantalla generosos
        val isInside = sx >= -viewportWidth * 0.5f && sx <= viewportWidth * 1.5f &&
                sy >= -viewportHeight * 0.5f && sy <= viewportHeight * 1.5f

        return ProjectedPoint(sx, sy, camForward, scale, isInside)
    }

    fun project(x: Float, y: Float, z: Float, applyCurvature: Boolean = true) =
        project(Vec3(x, y, z), applyCurvature)
}
