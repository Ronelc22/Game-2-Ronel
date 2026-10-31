package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.engine3d.EngineCamera3D
import com.example.engine3d.Vec3
import com.example.viewmodel.GameUiState
import com.example.world.GameWorld
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * Elemento de renderizado 3D para ordenar por profundidad (Painter's Algorithm).
 */
class Renderable3D(val depth: Float, val render: (DrawScope) -> Unit)

/**
 * Renderizador de Mundo Abierto 3D en Jetpack Compose, integrado con el motor 3D
 * de curvatura de planeta esférico, cel-shading y estética de Messenger (Abeto).
 */
@Composable
fun WorldRenderer(
    uiState: GameUiState,
    world: GameWorld,
    modifier: Modifier = Modifier
) {
    val animTime = (System.currentTimeMillis() % 100000) / 1000f

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val playerPos = Vec3(
            uiState.localPlayer.x,
            uiState.localPlayer.y,
            uiState.localPlayer.z + 1.25f
        )
        val dist = uiState.cameraDistance
        val yawRad = (uiState.cameraAngle * PI / 180f).toFloat()
        val pitchRad = (uiState.cameraPitch * PI / 180f).toFloat()

        val eyeX = playerPos.x - dist * cos(pitchRad) * sin(yawRad)
        val eyeY = playerPos.y - dist * cos(pitchRad) * cos(yawRad)
        val eyeZ = max(0.9f, playerPos.z + dist * sin(pitchRad))
        val eye = Vec3(eyeX, eyeY, eyeZ)

        val focal = w * 1.05f
        val camera = EngineCamera3D(
            eye = eye,
            target = playerPos,
            yawRad = yawRad,
            pitchRad = pitchRad,
            focal = focal,
            viewportWidth = w,
            viewportHeight = h
        )

        val sunDir = Vec3(0.55f, -0.55f, 0.75f).normalized()

        // 1. Cielo pastel suave con gradiente, sol de ilustración y nubes
        EnvironmentRenderer3D.drawSky(this, camera, sunDir, animTime)

        // 2. Océano turquesa con ondas de espuma y borde de arena
        EnvironmentRenderer3D.drawOceanAndShore(this, camera, animTime)

        // 3. Terreno de césped celadón, briznas de hierba '//', caminos y cancha de fútbol
        EnvironmentRenderer3D.drawTerrain(this, camera, world, animTime)

        // 4. Grafo de Elementos 3D ordenados por profundidad (Painter's Algorithm)
        val renderList = mutableListOf<Renderable3D>()

        // Casas estilo pueblo costero japonés con chimeneas y buzón de correos
        for (house in world.houses) {
            val d = camera.project(house.x, house.y, 0f).depth
            renderList.add(Renderable3D(d) { drawScope ->
                EnvironmentRenderer3D.drawHouse(drawScope, camera, house, sunDir, animTime)
            })
        }

        // Postes de tendido eléctrico
        for (pole in world.poles) {
            val d = camera.project(pole.x, pole.y, 0f).depth
            renderList.add(Renderable3D(d) { drawScope ->
                EnvironmentRenderer3D.drawPole(drawScope, camera, pole.x, pole.y)
            })
        }

        // Árboles (pinos y copas redondeadas)
        for (tree in world.trees) {
            val d = camera.project(tree.x, tree.y, 0f).depth
            renderList.add(Renderable3D(d) { drawScope ->
                EnvironmentRenderer3D.drawTree(drawScope, camera, tree, sunDir)
            })
        }

        // Flores silvestres
        for (flower in world.flowers) {
            val d = camera.project(flower.x, flower.y, 0.05f).depth
            renderList.add(Renderable3D(d) { drawScope ->
                EnvironmentRenderer3D.drawFlower(drawScope, camera, flower.x, flower.y, Color(flower.colorHex))
            })
        }

        // Mariposas revoloteando
        for (i in 0 until 4) {
            val bx = 34f + 8f * sin(animTime * 1.2f + i * 1.5f).toFloat()
            val by = 38f + 8f * cos(animTime * 0.9f + i * 2.1f).toFloat()
            val bz = 1.0f + 0.4f * sin(animTime * 4f + i).toFloat()
            val d = camera.project(bx, by, bz).depth
            renderList.add(Renderable3D(d) { drawScope ->
                EnvironmentRenderer3D.drawButterfly(drawScope, camera, bx, by, bz, animTime + i)
            })
        }

        // Bancas de descanso
        for (bench in world.benches) {
            val d = camera.project(bench.x, bench.y, 0f).depth
            renderList.add(Renderable3D(d) { drawScope ->
                EnvironmentRenderer3D.drawBench(drawScope, camera, bench.x, bench.y)
            })
        }

        // Fuente central con agua en movimiento y salpicaduras
        val fountainDepth = camera.project(40f, 40f, 0f).depth
        renderList.add(Renderable3D(fountainDepth) { drawScope ->
            EnvironmentRenderer3D.drawFountain(drawScope, camera, animTime, sunDir)
        })

        // Fogata comunitaria con llamas y rocas
        val campfireDepth = camera.project(64f, 50f, 0f).depth
        renderList.add(Renderable3D(campfireDepth) { drawScope ->
            EnvironmentRenderer3D.drawCampfire(drawScope, camera, animTime, sunDir)
        })

        // Muelle de pescador en el océano turquesa
        val pierDepth = camera.project(42.5f, 68f, 0f).depth
        renderList.add(Renderable3D(pierDepth) { drawScope ->
            EnvironmentRenderer3D.drawPier(drawScope, camera, sunDir)
        })

        // Porterías de fútbol
        val goalNorthDepth = camera.project(22f, 29f, 0f).depth
        renderList.add(Renderable3D(goalNorthDepth) { drawScope ->
            EnvironmentRenderer3D.drawGoal(drawScope, camera, 22f, 29f, isNorth = true)
        })
        val goalSouthDepth = camera.project(22f, 51f, 0f).depth
        renderList.add(Renderable3D(goalSouthDepth) { drawScope ->
            EnvironmentRenderer3D.drawGoal(drawScope, camera, 22f, 51f, isNorth = false)
        })

        // Balón de fútbol con física y sombra
        val ballDepth = camera.project(world.soccerBall.x, world.soccerBall.y, world.soccerBall.z).depth
        renderList.add(Renderable3D(ballDepth) { drawScope ->
            EnvironmentRenderer3D.drawSoccerBall(drawScope, camera, world.soccerBall.x, world.soccerBall.y, world.soccerBall.z)
        })

        // Estrellas coleccionables flotantes con brillo
        for (star in world.stars) {
            if (!star.isCollected) {
                val floatZ = 0.8f + 0.2f * sin(animTime * 4f + star.id).toFloat()
                val sd = camera.project(star.x, star.y, floatZ).depth
                renderList.add(Renderable3D(sd) { drawScope ->
                    EnvironmentRenderer3D.drawStar(drawScope, camera, star.x, star.y, floatZ, animTime + star.id)
                })
            }
        }

        // Personajes Chibi estilo Messenger (Local y Amigos Bots)
        val allPlayers = uiState.companionPlayers + uiState.localPlayer
        for (p in allPlayers) {
            val d = camera.project(p.x, p.y, p.z).depth
            val isLocal = p.id == uiState.localPlayer.id
            renderList.add(Renderable3D(d) { drawScope ->
                AvatarRenderer3D.drawCharacter(
                    drawScope = drawScope,
                    camera = camera,
                    player = p,
                    isLocal = isLocal,
                    animTime = animTime
                )
            })
        }

        // Ordenar y dibujar de más lejano a más cercano (Painter's algorithm)
        renderList.sortByDescending { it.depth }
        for (item in renderList) {
            item.render(this)
        }

        // 5. Cables aéreos de tendido eléctrico (catenarias curvas entre postes)
        EnvironmentRenderer3D.drawPowerCables(this, camera, world.poles)
    }
}
