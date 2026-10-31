package com.example.engine3d

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Vector tridimensional con operaciones vectoriales completas para el motor 3D.
 */
data class Vec3(val x: Float = 0f, val y: Float = 0f, val z: Float = 0f) {
    operator fun plus(o: Vec3) = Vec3(x + o.x, y + o.y, z + o.z)
    operator fun minus(o: Vec3) = Vec3(x - o.x, y - o.y, z - o.z)
    operator fun times(s: Float) = Vec3(x * s, y * s, z * s)
    operator fun div(s: Float) = if (s != 0f) Vec3(x / s, y / s, z / s) else Vec3()

    operator fun unaryMinus() = Vec3(-x, -y, -z)

    fun dot(o: Vec3): Float = x * o.x + y * o.y + z * o.z

    fun cross(o: Vec3) = Vec3(
        y * o.z - z * o.y,
        z * o.x - x * o.z,
        x * o.y - y * o.x
    )

    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun lengthSq(): Float = x * x + y * y + z * z

    fun normalized(): Vec3 {
        val l = length()
        return if (l > 0.00001f) Vec3(x / l, y / l, z / l) else Vec3(0f, 0f, 1f)
    }

    fun distanceTo(o: Vec3): Float = (this - o).length()

    fun distanceSqTo(o: Vec3): Float = (this - o).lengthSq()

    fun lerp(target: Vec3, factor: Float): Vec3 {
        val t = factor.coerceIn(0f, 1f)
        return Vec3(
            x + (target.x - x) * t,
            y + (target.y - y) * t,
            z + (target.z - z) * t
        )
    }

    companion object {
        val ZERO = Vec3(0f, 0f, 0f)
        val ONE = Vec3(1f, 1f, 1f)
        val UP = Vec3(0f, 0f, 1f)
        val FORWARD = Vec3(0f, 1f, 0f)
        val RIGHT = Vec3(1f, 0f, 0f)
    }
}

/**
 * Matriz 4x4 para transformaciones lineales, proyecciones y vistas en 3D.
 */
class Mat4(val m: FloatArray = FloatArray(16)) {

    init {
        if (m.size != 16) throw IllegalArgumentException("Mat4 requiere un array de 16 elementos")
    }

    operator fun get(row: Int, col: Int): Float = m[row * 4 + col]
    operator fun set(row: Int, col: Int, value: Float) {
        m[row * 4 + col] = value
    }

    operator fun times(other: Mat4): Mat4 {
        val result = FloatArray(16)
        for (i in 0 until 4) {
            for (j in 0 until 4) {
                var sum = 0f
                for (k in 0 until 4) {
                    sum += this[i, k] * other[k, j]
                }
                result[i * 4 + j] = sum
            }
        }
        return Mat4(result)
    }

    fun transform(v: Vec3, w: Float = 1f): Vec3 {
        val rx = m[0] * v.x + m[1] * v.y + m[2] * v.z + m[3] * w
        val ry = m[4] * v.x + m[5] * v.y + m[6] * v.z + m[7] * w
        val rz = m[8] * v.x + m[9] * v.y + m[10] * v.z + m[11] * w
        val rw = m[12] * v.x + m[13] * v.y + m[14] * v.z + m[15] * w

        return if (rw != 0f && rw != 1f) {
            Vec3(rx / rw, ry / rw, rz / rw)
        } else {
            Vec3(rx, ry, rz)
        }
    }

    companion object {
        fun identity(): Mat4 {
            val arr = FloatArray(16)
            arr[0] = 1f; arr[5] = 1f; arr[10] = 1f; arr[15] = 1f
            return Mat4(arr)
        }

        fun translation(x: Float, y: Float, z: Float): Mat4 {
            val r = identity()
            r[0, 3] = x
            r[1, 3] = y
            r[2, 3] = z
            return r
        }

        fun translation(v: Vec3) = translation(v.x, v.y, v.z)

        fun scale(sx: Float, sy: Float, sz: Float): Mat4 {
            val r = identity()
            r[0, 0] = sx
            r[1, 1] = sy
            r[2, 2] = sz
            return r
        }

        fun scale(s: Float) = scale(s, s, s)

        fun rotationZ(radians: Float): Mat4 {
            val r = identity()
            val c = cos(radians)
            val s = sin(radians)
            r[0, 0] = c;  r[0, 1] = -s
            r[1, 0] = s;  r[1, 1] = c
            return r
        }

        fun rotationX(radians: Float): Mat4 {
            val r = identity()
            val c = cos(radians)
            val s = sin(radians)
            r[1, 1] = c;  r[1, 2] = -s
            r[2, 1] = s;  r[2, 2] = c
            return r
        }

        fun rotationY(radians: Float): Mat4 {
            val r = identity()
            val c = cos(radians)
            val s = sin(radians)
            r[0, 0] = c;  r[0, 2] = s
            r[2, 0] = -s; r[2, 2] = c
            return r
        }
    }
}

/**
 * Calculador de curvatura esférica de mundo abierto estilo "Tiny Planet".
 * Permite que los bordes del mapa desciendan hacia el horizonte emulando un planeta redondo.
 */
object WorldCurvature {
    fun applyPlanetCurvature(
        pos: Vec3,
        cameraCenter: Vec3,
        planetRadius: Float = 55.0f
    ): Vec3 {
        val dx = pos.x - cameraCenter.x
        val dy = pos.y - cameraCenter.y
        val distSq = dx * dx + dy * dy
        val curvatureOffset = distSq / (2f * planetRadius)
        return Vec3(pos.x, pos.y, pos.z - curvatureOffset)
    }
}
