package com.example

import com.example.model.ActivityZone
import com.example.world.GameWorld
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot

class ExampleUnitTest {

    @Test
    fun testWorldZonesDetection() {
        val world = GameWorld()

        // Cancha de fútbol
        assertEquals(ActivityZone.SOCCER, world.getZoneForPosition(22f, 40f))

        // Muelle de pesca
        assertEquals(ActivityZone.FISHING_PIER, world.getZoneForPosition(42f, 68f))

        // Fogata
        assertEquals(ActivityZone.CAMPFIRE, world.getZoneForPosition(64f, 50f))

        // Plaza central
        assertEquals(ActivityZone.FOUNTAIN, world.getZoneForPosition(40f, 40f))
    }

    @Test
    fun testSoccerBallKickAndFriction() {
        val world = GameWorld()
        world.resetBall()

        val initialX = world.soccerBall.x
        val initialY = world.soccerBall.y

        // Patear el balón desde una posición cercana
        val kicked = world.kickBall(playerX = initialX - 1f, playerY = initialY, playerHeading = 90f, power = 1.0f)
        assertTrue(kicked)
        assertTrue(world.soccerBall.vx != 0f || world.soccerBall.vy != 0f)

        // Simular un paso de física
        world.updateSoccerPhysics()
        assertTrue(world.soccerBall.x != initialX || world.soccerBall.y != initialY)
    }

    @Test
    fun testCollectibleStarsAvailable() {
        val world = GameWorld()
        assertEquals(8, world.stars.size)
        assertTrue(world.stars.any { it.name.contains("Fuente") })
    }

    @Test
    fun testPostOfficeZoneAndDeliveryParcels() {
        val world = GameWorld()
        assertEquals(ActivityZone.POST_OFFICE, world.getZoneForPosition(40f, 23f))
        assertTrue(world.availableParcels.isNotEmpty())
    }

    @Test
    fun test3DEngineMathOperations() {
        val v1 = com.example.engine3d.Vec3(1f, 2f, 3f)
        val v2 = com.example.engine3d.Vec3(4f, 5f, 6f)
        val sum = v1 + v2
        assertEquals(5f, sum.x, 0.001f)
        assertEquals(7f, sum.y, 0.001f)
        assertEquals(9f, sum.z, 0.001f)

        val dot = v1.dot(v2)
        assertEquals(32f, dot, 0.001f)

        val cross = com.example.engine3d.Vec3(1f, 0f, 0f).cross(com.example.engine3d.Vec3(0f, 1f, 0f))
        assertEquals(0f, cross.x, 0.001f)
        assertEquals(0f, cross.y, 0.001f)
        assertEquals(1f, cross.z, 0.001f)
    }

    @Test
    fun testPlanetCurvatureCalculation() {
        val center = com.example.engine3d.Vec3(40f, 40f, 0f)
        val posFar = com.example.engine3d.Vec3(40f, 60f, 0f) // 20m de distancia
        val curved = com.example.engine3d.WorldCurvature.applyPlanetCurvature(posFar, center, 50f)
        // El terreno lejano debe tener z menor (curvado hacia abajo)
        assertTrue(curved.z < 0f)
    }

    @Test
    fun testEngineCameraProjection() {
        val camera = com.example.engine3d.EngineCamera3D(
            eye = com.example.engine3d.Vec3(0f, -10f, 5f),
            target = com.example.engine3d.Vec3(0f, 0f, 0f),
            yawRad = 0f,
            pitchRad = 0.3f,
            focal = 500f,
            viewportWidth = 800f,
            viewportHeight = 600f
        )
        val proj = camera.project(0f, 0f, 0f)
        assertTrue(proj.isVisible)
        assertTrue(proj.depth > 0f)
    }

    @Test
    fun testMeshPrimitivesCreation() {
        val box = com.example.engine3d.MeshPrimitives.createBox(
            width = 2f,
            length = 2f,
            height = 2f,
            color = androidx.compose.ui.graphics.Color.Blue
        )
        assertEquals(8, box.vertices.size)
        assertEquals(6, box.faces.size)
    }
}

