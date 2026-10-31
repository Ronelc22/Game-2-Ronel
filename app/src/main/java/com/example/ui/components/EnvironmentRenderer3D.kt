package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.engine3d.EngineCamera3D
import com.example.engine3d.Vec3
import com.example.world.GameWorld
import com.example.world.WorldHouse
import com.example.world.WorldPole
import com.example.world.WorldTree
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Renderizador de entorno de mundo abierto estilo Messenger (Abeto),
 * con iluminación cel-shading, tramas de lápiz (hatching) y contornos entintados.
 */
object EnvironmentRenderer3D {

    fun drawSky(drawScope: DrawScope, camera: EngineCamera3D, sunDir: Vec3, animTime: Float) {
        val horizonY = camera.centerY + (sin(camera.pitchRad) / cos(camera.pitchRad)) * camera.focal

        // Gradiente pastel luminoso
        drawScope.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFA5F3FC),
                    Color(0xFFBAE6FD),
                    Color(0xFFFFF1E6),
                    Color(0xFFFEF3C7)
                ),
                startY = 0f,
                endY = max(horizonY, 20f)
            )
        )

        // Sol estilizado
        val sunWorld = Vec3(
            camera.eye.x + sunDir.x * 60f,
            camera.eye.y + sunDir.y * 60f,
            camera.eye.z + sunDir.z * 60f
        )
        val sunP = camera.project(sunWorld)
        if (sunP.isVisible) {
            drawScope.drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFFAF0), Color(0xFFFDE68A), Color(0x00FDE68A)),
                    center = Offset(sunP.screenX, sunP.screenY),
                    radius = 68f
                ),
                radius = 68f,
                center = Offset(sunP.screenX, sunP.screenY)
            )
            drawScope.drawCircle(Color(0xFFFFFDF5), radius = 24f, center = Offset(sunP.screenX, sunP.screenY))
            drawScope.drawCircle(Color(0xFF292524), radius = 24f, center = Offset(sunP.screenX, sunP.screenY), style = Stroke(width = 2.5f))
        }

        // Nubes esponjosas con contornos
        for (i in 0 until 5) {
            val cloudX = ((i * 22f + animTime * 1.2f) % 86f)
            val cloudY = 12f + (i * 15f) % 65f
            val cloudZ = 22f + (i * 2.5f)
            val cp = camera.project(cloudX, cloudY, cloudZ)
            if (cp.isVisible) {
                val cw = cp.scale * 4.4f
                val ch = cp.scale * 1.9f
                drawScope.drawOval(
                    color = Color(0xFF292524),
                    topLeft = Offset(cp.screenX - cw / 2 - 2f, cp.screenY - ch / 2 - 2f),
                    size = Size(cw + 4f, ch + 4f)
                )
                drawScope.drawOval(
                    color = Color(0xFFFFFDF8),
                    topLeft = Offset(cp.screenX - cw / 2, cp.screenY - ch / 2),
                    size = Size(cw, ch)
                )
            }
        }
    }

    fun drawOceanAndShore(drawScope: DrawScope, camera: EngineCamera3D, animTime: Float) {
        val oceanQuad = listOf(
            camera.project(-40f, -40f, -0.4f),
            camera.project(120f, -40f, -0.4f),
            camera.project(120f, 120f, -0.4f),
            camera.project(-40f, 120f, -0.4f)
        )
        if (oceanQuad.all { it.isVisible }) {
            val path = Path().apply {
                moveTo(oceanQuad[0].screenX, oceanQuad[0].screenY)
                for (p in oceanQuad.drop(1)) lineTo(p.screenX, p.screenY)
                close()
            }
            drawScope.drawPath(path, Color(0xFF81BFBC))
        }

        // Ondas de espuma
        for (step in 0 until 4) {
            val waveY = 62f + step * 4f + sin(animTime * 2.2f + step).toFloat() * 0.7f
            val p1 = camera.project(4f, waveY, -0.3f)
            val p2 = camera.project(76f, waveY, -0.3f)
            if (p1.isVisible && p2.isVisible) {
                drawScope.drawLine(Color(0xFF292524), Offset(p1.screenX, p1.screenY), Offset(p2.screenX, p2.screenY), strokeWidth = 5.5f, cap = StrokeCap.Round)
                drawScope.drawLine(Color(0xFFFFFFFF), Offset(p1.screenX, p1.screenY), Offset(p2.screenX, p2.screenY), strokeWidth = 3.5f, cap = StrokeCap.Round)
            }
        }

        // Isla de césped celadón con borde de arena
        val islandPts = listOf(
            Vec3(8f, 8f, 0f), Vec3(40f, 6f, 0f), Vec3(72f, 8f, 0f),
            Vec3(74f, 38f, 0f), Vec3(72f, 60f, 0f), Vec3(48f, 60f, 0f),
            Vec3(38f, 60f, 0f), Vec3(8f, 60f, 0f), Vec3(6f, 35f, 0f)
        )
        val projIsland = islandPts.map { camera.project(it) }
        if (projIsland.any { it.isVisible }) {
            val islandPath = Path()
            var started = false
            for (p in projIsland) {
                if (p.isVisible) {
                    if (!started) { islandPath.moveTo(p.screenX, p.screenY); started = true }
                    else islandPath.lineTo(p.screenX, p.screenY)
                }
            }
            if (started) {
                islandPath.close()
                drawScope.drawPath(islandPath, Color(0xFFF4F1DE), style = Stroke(width = 28f))
                drawScope.drawPath(islandPath, Color(0xFF292524), style = Stroke(width = 3.5f))
                drawScope.drawPath(islandPath, Color(0xFFA7D7C5))
            }
        }
    }

    fun drawTerrain(drawScope: DrawScope, camera: EngineCamera3D, world: GameWorld, animTime: Float) {
        // Senderos de tierra crema
        drawPathLine(drawScope, camera, listOf(Vec3(40f, 40f, 0.03f), Vec3(22f, 40f, 0.03f)), 2.2f)
        drawPathLine(drawScope, camera, listOf(Vec3(40f, 40f, 0.03f), Vec3(40f, 24f, 0.03f)), 2.2f)
        drawPathLine(drawScope, camera, listOf(Vec3(40f, 40f, 0.03f), Vec3(42.5f, 64f, 0.03f)), 2.2f)
        drawPathLine(drawScope, camera, listOf(Vec3(40f, 40f, 0.03f), Vec3(64f, 50f, 0.03f)), 2.2f)
        drawPathLine(drawScope, camera, listOf(Vec3(40f, 40f, 0.03f), Vec3(62f, 30f, 0.03f)), 2.2f)

        // Briznas de hierba '//'
        val grassTufts = listOf(
            Pair(33f, 32f), Pair(37f, 48f), Pair(45f, 34f), Pair(48f, 46f),
            Pair(24f, 22f), Pair(58f, 38f), Pair(62f, 54f), Pair(28f, 54f),
            Pair(44f, 58f), Pair(18f, 36f), Pair(66f, 22f), Pair(38f, 16f)
        )
        for (t in grassTufts) {
            val tp = camera.project(t.first, t.second, 0.05f)
            if (tp.isVisible) {
                val len = tp.scale * 0.28f
                drawScope.drawLine(Color(0xFF292524), Offset(tp.screenX, tp.screenY), Offset(tp.screenX - len * 0.4f, tp.screenY - len), strokeWidth = 2f, cap = StrokeCap.Round)
                drawScope.drawLine(Color(0xFF292524), Offset(tp.screenX + len * 0.3f, tp.screenY), Offset(tp.screenX + len * 0.2f, tp.screenY - len * 1.1f), strokeWidth = 2f, cap = StrokeCap.Round)
            }
        }

        // Plaza Central
        val segs = 16
        val plazaR = 8.5f
        val plazaPts = (0 until segs).map { i ->
            val a = i * 2 * PI / segs
            camera.project(40f + cos(a).toFloat() * plazaR, 40f + sin(a).toFloat() * plazaR, 0.04f)
        }
        if (plazaPts.all { it.isVisible }) {
            val p = Path().apply {
                moveTo(plazaPts[0].screenX, plazaPts[0].screenY)
                for (pt in plazaPts.drop(1)) lineTo(pt.screenX, pt.screenY)
                close()
            }
            drawScope.drawPath(p, Color(0xFFF4F1DE))
            drawScope.drawPath(p, Color(0xFF292524), style = Stroke(width = 3.5f))
        }

        // Cancha de Fútbol
        val pTL = camera.project(15f, 29f, 0.04f)
        val pTR = camera.project(29f, 29f, 0.04f)
        val pBR = camera.project(29f, 51f, 0.04f)
        val pBL = camera.project(15f, 51f, 0.04f)
        if (pTL.isVisible && pTR.isVisible && pBR.isVisible && pBL.isVisible) {
            val pitchPath = Path().apply {
                moveTo(pTL.screenX, pTL.screenY); lineTo(pTR.screenX, pTR.screenY); lineTo(pBR.screenX, pBR.screenY); lineTo(pBL.screenX, pBL.screenY); close()
            }
            drawScope.drawPath(pitchPath, Color(0xFF68A691))
            drawScope.drawPath(pitchPath, Color(0xFF292524), style = Stroke(width = 3.5f))
            drawScope.drawPath(pitchPath, Color(0xEEFFFFFF), style = Stroke(width = 2.5f))

            val midL = camera.project(15f, 40f, 0.05f)
            val midR = camera.project(29f, 40f, 0.05f)
            if (midL.isVisible && midR.isVisible) {
                drawScope.drawLine(Color.White, Offset(midL.screenX, midL.screenY), Offset(midR.screenX, midR.screenY), strokeWidth = 2.5f)
            }
            val circlePts = (0 until 12).map { i ->
                val a = i * 2 * PI / 12
                camera.project(22f + cos(a).toFloat() * 3.2f, 40f + sin(a).toFloat() * 3.2f, 0.05f)
            }
            if (circlePts.all { it.isVisible }) {
                val cp = Path().apply {
                    moveTo(circlePts[0].screenX, circlePts[0].screenY)
                    for (pt in circlePts.drop(1)) lineTo(pt.screenX, pt.screenY)
                    close()
                }
                drawScope.drawPath(cp, Color.White, style = Stroke(width = 2.5f))
            }
        }
    }

    private fun drawPathLine(drawScope: DrawScope, camera: EngineCamera3D, points: List<Vec3>, widthMeters: Float) {
        for (i in 0 until points.size - 1) {
            val p1 = camera.project(points[i])
            val p2 = camera.project(points[i + 1])
            if (p1.isVisible && p2.isVisible) {
                val sw = ((p1.scale + p2.scale) * 0.5f * widthMeters).coerceIn(8f, 45f)
                drawScope.drawLine(Color(0xFF292524), Offset(p1.screenX, p1.screenY), Offset(p2.screenX, p2.screenY), strokeWidth = sw + 3.5f, cap = StrokeCap.Round)
                drawScope.drawLine(Color(0xFFF4F1DE), Offset(p1.screenX, p1.screenY), Offset(p2.screenX, p2.screenY), strokeWidth = sw, cap = StrokeCap.Round)
            }
        }
    }

    fun drawPole(drawScope: DrawScope, camera: EngineCamera3D, x: Float, y: Float) {
        val poleH = 4.2f
        val b = camera.project(x, y, 0f)
        val t = camera.project(x, y, poleH)
        if (!b.isVisible || !t.isVisible) return

        val pw = t.scale * 0.22f
        drawScope.drawLine(Color(0xFF292524), Offset(b.screenX, b.screenY), Offset(t.screenX, t.screenY), strokeWidth = pw + 3f, cap = StrokeCap.Round)
        drawScope.drawLine(Color(0xFF78716C), Offset(b.screenX, b.screenY), Offset(t.screenX, t.screenY), strokeWidth = pw, cap = StrokeCap.Round)

        val crossArmL = camera.project(x - 0.7f, y, poleH - 0.3f)
        val crossArmR = camera.project(x + 0.7f, y, poleH - 0.3f)
        if (crossArmL.isVisible && crossArmR.isVisible) {
            drawScope.drawLine(Color(0xFF292524), Offset(crossArmL.screenX, crossArmL.screenY), Offset(crossArmR.screenX, crossArmR.screenY), strokeWidth = pw * 0.8f + 2f)
            drawScope.drawLine(Color(0xFFD6D3D1), Offset(crossArmL.screenX, crossArmL.screenY), Offset(crossArmR.screenX, crossArmR.screenY), strokeWidth = pw * 0.8f)

            drawScope.drawCircle(Color(0xFF292524), radius = pw * 0.5f + 1.5f, center = Offset(crossArmL.screenX, crossArmL.screenY - pw * 0.4f))
            drawScope.drawCircle(Color.White, radius = pw * 0.5f, center = Offset(crossArmL.screenX, crossArmL.screenY - pw * 0.4f))
            drawScope.drawCircle(Color(0xFF292524), radius = pw * 0.5f + 1.5f, center = Offset(crossArmR.screenX, crossArmR.screenY - pw * 0.4f))
            drawScope.drawCircle(Color.White, radius = pw * 0.5f, center = Offset(crossArmR.screenX, crossArmR.screenY - pw * 0.4f))
        }
    }

    fun drawPowerCables(drawScope: DrawScope, camera: EngineCamera3D, poles: List<WorldPole>) {
        if (poles.size < 2) return
        val poleH = 4.2f - 0.3f

        for (i in 0 until poles.size) {
            val pA = poles[i]
            val pB = poles[(i + 1) % poles.size]

            val start = camera.project(pA.x, pA.y, poleH)
            val end = camera.project(pB.x, pB.y, poleH)

            val midX = (pA.x + pB.x) / 2f
            val midY = (pA.y + pB.y) / 2f
            val midZ = poleH - 0.45f
            val mid = camera.project(midX, midY, midZ)

            if (start.isVisible && end.isVisible && mid.isVisible) {
                val cablePath = Path().apply {
                    moveTo(start.screenX, start.screenY)
                    quadraticTo(mid.screenX, mid.screenY, end.screenX, end.screenY)
                }
                drawScope.drawPath(cablePath, Color(0xFF1C1917), style = Stroke(width = 2.2f, cap = StrokeCap.Round))
            }
        }
    }

    fun drawHouse(drawScope: DrawScope, camera: EngineCamera3D, house: WorldHouse, sunDir: Vec3, animTime: Float) {
        val x = house.x
        val y = house.y
        val hw = house.width / 2f
        val hd = house.depth / 2f
        val wallH = 3.2f
        val roofH = 5.2f

        val b0 = Vec3(x - hw, y - hd, 0f)
        val b1 = Vec3(x + hw, y - hd, 0f)
        val b2 = Vec3(x + hw, y + hd, 0f)
        val b3 = Vec3(x - hw, y + hd, 0f)

        val t0 = Vec3(x - hw, y - hd, wallH)
        val t1 = Vec3(x + hw, y - hd, wallH)
        val t2 = Vec3(x + hw, y + hd, wallH)
        val t3 = Vec3(x - hw, y + hd, wallH)

        val ridge0 = Vec3(x - hw, y, roofH)
        val ridge1 = Vec3(x + hw, y, roofH)

        val wallColor = Color(0xFFFFFBEB)
        val roofColor = Color(house.roofColorHex)

        drawFace(drawScope, camera, b0, b1, t1, t0, wallColor, sunDir)
        drawFace(drawScope, camera, b1, b2, t2, t1, wallColor, sunDir)
        drawFace(drawScope, camera, b2, b3, t3, t2, wallColor, sunDir)
        drawFace(drawScope, camera, b3, b0, t0, t3, wallColor, sunDir)

        drawFace(drawScope, camera, t0, t1, ridge1, ridge0, roofColor, sunDir)
        drawFace(drawScope, camera, t3, ridge0, ridge1, t2, roofColor, sunDir)
        drawFace(drawScope, camera, t0, ridge0, t3, null, wallColor, sunDir)
        drawFace(drawScope, camera, t1, t2, ridge1, null, wallColor, sunDir)

        // Puerta
        val d0 = Vec3(x - 0.7f, y + hd + 0.02f, 0f)
        val d1 = Vec3(x + 0.7f, y + hd + 0.02f, 0f)
        val d2 = Vec3(x + 0.7f, y + hd + 0.02f, 1.7f)
        val d3 = Vec3(x - 0.7f, y + hd + 0.02f, 1.7f)
        drawFace(drawScope, camera, d0, d1, d2, d3, Color(0xFFB08968), sunDir)

        // Ventana
        val w0 = Vec3(x - 1.8f, y + hd + 0.02f, 1.2f)
        val w1 = Vec3(x - 0.9f, y + hd + 0.02f, 1.2f)
        val w2 = Vec3(x - 0.9f, y + hd + 0.02f, 2.1f)
        val w3 = Vec3(x - 1.8f, y + hd + 0.02f, 2.1f)
        drawFace(drawScope, camera, w0, w1, w2, w3, Color(0xFFBAE6FD), sunDir)

        // Chimenea y humo
        val chimX = x + hw * 0.5f
        val chimY = y - hd * 0.2f
        val chimBase = Vec3(chimX, chimY, wallH + 1.2f)
        val chimTop = Vec3(chimX, chimY, wallH + 2.4f)
        val cpB = camera.project(chimBase)
        val cpT = camera.project(chimTop)
        if (cpB.isVisible && cpT.isVisible) {
            drawScope.drawLine(Color(0xFF292524), Offset(cpB.screenX, cpB.screenY), Offset(cpT.screenX, cpT.screenY), strokeWidth = cpT.scale * 0.45f + 3f)
            drawScope.drawLine(Color(0xFFE07A5F), Offset(cpB.screenX, cpB.screenY), Offset(cpT.screenX, cpT.screenY), strokeWidth = cpT.scale * 0.45f)

            for (i in 0 until 3) {
                val smokeTime = (animTime * 1.5f + i * 1.1f) % 3.0f
                val smokeZ = wallH + 2.4f + smokeTime * 1.4f
                val smokeW = chimX + sin(smokeTime * 2.5f).toFloat() * 0.4f
                val sp = camera.project(smokeW, chimY, smokeZ)
                if (sp.isVisible) {
                    val sRad = sp.scale * (0.25f + smokeTime * 0.2f)
                    drawScope.drawCircle(Color(0xFF292524), radius = sRad + 2f, center = Offset(sp.screenX, sp.screenY))
                    drawScope.drawCircle(Color(0xFFFFFDF8), radius = sRad, center = Offset(sp.screenX, sp.screenY))
                }
            }
        }

        // Oficina Postal: buzón rojo japonés y cajas apiladas
        if (house.name.contains("Postal")) {
            val mbPos = Vec3(x + 1.8f, y + hd + 1.0f, 0.7f)
            val mbP = camera.project(mbPos)
            if (mbP.isVisible) {
                val r = mbP.scale * 0.35f
                drawScope.drawCircle(Color(0xFF292524), radius = r + 2.5f, center = Offset(mbP.screenX, mbP.screenY))
                drawScope.drawCircle(Color(0xFFDC2626), radius = r, center = Offset(mbP.screenX, mbP.screenY))
                drawScope.drawCircle(Color.White, radius = r * 0.4f, center = Offset(mbP.screenX, mbP.screenY - r * 0.15f))
            }
            val crateP = camera.project(x - 2.0f, y + hd + 0.8f, 0.3f)
            if (crateP.isVisible) {
                val cr = crateP.scale * 0.35f
                drawScope.drawRoundRect(
                    color = Color(0xFF292524),
                    topLeft = Offset(crateP.screenX - cr - 2f, crateP.screenY - cr - 2f),
                    size = Size(cr * 2f + 4f, cr * 2f + 4f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
                )
                drawScope.drawRoundRect(
                    color = Color(0xFFD4A373),
                    topLeft = Offset(crateP.screenX - cr, crateP.screenY - cr),
                    size = Size(cr * 2f, cr * 2f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
                )
            }
        }
    }

    fun drawTree(drawScope: DrawScope, camera: EngineCamera3D, tree: WorldTree, sunDir: Vec3) {
        val x = tree.x
        val y = tree.y
        val sc = tree.scale
        val trunkH = 1.4f * sc

        val b0 = Vec3(x - 0.25f * sc, y, 0f)
        val b1 = Vec3(x + 0.25f * sc, y, 0f)
        val t1 = Vec3(x + 0.25f * sc, y, trunkH)
        val t0 = Vec3(x - 0.25f * sc, y, trunkH)
        drawFace(drawScope, camera, b0, b1, t1, t0, Color(0xFF9C6644), sunDir)

        if (tree.type == 0) {
            for (i in 0 until 3) {
                val baseZ = trunkH + i * 0.9f * sc
                val tipZ = baseZ + 1.5f * sc
                val r = (1.7f - i * 0.4f) * sc

                val c0 = Vec3(x - r, y - r, baseZ)
                val c1 = Vec3(x + r, y - r, baseZ)
                val c2 = Vec3(x + r, y + r, baseZ)
                val c3 = Vec3(x - r, y + r, baseZ)
                val tip = Vec3(x, y, tipZ)

                val pineColor = Color(0xFF68A691)
                drawFace(drawScope, camera, c0, c1, tip, null, pineColor, sunDir)
                drawFace(drawScope, camera, c1, c2, tip, null, pineColor, sunDir)
                drawFace(drawScope, camera, c2, c3, tip, null, pineColor, sunDir)
                drawFace(drawScope, camera, c3, c0, tip, null, pineColor, sunDir)
            }
        } else {
            val foliageZ = trunkH + 1.4f * sc
            val fp = camera.project(x, y, foliageZ)
            if (fp.isVisible) {
                val fr = fp.scale * 1.6f * sc
                drawScope.drawCircle(Color(0xFF292524), radius = fr + 3.5f, center = Offset(fp.screenX, fp.screenY))
                drawScope.drawCircle(Color(0xFFA7D7C5), radius = fr, center = Offset(fp.screenX, fp.screenY))
                drawScope.drawCircle(Color(0x334E9F3D), radius = fr * 0.75f, center = Offset(fp.screenX + fr * 0.2f, fp.screenY + fr * 0.2f))
            }
        }
    }

    fun drawFlower(drawScope: DrawScope, camera: EngineCamera3D, x: Float, y: Float, color: Color) {
        val p = camera.project(x, y, 0.05f)
        if (!p.isVisible) return
        val r = p.scale * 0.22f
        drawScope.drawCircle(Color(0xFF292524), radius = r + 1.5f, center = Offset(p.screenX, p.screenY))
        drawScope.drawCircle(color, radius = r, center = Offset(p.screenX, p.screenY))
        drawScope.drawCircle(Color.White, radius = r * 0.35f, center = Offset(p.screenX, p.screenY))
    }

    fun drawButterfly(drawScope: DrawScope, camera: EngineCamera3D, x: Float, y: Float, z: Float, time: Float) {
        val p = camera.project(x, y, z)
        if (!p.isVisible) return
        val r = p.scale * 0.18f
        val wingFlap = sin(time * 16f).toFloat() * r * 0.8f
        drawScope.drawCircle(Color(0xFF292524), radius = r + 1f, center = Offset(p.screenX - r + wingFlap, p.screenY))
        drawScope.drawCircle(Color(0xFFF472B6), radius = r, center = Offset(p.screenX - r + wingFlap, p.screenY))
        drawScope.drawCircle(Color(0xFF292524), radius = r + 1f, center = Offset(p.screenX + r - wingFlap, p.screenY))
        drawScope.drawCircle(Color(0xFFF472B6), radius = r, center = Offset(p.screenX + r - wingFlap, p.screenY))
    }

    fun drawBench(drawScope: DrawScope, camera: EngineCamera3D, x: Float, y: Float) {
        val p = camera.project(x, y, 0.35f)
        if (!p.isVisible) return
        val bw = p.scale * 0.9f
        val bh = p.scale * 0.35f
        drawScope.drawRoundRect(
            color = Color(0xFF292524),
            topLeft = Offset(p.screenX - bw / 2 - 2f, p.screenY - bh / 2 - 2f),
            size = Size(bw + 4f, bh + 4f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
        )
        drawScope.drawRoundRect(
            color = Color(0xFFD4A373),
            topLeft = Offset(p.screenX - bw / 2, p.screenY - bh / 2),
            size = Size(bw, bh),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
        )
    }

    fun drawFountain(drawScope: DrawScope, camera: EngineCamera3D, animTime: Float, sunDir: Vec3) {
        val cx = 40f
        val cy = 40f
        val basinR = 3.4f
        val basinH = 0.9f
        val segs = 8
        val botPts = (0 until segs).map { i ->
            val a = i * 2 * PI / segs
            Vec3(cx + cos(a).toFloat() * basinR, cy + sin(a).toFloat() * basinR, 0f)
        }
        val topPts = (0 until segs).map { i ->
            val a = i * 2 * PI / segs
            Vec3(cx + cos(a).toFloat() * basinR, cy + sin(a).toFloat() * basinR, basinH)
        }

        for (i in 0 until segs) {
            val next = (i + 1) % segs
            drawFace(drawScope, camera, botPts[i], botPts[next], topPts[next], topPts[i], Color(0xFFE2E8F0), sunDir)
        }

        val wPts = topPts.map { camera.project(it.x, it.y, basinH * 0.82f) }
        if (wPts.all { it.isVisible }) {
            val path = Path().apply {
                moveTo(wPts[0].screenX, wPts[0].screenY)
                for (pt in wPts.drop(1)) lineTo(pt.screenX, pt.screenY)
                close()
            }
            drawScope.drawPath(path, Color(0xFF81BFBC))
            drawScope.drawPath(path, Color(0xFF292524), style = Stroke(width = 2.5f))
        }

        val pT = camera.project(cx, cy, 2.0f)
        val pB = camera.project(cx, cy, 0f)
        if (pT.isVisible && pB.isVisible) {
            drawScope.drawLine(Color(0xFF292524), Offset(pB.screenX, pB.screenY), Offset(pT.screenX, pT.screenY), strokeWidth = pT.scale * 0.7f + 3f)
            drawScope.drawLine(Color(0xFFE2E8F0), Offset(pB.screenX, pB.screenY), Offset(pT.screenX, pT.screenY), strokeWidth = pT.scale * 0.7f)
        }

        for (i in 0 until 6) {
            val a = (i * 60f + animTime * 100f) * PI / 180f
            val dropDist = 1.3f + 0.3f * sin(animTime * 3f + i).toFloat()
            val dropZ = 2.0f - (0.7f * (animTime * 2.5f + i) % 1.0f).toFloat()
            val dp = camera.project(cx + cos(a).toFloat() * dropDist, cy + sin(a).toFloat() * dropDist, dropZ)
            if (dp.isVisible) {
                drawScope.drawCircle(Color(0xFF292524), radius = dp.scale * 0.14f + 1f, center = Offset(dp.screenX, dp.screenY))
                drawScope.drawCircle(Color(0xFFBAE6FD), radius = dp.scale * 0.14f, center = Offset(dp.screenX, dp.screenY))
            }
        }
    }

    fun drawCampfire(drawScope: DrawScope, camera: EngineCamera3D, animTime: Float, sunDir: Vec3) {
        val cx = 64f
        val cy = 50f
        for (i in 0 until 8) {
            val a = i * 2 * PI / 8
            val rp = camera.project(cx + cos(a).toFloat() * 1.8f, cy + sin(a).toFloat() * 1.8f, 0.35f)
            if (rp.isVisible) {
                val r = rp.scale * 0.36f
                drawScope.drawCircle(Color(0xFF292524), radius = r + 2f, center = Offset(rp.screenX, rp.screenY))
                drawScope.drawCircle(Color(0xFF78716C), radius = r, center = Offset(rp.screenX, rp.screenY))
            }
        }

        val t1 = camera.project(cx - 1.1f, cy - 0.2f, 0.2f)
        val t2 = camera.project(cx + 1.1f, cy + 0.2f, 0.2f)
        if (t1.isVisible && t2.isVisible) {
            drawScope.drawLine(Color(0xFF292524), Offset(t1.screenX, t1.screenY), Offset(t2.screenX, t2.screenY), strokeWidth = t1.scale * 0.45f + 3f, cap = StrokeCap.Round)
            drawScope.drawLine(Color(0xFF9C6644), Offset(t1.screenX, t1.screenY), Offset(t2.screenX, t2.screenY), strokeWidth = t1.scale * 0.45f, cap = StrokeCap.Round)
        }

        val flameP = camera.project(cx, cy, 0.6f + 0.2f * sin(animTime * 8f).toFloat())
        if (flameP.isVisible) {
            val fr = flameP.scale * 0.9f
            drawScope.drawCircle(Color(0xFF292524), radius = fr + 2f, center = Offset(flameP.screenX, flameP.screenY))
            drawScope.drawCircle(Color(0xFFF97316), radius = fr, center = Offset(flameP.screenX, flameP.screenY))
            drawScope.drawCircle(Color(0xFFFEF08A), radius = fr * 0.55f, center = Offset(flameP.screenX, flameP.screenY))
        }
    }

    fun drawPier(drawScope: DrawScope, camera: EngineCamera3D, sunDir: Vec3) {
        val d0 = Vec3(40f, 60f, 0.5f)
        val d1 = Vec3(45f, 60f, 0.5f)
        val d2 = Vec3(45f, 76f, 0.5f)
        val d3 = Vec3(40f, 76f, 0.5f)
        drawFace(drawScope, camera, d0, d1, d2, d3, Color(0xFFD4A373), sunDir)
    }

    fun drawGoal(drawScope: DrawScope, camera: EngineCamera3D, cx: Float, cy: Float, isNorth: Boolean) {
        val gw = 2.5f
        val gh = 2.0f
        val pL = camera.project(cx - gw, cy, 0f)
        val pR = camera.project(cx + gw, cy, 0f)
        val pLT = camera.project(cx - gw, cy, gh)
        val pRT = camera.project(cx + gw, cy, gh)

        val col = if (isNorth) Color(0xFF81BFBC) else Color(0xFFF28482)
        if (pL.isVisible && pLT.isVisible) {
            drawScope.drawLine(Color(0xFF292524), Offset(pL.screenX, pL.screenY), Offset(pLT.screenX, pLT.screenY), strokeWidth = 8f)
            drawScope.drawLine(col, Offset(pL.screenX, pL.screenY), Offset(pLT.screenX, pLT.screenY), strokeWidth = 5f)
        }
        if (pR.isVisible && pRT.isVisible) {
            drawScope.drawLine(Color(0xFF292524), Offset(pR.screenX, pR.screenY), Offset(pRT.screenX, pRT.screenY), strokeWidth = 8f)
            drawScope.drawLine(col, Offset(pR.screenX, pR.screenY), Offset(pRT.screenX, pRT.screenY), strokeWidth = 5f)
        }
        if (pLT.isVisible && pRT.isVisible) {
            drawScope.drawLine(Color(0xFF292524), Offset(pLT.screenX, pLT.screenY), Offset(pRT.screenX, pRT.screenY), strokeWidth = 8f)
            drawScope.drawLine(col, Offset(pLT.screenX, pLT.screenY), Offset(pRT.screenX, pRT.screenY), strokeWidth = 5f)
        }
    }

    fun drawSoccerBall(drawScope: DrawScope, camera: EngineCamera3D, x: Float, y: Float, z: Float) {
        val sp = camera.project(x, y, 0.03f)
        if (sp.isVisible) {
            val sw = sp.scale * 0.95f
            val sh = sp.scale * 0.45f
            drawScope.drawOval(Color(0x55292524), Offset(sp.screenX - sw / 2, sp.screenY - sh / 2), Size(sw, sh))
        }
        val bp = camera.project(x, y, z + 0.35f)
        if (bp.isVisible) {
            val rad = bp.scale * 0.42f
            drawScope.drawCircle(Color(0xFF292524), radius = rad + 2.5f, center = Offset(bp.screenX, bp.screenY))
            drawScope.drawCircle(Color(0xFFF8FAFC), radius = rad, center = Offset(bp.screenX, bp.screenY))
            drawScope.drawCircle(Color(0xFF1E293B), radius = rad * 0.35f, center = Offset(bp.screenX, bp.screenY))
        }
    }

    fun drawStar(drawScope: DrawScope, camera: EngineCamera3D, x: Float, y: Float, z: Float, animTime: Float) {
        val p = camera.project(x, y, z)
        if (!p.isVisible) return
        val rad = p.scale * 0.55f
        val spin = animTime * 3f
        val path = Path()
        for (i in 0 until 10) {
            val a = (i * PI / 5 + spin).toFloat()
            val r = if (i % 2 == 0) rad else rad * 0.45f
            val px = p.screenX + cos(a) * r
            val py = p.screenY + sin(a) * r
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        drawScope.drawPath(path, Color(0xFF292524), style = Stroke(width = 3.5f))
        drawScope.drawPath(path, Color(0xFFFBBF24))
    }

    private fun drawFace(
        drawScope: DrawScope,
        camera: EngineCamera3D,
        v0: Vec3,
        v1: Vec3,
        v2: Vec3,
        v3: Vec3? = null,
        baseColor: Color,
        sunDir: Vec3,
        hasOutline: Boolean = true
    ) {
        val p0 = camera.project(v0)
        val p1 = camera.project(v1)
        val p2 = camera.project(v2)
        val p3 = v3?.let { camera.project(it) }

        if (!p0.isVisible || !p1.isVisible || !p2.isVisible || (p3 != null && !p3.isVisible)) return

        val normal = (v1 - v0).cross(v2 - v0).normalized()
        val isLit = normal.dot(sunDir) > 0.15f

        val faceColor = if (isLit) {
            baseColor
        } else {
            Color(
                red = (baseColor.red * 0.82f).coerceIn(0f, 1f),
                green = (baseColor.green * 0.82f).coerceIn(0f, 1f),
                blue = (baseColor.blue * 0.88f).coerceIn(0f, 1f),
                alpha = baseColor.alpha
            )
        }

        val path = Path().apply {
            moveTo(p0.screenX, p0.screenY)
            lineTo(p1.screenX, p1.screenY)
            lineTo(p2.screenX, p2.screenY)
            if (p3 != null) lineTo(p3.screenX, p3.screenY)
            close()
        }
        drawScope.drawPath(path, faceColor)

        // Tramas de lápiz para sombras (Moebius hatching)
        if (!isLit) {
            val minX = min(min(p0.screenX, p1.screenX), min(p2.screenX, p3?.screenX ?: p2.screenX))
            val maxX = max(max(p0.screenX, p1.screenX), max(p2.screenX, p3?.screenX ?: p2.screenX))
            val step = 10f
            var curX = minX
            while (curX < maxX) {
                drawScope.drawLine(
                    Color(0x221C1917),
                    Offset(curX, p0.screenY - 8f),
                    Offset(curX + 16f, p2.screenY + 8f),
                    strokeWidth = 1.2f
                )
                curX += step
            }
        }

        if (hasOutline) {
            drawScope.drawPath(path, Color(0xFF292524), style = Stroke(width = 2.8f, join = StrokeJoin.Round))
        }
    }
}
