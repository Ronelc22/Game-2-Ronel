package com.example.engine3d

import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Cara poligonal (triángulo o cuadrilátero) con normales y color base.
 */
data class Face3D(
    val vertexIndices: List<Int>,
    val normal: Vec3,
    val baseColor: Color,
    val outlineColor: Color = Color(0xFF292524),
    val hasOutline: Boolean = true,
    val hasShadowHatching: Boolean = false,
    val isDoubleSided: Boolean = false
)

/**
 * Malla geométrica 3D compuesta de vértices locales y caras poligonales.
 */
data class Mesh3D(
    val name: String,
    val vertices: List<Vec3>,
    val faces: List<Face3D>
)

/**
 * Transformación tridimensional (posición, rotación de Euler en radianes y escala).
 */
data class Transform3D(
    var position: Vec3 = Vec3.ZERO,
    var rotation: Vec3 = Vec3.ZERO, // pitch(X), roll(Y), yaw(Z)
    var scale: Vec3 = Vec3.ONE
) {
    fun toMatrix(): Mat4 {
        val t = Mat4.translation(position)
        val rz = Mat4.rotationZ(rotation.z)
        val rx = Mat4.rotationX(rotation.x)
        val ry = Mat4.rotationY(rotation.y)
        val s = Mat4.scale(scale.x, scale.y, scale.z)

        return t * rz * rx * ry * s
    }
}

/**
 * Generador de mallas primitivas para el motor 3D.
 */
object MeshPrimitives {

    /**
     * Crea una caja/cubo 3D centrado o alineado con la base.
     */
    fun createBox(
        width: Float,
        length: Float,
        height: Float,
        color: Color,
        centerOriginZ: Boolean = false
    ): Mesh3D {
        val hw = width / 2f
        val hl = length / 2f
        val zBottom = if (centerOriginZ) -height / 2f else 0f
        val zTop = if (centerOriginZ) height / 2f else height

        val vertices = listOf(
            Vec3(-hw, -hl, zBottom), // 0: BLB (Bottom-Left-Back)
            Vec3(hw, -hl, zBottom),  // 1: BRB
            Vec3(hw, hl, zBottom),   // 2: BRF
            Vec3(-hw, hl, zBottom),  // 3: BLF
            Vec3(-hw, -hl, zTop),    // 4: TLB (Top-Left-Back)
            Vec3(hw, -hl, zTop),     // 5: TRB
            Vec3(hw, hl, zTop),      // 6: TRF
            Vec3(-hw, hl, zTop)      // 7: TLF
        )

        val faces = listOf(
            // Arriba (+Z)
            Face3D(listOf(4, 5, 6, 7), Vec3(0f, 0f, 1f), color),
            // Abajo (-Z)
            Face3D(listOf(3, 2, 1, 0), Vec3(0f, 0f, -1f), color),
            // Frente (+Y)
            Face3D(listOf(7, 6, 2, 3), Vec3(0f, 1f, 0f), color, hasShadowHatching = true),
            // Espalda (-Y)
            Face3D(listOf(5, 4, 0, 1), Vec3(0f, -1f, 0f), color, hasShadowHatching = true),
            // Derecha (+X)
            Face3D(listOf(6, 5, 1, 2), Vec3(1f, 0f, 0f), color),
            // Izquierda (-X)
            Face3D(listOf(4, 7, 3, 0), Vec3(-1f, 0f, 0f), color, hasShadowHatching = true)
        )

        return Mesh3D("Box_${width}x${length}x${height}", vertices, faces)
    }

    /**
     * Crea un cilindro poligonal (útil para troncos de árboles, postes de luz, barriles).
     */
    fun createCylinder(
        radius: Float,
        height: Float,
        segments: Int = 8,
        color: Color
    ): Mesh3D {
        val vertices = mutableListOf<Vec3>()
        val faces = mutableListOf<Face3D>()

        // Vértices del círculo inferior y superior
        for (i in 0 until segments) {
            val angle = (i.toFloat() / segments) * 2f * PI.toFloat()
            val x = radius * cos(angle)
            val y = radius * sin(angle)
            vertices.add(Vec3(x, y, 0f))       // i: base
            vertices.add(Vec3(x, y, height))   // i + segments: tope
        }

        // Caras laterales
        for (i in 0 until segments) {
            val next = (i + 1) % segments
            val b0 = i * 2
            val t0 = i * 2 + 1
            val b1 = next * 2
            val t1 = next * 2 + 1

            val angle = ((i + 0.5f) / segments) * 2f * PI.toFloat()
            val normal = Vec3(cos(angle), sin(angle), 0f)

            faces.add(Face3D(listOf(b0, b1, t1, t0), normal, color))
        }

        // Tapa superior
        val topIndices = (0 until segments).map { it * 2 + 1 }
        faces.add(Face3D(topIndices, Vec3(0f, 0f, 1f), color))

        return Mesh3D("Cylinder_r${radius}_h$height", vertices, faces)
    }

    /**
     * Crea un tejado a dos aguas para casas estilo Abeto.
     */
    fun createGabledRoof(
        width: Float,
        length: Float,
        roofHeight: Float,
        baseZ: Float,
        roofColor: Color
    ): Mesh3D {
        val hw = width / 2f
        val hl = length / 2f
        val topZ = baseZ + roofHeight

        val vertices = listOf(
            Vec3(-hw, -hl, baseZ), // 0: alero frontal izq
            Vec3(hw, -hl, baseZ),  // 1: alero frontal der
            Vec3(hw, hl, baseZ),   // 2: alero posterior der
            Vec3(-hw, hl, baseZ),  // 3: alero posterior izq
            Vec3(0f, -hl, topZ),   // 4: cumbrera frontal
            Vec3(0f, hl, topZ)     // 5: cumbrera posterior
        )

        val faces = listOf(
            // Vertiente derecha (+X)
            Face3D(listOf(1, 2, 5, 4), Vec3(roofHeight, 0f, hw).normalized(), roofColor),
            // Vertiente izquierda (-X)
            Face3D(listOf(3, 0, 4, 5), Vec3(-roofHeight, 0f, hw).normalized(), roofColor, hasShadowHatching = true),
            // Frontón frontal (-Y)
            Face3D(listOf(0, 1, 4), Vec3(0f, -1f, 0f), roofColor),
            // Frontón posterior (+Y)
            Face3D(listOf(2, 3, 5), Vec3(0f, 1f, 0f), roofColor)
        )

        return Mesh3D("Roof", vertices, faces)
    }
}
