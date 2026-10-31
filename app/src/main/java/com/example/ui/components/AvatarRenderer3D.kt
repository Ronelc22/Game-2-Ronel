package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.example.engine3d.EngineCamera3D
import com.example.engine3d.Vec3
import com.example.model.EmoteType
import com.example.model.Player
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renderizador de personajes Chibi estilo Messenger (Abeto),
 * con boina de cartero, morral cruzado de cuero, paquete postal en mano,
 * mejillas sonrojadas, headset gamer y bocadillo de voz espacial en vivo.
 */
object AvatarRenderer3D {

    fun drawCharacter(
        drawScope: DrawScope,
        camera: EngineCamera3D,
        player: Player,
        isLocal: Boolean,
        animTime: Float
    ) {
        val px = player.x
        val py = player.y
        val pz = if (player.isSitting) -0.3f else player.z

        // Sombra suave en el suelo
        val shadowP = camera.project(px, py, 0.03f)
        if (shadowP.isVisible) {
            val sw = shadowP.scale * 0.95f
            val sh = shadowP.scale * 0.5f
            drawScope.drawOval(
                Color(0x55292524),
                Offset(shadowP.screenX - sw / 2, shadowP.screenY - sh / 2),
                Size(sw, sh)
            )

            // Ondas sonoras de voz en el suelo
            if (player.isSpeaking || player.voiceLevel > 0.08f) {
                val pulse = (animTime * 6f) % 2f
                val auraR = shadowP.scale * (1.8f + pulse * 1.5f)
                drawScope.drawCircle(
                    Color(0xFF292524),
                    radius = auraR + 2f,
                    center = Offset(shadowP.screenX, shadowP.screenY),
                    style = Stroke(width = 2f)
                )
                drawScope.drawCircle(
                    Color(0xFF81BFBC),
                    radius = auraR,
                    center = Offset(shadowP.screenX, shadowP.screenY),
                    style = Stroke(width = 4f)
                )
            }
        }

        val charYaw = Math.toRadians(player.headingAngle.toDouble()).toFloat()
        val forwardX = sin(charYaw)
        val forwardY = -cos(charYaw)
        val rightX = cos(charYaw)
        val rightY = sin(charYaw)

        val walkCycle = if (player.isMoving) sin(animTime * 14f).toFloat() else 0f
        val legSwing = walkCycle * 0.35f

        // 1. Piernas Chibi
        val pelvisPos = Vec3(px, py, pz + 0.85f)
        val leftFoot = Vec3(px - rightX * 0.22f + forwardX * legSwing, py - rightY * 0.22f + forwardY * legSwing, pz)
        val rightFoot = Vec3(px + rightX * 0.22f - forwardX * legSwing, py + rightY * 0.22f - forwardY * legSwing, pz)

        val pPelvis = camera.project(pelvisPos)
        val pLF = camera.project(leftFoot)
        val pRF = camera.project(rightFoot)

        if (pPelvis.isVisible && pLF.isVisible && pRF.isVisible) {
            val legW = pPelvis.scale * 0.22f
            drawScope.drawLine(Color(0xFF292524), Offset(pPelvis.screenX, pPelvis.screenY), Offset(pLF.screenX, pLF.screenY), strokeWidth = legW + 3f, cap = StrokeCap.Round)
            drawScope.drawLine(Color(0xFF292524), Offset(pPelvis.screenX, pPelvis.screenY), Offset(pRF.screenX, pRF.screenY), strokeWidth = legW + 3f, cap = StrokeCap.Round)
            drawScope.drawLine(player.avatar.pantsColor, Offset(pPelvis.screenX, pPelvis.screenY), Offset(pLF.screenX, pLF.screenY), strokeWidth = legW, cap = StrokeCap.Round)
            drawScope.drawLine(player.avatar.pantsColor, Offset(pPelvis.screenX, pPelvis.screenY), Offset(pRF.screenX, pRF.screenY), strokeWidth = legW, cap = StrokeCap.Round)
        }

        // 2. Torso Suéter Oversized en tono pastel Abeto (#81BFBC)
        val chestPos = Vec3(px, py, pz + 1.55f)
        val pChest = camera.project(chestPos)
        if (pPelvis.isVisible && pChest.isVisible) {
            val torsoW = pChest.scale * 0.52f
            drawScope.drawLine(Color(0xFF292524), Offset(pPelvis.screenX, pPelvis.screenY), Offset(pChest.screenX, pChest.screenY), strokeWidth = torsoW + 3f, cap = StrokeCap.Round)
            drawScope.drawLine(player.avatar.shirtColor, Offset(pPelvis.screenX, pPelvis.screenY), Offset(pChest.screenX, pChest.screenY), strokeWidth = torsoW, cap = StrokeCap.Round)
        }

        // 3. Cartera de Mensajero / Satchel Bag cruzada de cuero (El sello de Messenger!)
        if (pChest.isVisible) {
            val bagW = pChest.scale * 0.36f
            val bagH = pChest.scale * 0.28f
            val bagX = pChest.screenX + rightX * pChest.scale * 0.18f
            val bagY = pChest.screenY + pChest.scale * 0.25f

            // Correa cruzada
            drawScope.drawLine(
                Color(0xFF78350F),
                Offset(pChest.screenX - pChest.scale * 0.22f, pChest.screenY - pChest.scale * 0.35f),
                Offset(bagX, bagY),
                strokeWidth = pChest.scale * 0.08f
            )
            // Bolso de cuero con contorno
            drawScope.drawRoundRect(
                color = Color(0xFF292524),
                topLeft = Offset(bagX - bagW / 2 - 2f, bagY - bagH / 2 - 2f),
                size = Size(bagW + 4f, bagH + 4f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
            )
            drawScope.drawRoundRect(
                color = player.avatar.bagColor,
                topLeft = Offset(bagX - bagW / 2, bagY - bagH / 2),
                size = Size(bagW, bagH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
            )
            drawScope.drawCircle(Color(0xFFFBBF24), radius = bagW * 0.15f, center = Offset(bagX, bagY))
        }

        // 4. Brazos, Paquete en mano y Emotes
        var lHand = Vec3(px - rightX * 0.45f - forwardX * legSwing, py - rightY * 0.45f - forwardY * legSwing, pz + 0.95f)
        var rHand = Vec3(px + rightX * 0.45f + forwardX * legSwing, py + rightY * 0.45f + forwardY * legSwing, pz + 0.95f)

        // Si lleva encomienda, sostiene el paquete hacia adelante
        if (player.isCarryingParcel || player.activeDelivery != null) {
            rHand = Vec3(px + forwardX * 0.5f, py + forwardY * 0.5f, pz + 1.25f)
        }

        if (player.currentEmote == EmoteType.WAVE) {
            val waveSwing = sin(animTime * 18f).toFloat() * 0.3f
            rHand = Vec3(px + rightX * 0.45f + waveSwing, py + rightY * 0.45f, pz + 2.1f)
        } else if (player.currentEmote == EmoteType.CHEER || player.currentEmote == EmoteType.MAIL) {
            lHand = Vec3(px - rightX * 0.45f, py - rightY * 0.45f, pz + 2.1f)
            rHand = Vec3(px + rightX * 0.45f, py + rightY * 0.45f, pz + 2.1f)
        } else if (player.currentEmote == EmoteType.DANCE) {
            val danceShift = sin(animTime * 16f).toFloat() * 0.4f
            lHand = Vec3(px - rightX * 0.45f + danceShift, py - rightY * 0.45f, pz + 1.4f)
            rHand = Vec3(px + rightX * 0.45f - danceShift, py + rightY * 0.45f, pz + 1.6f)
        }

        val pLH = camera.project(lHand)
        val pRH = camera.project(rHand)
        if (pChest.isVisible && pLH.isVisible && pRH.isVisible) {
            val armW = pChest.scale * 0.18f
            drawScope.drawLine(Color(0xFF292524), Offset(pChest.screenX, pChest.screenY), Offset(pLH.screenX, pLH.screenY), strokeWidth = armW + 3f, cap = StrokeCap.Round)
            drawScope.drawLine(Color(0xFF292524), Offset(pChest.screenX, pChest.screenY), Offset(pRH.screenX, pRH.screenY), strokeWidth = armW + 3f, cap = StrokeCap.Round)
            drawScope.drawLine(player.avatar.shirtColor, Offset(pChest.screenX, pChest.screenY), Offset(pLH.screenX, pLH.screenY), strokeWidth = armW, cap = StrokeCap.Round)
            drawScope.drawLine(player.avatar.shirtColor, Offset(pChest.screenX, pChest.screenY), Offset(pRH.screenX, pRH.screenY), strokeWidth = armW, cap = StrokeCap.Round)

            // Dibujar el paquete postal en la mano si lo lleva
            if (player.isCarryingParcel || player.activeDelivery != null) {
                val pw = pRH.scale * 0.32f
                drawScope.drawRoundRect(
                    color = Color(0xFF292524),
                    topLeft = Offset(pRH.screenX - pw / 2 - 2f, pRH.screenY - pw / 2 - 2f),
                    size = Size(pw + 4f, pw + 4f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
                )
                drawScope.drawRoundRect(
                    color = Color(0xFFD4A373),
                    topLeft = Offset(pRH.screenX - pw / 2, pRH.screenY - pw / 2),
                    size = Size(pw, pw),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
                )
                drawScope.drawLine(Color(0xFFDC2626), Offset(pRH.screenX, pRH.screenY - pw / 2), Offset(pRH.screenX, pRH.screenY + pw / 2), strokeWidth = 2f)
                drawScope.drawLine(Color(0xFFDC2626), Offset(pRH.screenX - pw / 2, pRH.screenY), Offset(pRH.screenX + pw / 2, pRH.screenY), strokeWidth = 2f)
            }
        }

        // 5. Cabeza Chibi con mejillas sonrojadas y estilo Abeto
        val headPos = Vec3(px, py, pz + 2.05f)
        val pHead = camera.project(headPos)
        if (!pHead.isVisible) return

        val headR = pHead.scale * 0.42f
        drawScope.drawCircle(Color(0xFF292524), radius = headR + 3f, center = Offset(pHead.screenX, pHead.screenY))
        drawScope.drawCircle(player.avatar.skinTone, radius = headR, center = Offset(pHead.screenX, pHead.screenY))

        val relAngle = charYaw - camera.yawRad
        val faceVisible = cos(relAngle) > -0.3f
        if (faceVisible) {
            val eyeShift = sin(relAngle) * headR * 0.4f
            val eyeY = pHead.screenY - headR * 0.05f
            val eyeSpacing = headR * 0.28f

            // Ojos con brillo anime
            drawScope.drawCircle(Color(0xFF1E293B), radius = headR * 0.11f, center = Offset(pHead.screenX + eyeShift - eyeSpacing, eyeY))
            drawScope.drawCircle(Color(0xFF1E293B), radius = headR * 0.11f, center = Offset(pHead.screenX + eyeShift + eyeSpacing, eyeY))
            drawScope.drawCircle(Color.White, radius = headR * 0.04f, center = Offset(pHead.screenX + eyeShift - eyeSpacing - headR * 0.03f, eyeY - headR * 0.03f))
            drawScope.drawCircle(Color.White, radius = headR * 0.04f, center = Offset(pHead.screenX + eyeShift + eyeSpacing - headR * 0.03f, eyeY - headR * 0.03f))

            // Mejillas rosadas
            drawScope.drawCircle(Color(0x88F472B6), radius = headR * 0.16f, center = Offset(pHead.screenX + eyeShift - eyeSpacing - headR * 0.08f, eyeY + headR * 0.18f))
            drawScope.drawCircle(Color(0x88F472B6), radius = headR * 0.16f, center = Offset(pHead.screenX + eyeShift + eyeSpacing + headR * 0.08f, eyeY + headR * 0.18f))

            // Sonrisa pequeña
            val smilePath = Path().apply {
                moveTo(pHead.screenX + eyeShift - headR * 0.12f, eyeY + headR * 0.22f)
                quadraticTo(pHead.screenX + eyeShift, eyeY + headR * 0.32f, pHead.screenX + eyeShift + headR * 0.12f, eyeY + headR * 0.22f)
            }
            drawScope.drawPath(smilePath, Color(0xFF292524), style = Stroke(width = 2.5f, cap = StrokeCap.Round))
        }

        // Cabello / Boina de Cartero
        val hairY = pHead.screenY - headR * 0.35f
        drawScope.drawCircle(Color(0xFF292524), radius = headR * 0.95f + 2f, center = Offset(pHead.screenX, hairY))
        drawScope.drawCircle(player.avatar.hairColor, radius = headR * 0.95f, center = Offset(pHead.screenX, hairY))

        // Gorra de Mensajero
        val capPath = Path().apply {
            moveTo(pHead.screenX - headR * 1.05f, hairY)
            quadraticTo(pHead.screenX, hairY - headR * 0.75f, pHead.screenX + headR * 1.05f, hairY)
            close()
        }
        drawScope.drawPath(capPath, Color(0xFF292524), style = Stroke(width = 3f))
        drawScope.drawPath(capPath, Color(0xFF0F766E))

        // Headset Gamer con Micrófono y LED
        val earLPos = Offset(pHead.screenX - headR * 1.0f, pHead.screenY)
        val earRPos = Offset(pHead.screenX + headR * 1.0f, pHead.screenY)
        drawScope.drawCircle(Color(0xFF292524), radius = headR * 0.35f + 2f, center = earLPos)
        drawScope.drawCircle(Color(0xFF292524), radius = headR * 0.35f + 2f, center = earRPos)
        drawScope.drawCircle(player.avatar.headsetColor, radius = headR * 0.35f, center = earLPos)
        drawScope.drawCircle(player.avatar.headsetColor, radius = headR * 0.35f, center = earRPos)

        val micTip = Offset(pHead.screenX + headR * 0.45f, pHead.screenY + headR * 0.45f)
        drawScope.drawLine(Color(0xFF292524), earRPos, micTip, strokeWidth = headR * 0.1f)
        drawScope.drawCircle(Color(0xFF10B981), radius = headR * 0.16f, center = micTip)

        // 6. Efectos de Emotes Flotantes
        if (player.currentEmote != null) {
            val floatOffset = (animTime * 30f) % 25f
            drawScope.drawContext.canvas.nativeCanvas.drawText(
                player.currentEmote!!.particleEmoji,
                pHead.screenX - 12f,
                pHead.screenY - headR * 1.6f - floatOffset,
                android.graphics.Paint().apply { textSize = 32f }
            )
        }

        // 7. Etiqueta con Nombre del Jugador en 3D
        val nameTagY = pHead.screenY - headR * 1.4f
        val tagPaint = android.graphics.Paint().apply {
            textSize = (pHead.scale * 0.42f).coerceIn(20f, 32f)
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
            color = android.graphics.Color.WHITE
        }
        val bgPaint = android.graphics.Paint().apply {
            color = if (isLocal) android.graphics.Color.argb(220, 41, 37, 36) else android.graphics.Color.argb(200, 28, 25, 23)
            style = android.graphics.Paint.Style.FILL
        }
        val textWidth = tagPaint.measureText(player.name)
        val pad = 14f
        drawScope.drawContext.canvas.nativeCanvas.drawRoundRect(
            pHead.screenX - textWidth / 2 - pad,
            nameTagY - 26f,
            pHead.screenX + textWidth / 2 + pad,
            nameTagY + 8f,
            14f, 14f,
            bgPaint
        )
        drawScope.drawContext.canvas.nativeCanvas.drawText(player.name, pHead.screenX, nameTagY, tagPaint)

        // 8. Bocadillo con Ondas de Voz en Vivo
        if (player.isSpeaking || player.voiceLevel > 0.08f) {
            val bubbleY = nameTagY - 38f
            drawScope.drawContext.canvas.nativeCanvas.drawRoundRect(
                pHead.screenX - 34f,
                bubbleY - 26f,
                pHead.screenX + 34f,
                bubbleY + 14f,
                14f, 14f,
                android.graphics.Paint().apply { color = android.graphics.Color.argb(240, 41, 37, 36) }
            )
            drawScope.drawContext.canvas.nativeCanvas.drawRoundRect(
                pHead.screenX - 32f,
                bubbleY - 24f,
                pHead.screenX + 32f,
                bubbleY + 12f,
                12f, 12f,
                android.graphics.Paint().apply { color = android.graphics.Color.argb(250, 129, 191, 188) }
            )
            for (b in -2..2) {
                val barAmp = ((sin(animTime * 15f + b) + 1f) * 0.5f * (player.voiceLevel + 0.4f)).coerceIn(0.2f, 1f)
                val barH = 16f * barAmp
                val barX = pHead.screenX + b * 10f
                drawScope.drawLine(Color.White, Offset(barX, bubbleY + barH / 2), Offset(barX, bubbleY - barH / 2), strokeWidth = 3.5f, cap = StrokeCap.Round)
            }
        } else if (player.speechBubbleTimer > 0f && player.lastSpokenText.isNotEmpty()) {
            val bubbleY = nameTagY - 34f
            val bubblePaint = android.graphics.Paint().apply {
                textSize = 22f
                color = android.graphics.Color.BLACK
                textAlign = android.graphics.Paint.Align.CENTER
            }
            val msgText = if (player.lastSpokenText.length > 25) player.lastSpokenText.take(24) + "..." else player.lastSpokenText
            val bw = bubblePaint.measureText(msgText)
            val padX = 16f

            drawScope.drawContext.canvas.nativeCanvas.drawRoundRect(
                pHead.screenX - bw / 2 - padX - 2f,
                bubbleY - 28f,
                pHead.screenX + bw / 2 + padX + 2f,
                bubbleY + 12f,
                16f, 16f,
                android.graphics.Paint().apply { color = android.graphics.Color.argb(240, 41, 37, 36) }
            )
            drawScope.drawContext.canvas.nativeCanvas.drawRoundRect(
                pHead.screenX - bw / 2 - padX,
                bubbleY - 26f,
                pHead.screenX + bw / 2 + padX,
                bubbleY + 10f,
                14f, 14f,
                android.graphics.Paint().apply { color = android.graphics.Color.argb(245, 255, 251, 235) }
            )
            drawScope.drawContext.canvas.nativeCanvas.drawText(msgText, pHead.screenX, bubbleY - 4f, bubblePaint)
        }
    }
}
