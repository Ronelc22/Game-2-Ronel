package com.example.engine3d

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Elemento de renderizado ordenable por profundidad para el algoritmo de pintor.
 */
sealed class RenderItem(val depth: Float) {
    class PolygonItem(
        depth: Float,
        val points: List<ProjectedPoint>,
        val fillColor: Color,
        val outlineColor: Color,
        val strokeWidth: Float,
        val hasOutline: Boolean,
        val hasHatching: Boolean
    ) : RenderItem(depth)

    class CustomItem(
        depth: Float,
        val drawBlock: (DrawScope) -> Unit
    ) : RenderItem(depth)
}

/**
 * Componente Jetpack Compose del motor de renderizado 3D.
 * Procesa la escena tridimensional, proyecta vértices con división en perspectiva,
 * realiza descarte de caras posteriores (backface culling), cel-shading y
 * rasterización por capas de profundidad con estética de contornos entintados.
 */
@Composable
fun Engine3DCanvas(
    scene: Scene3D,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val cam = scene.camera ?: return@Canvas
        val light = scene.light
        val renderQueue = mutableListOf<RenderItem>()

        val viewDir = (cam.target - cam.eye).normalized()

        for (entity in scene.getEntities()) {
            if (!entity.isVisible) continue

            // 1. Renderizado de malla poligonal si existe
            val mesh = entity.mesh
            if (mesh != null) {
                val modelMatrix = entity.transform.toMatrix()

                // Transformar vértices locales a coordenadas de mundo
                val worldVertices = mesh.vertices.map { v ->
                    modelMatrix.transform(v)
                }

                for (face in mesh.faces) {
                    val v0 = worldVertices[face.vertexIndices[0]]
                    val v1 = worldVertices[face.vertexIndices[1]]
                    val v2 = worldVertices[face.vertexIndices[2]]

                    // Calcular normal de la cara en espacio de mundo
                    val edge1 = v1 - v0
                    val edge2 = v2 - v0
                    val worldNormal = edge1.cross(edge2).normalized()

                    // Backface culling: omitir si la cara no mira hacia la cámara
                    if (!face.isDoubleSided && ShadingEngine.isBackface(worldNormal, viewDir)) {
                        continue
                    }

                    // Proyectar vértices a pantalla
                    val projectedPoints = face.vertexIndices.map { idx ->
                        cam.project(worldVertices[idx])
                    }

                    // Si ningún punto es visible o está detrás del plano cercano, omitir
                    if (projectedPoints.none { it.isVisible }) continue

                    // Calcular profundidad promedio para el algoritmo de pintor
                    val avgDepth = projectedPoints.map { it.depth }.average().toFloat()

                    // Sombreado plano estilizado (cel-shading)
                    val shadedColor = ShadingEngine.computeCelShading(worldNormal, light, face.baseColor)

                    val strokeWidth = (projectedPoints.first().scale * 0.045f).coerceIn(1.0f, 3.5f)

                    renderQueue.add(
                        RenderItem.PolygonItem(
                            depth = avgDepth,
                            points = projectedPoints,
                            fillColor = shadedColor,
                            outlineColor = face.outlineColor,
                            strokeWidth = strokeWidth,
                            hasOutline = face.hasOutline,
                            hasHatching = face.hasShadowHatching
                        )
                    )
                }
            }

            // 2. Renderizado personalizado procedural (avatares, cables, partículas)
            val custom = entity.customRenderer
            if (custom != null) {
                val projPos = cam.project(entity.transform.position)
                if (projPos.isVisible) {
                    renderQueue.add(
                        RenderItem.CustomItem(projPos.depth) { drawScope ->
                            custom(drawScope, cam, light)
                        }
                    )
                }
            }
        }

        // Ordenar elementos de mayor a menor profundidad (los más lejanos se pintan primero)
        renderQueue.sortByDescending { it.depth }

        // Rasterizar todos los polígonos y elementos en el Canvas de Compose
        for (item in renderQueue) {
            when (item) {
                is RenderItem.PolygonItem -> {
                    drawPolygonItem(item)
                }
                is RenderItem.CustomItem -> {
                    item.drawBlock(this)
                }
            }
        }
    }
}

/**
 * Dibuja un polígono 3D proyectado en pantalla con relleno cel-shaded, contorno de tinta y rayado.
 */
private fun DrawScope.drawPolygonItem(item: RenderItem.PolygonItem) {
    if (item.points.size < 3) return

    val path = Path().apply {
        moveTo(item.points[0].screenX, item.points[0].screenY)
        for (i in 1 until item.points.size) {
            lineTo(item.points[i].screenX, item.points[i].screenY)
        }
        close()
    }

    // Relleno de color sombreado
    drawPath(path = path, color = item.fillColor, style = Fill)

    // Trama de rayado Moebius para caras en sombra
    if (item.hasHatching && item.points.size >= 4) {
        val p0 = item.points[0]
        val p1 = item.points[1]
        val p2 = item.points[2]
        val p3 = item.points[3]
        val hatchColor = Color(0x33292524)
        for (f in 2..8 step 2) {
            val t = f / 10f
            val startX = p0.screenX + (p3.screenX - p0.screenX) * t
            val startY = p0.screenY + (p3.screenY - p0.screenY) * t
            val endX = p1.screenX + (p2.screenX - p1.screenX) * t
            val endY = p1.screenY + (p2.screenY - p1.screenY) * t
            drawLine(
                color = hatchColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = item.strokeWidth * 0.75f,
                cap = StrokeCap.Round
            )
        }
    }

    // Contorno entintado oscuro (Ligne Claire)
    if (item.hasOutline) {
        drawPath(
            path = path,
            color = item.outlineColor,
            style = Stroke(
                width = item.strokeWidth,
                cap = StrokeCap.Round
            )
        )
    }
}
