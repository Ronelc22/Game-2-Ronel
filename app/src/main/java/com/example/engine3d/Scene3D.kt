package com.example.engine3d

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Entidad dentro de la escena 3D con malla, transformación y soporte para renderizado procedural personalizado.
 */
class Entity3D(
    val id: String,
    var mesh: Mesh3D? = null,
    val transform: Transform3D = Transform3D(),
    var isVisible: Boolean = true,
    var customRenderer: ((DrawScope, EngineCamera3D, Light3D) -> Unit)? = null
)

/**
 * Escena 3D que administra el grafo de entidades, iluminación global y estado ambiental.
 */
class Scene3D(
    var camera: EngineCamera3D? = null,
    val light: Light3D = Light3D(),
    var skyColorTop: Color = Color(0xFFFDE68A),
    var skyColorHorizon: Color = Color(0xFFFED7AA),
    var ambientColor: Color = Color(0xFFE2E8F0)
) {
    private val entities = mutableListOf<Entity3D>()

    fun addEntity(entity: Entity3D) {
        entities.add(entity)
    }

    fun removeEntity(id: String) {
        entities.removeAll { it.id == id }
    }

    fun clearEntities() {
        entities.clear()
    }

    fun getEntities(): List<Entity3D> = entities

    fun findEntity(id: String): Entity3D? = entities.find { it.id == id }
}
