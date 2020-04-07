package com.chomusukestudio.prcandroidopenglengine.glRenderer

import android.content.Context
import com.chomusukestudio.prcandroidopenglengine.glRenderer.Layers
import com.chomusukestudio.prcandroidopenglengine.glRenderer.TextureLayer
import com.chomusukestudio.prcandroidopenglengine.shape.IMovable
import com.chomusukestudio.prcandroidopenglengine.shape.IRemovable
import com.chomusukestudio.prcandroidopenglengine.shape.Vector

class GLImage(context: Context, resourceId: Int, vertex1: Vector, vertex2: Vector, vertex3: Vector, vertex4: Vector, z: Float, private val layers: Layers): IMovable,
    IRemovable {
    private val textureLayer = TextureLayer(context, resourceId, vertex1, vertex2, vertex3, vertex4, z)

    init {
        layers.insert(textureLayer)
    }
    
    var vertex1
        set(value) {
            textureLayer.triangleCoords[0] = value.x
            textureLayer.triangleCoords[1] = value.y
            textureLayer.triangleCoords[6] = value.x
            textureLayer.triangleCoords[7] = value.y
        }
        get() = Vector(textureLayer.triangleCoords[0], textureLayer.triangleCoords[1])
    var vertex2
        set(value) {
            textureLayer.triangleCoords[2] = value.x
            textureLayer.triangleCoords[3] = value.y
        }
        get() = Vector(textureLayer.triangleCoords[2], textureLayer.triangleCoords[3])
    var vertex3
        set(value) {
            textureLayer.triangleCoords[4] = value.x
            textureLayer.triangleCoords[5] = value.y
            textureLayer.triangleCoords[10] = value.x
            textureLayer.triangleCoords[11] = value.y
        }
        get() = Vector(textureLayer.triangleCoords[4], textureLayer.triangleCoords[5])
    var vertex4
        set(value) {
            textureLayer.triangleCoords[8] = value.x
            textureLayer.triangleCoords[9] = value.y
        }
        get() = Vector(textureLayer.triangleCoords[8], textureLayer.triangleCoords[9])

    fun setColorSwap(colorBeSwapped: Array<Float>, colorSwappedTo: Array<Float>) = textureLayer.setColorSwap(colorBeSwapped, colorSwappedTo)

    val colorOffset = textureLayer.colorOffset

    override fun move(displacement: Vector) {
        vertex1 += displacement
        vertex2 += displacement
        vertex3 += displacement
        vertex4 += displacement
    }

    override fun rotate(centerOfRotation: Vector, angle: Float) {
        vertex1 = vertex1.rotateVector(centerOfRotation, angle)
        vertex2 = vertex2.rotateVector(centerOfRotation, angle)
        vertex3 = vertex3.rotateVector(centerOfRotation, angle)
        vertex4 = vertex4.rotateVector(centerOfRotation, angle)
    }

    override var removed = false
        private set

    override fun remove() {
        removed = true
        layers.remove(textureLayer)
    }
}