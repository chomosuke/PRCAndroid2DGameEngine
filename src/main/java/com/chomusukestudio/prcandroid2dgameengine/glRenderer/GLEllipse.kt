package com.chomusukestudio.prcandroid2dgameengine.glRenderer

import android.util.Log
import com.chomusukestudio.prcandroid2dgameengine.shape.*

class GLEllipse(center: Vector, a: Float, b: Float, color: Color, buildShapeAttr: BuildShapeAttr): IMovable, IRemovable {
    private val layer = buildShapeAttr.drawData.getLayer(buildShapeAttr.z) { EllipseLayer(buildShapeAttr.z) }

    private val coordsPointer = layer.getCoordPointer()

    private var vertex1
        inline set(value) {
            layer.triangleCoords[0 + coordsPointer] = value.x
            layer.triangleCoords[1 + coordsPointer] = value.y
            layer.triangleCoords[6 + coordsPointer] = value.x
            layer.triangleCoords[7 + coordsPointer] = value.y
        }
        inline get() = Vector(layer.triangleCoords[0 + coordsPointer], layer.triangleCoords[1 + coordsPointer])
    private var vertex2
        inline set(value) {
            layer.triangleCoords[2 + coordsPointer] = value.x
            layer.triangleCoords[3 + coordsPointer] = value.y
        }
        inline get() = Vector(layer.triangleCoords[2 + coordsPointer], layer.triangleCoords[3 + coordsPointer])
    private var vertex3
        inline set(value) {
            layer.triangleCoords[4 + coordsPointer] = value.x
            layer.triangleCoords[5 + coordsPointer] = value.y
            layer.triangleCoords[10 + coordsPointer] = value.x
            layer.triangleCoords[11 + coordsPointer] = value.y
        }
        inline get() = Vector(layer.triangleCoords[4 + coordsPointer], layer.triangleCoords[5 + coordsPointer])
    private var vertex4
        inline set(value) {
            layer.triangleCoords[8 + coordsPointer] = value.x
            layer.triangleCoords[9 + coordsPointer] = value.y
        }
        inline get() = Vector(layer.triangleCoords[8 + coordsPointer], layer.triangleCoords[9 + coordsPointer])

    private val colorPointer = layer.getFragmentPointer(coordsPointer, 0)

    var color: Color
        set(value) {
            var i = 0
            while (i < 4 * 6) { // 6 vertexes total
                layer.fragmentDatas[0][i++ + colorPointer] = value.red
                layer.fragmentDatas[0][i++ + colorPointer] = value.green
                layer.fragmentDatas[0][i++ + colorPointer] = value.blue
                layer.fragmentDatas[0][i++ + colorPointer] = value.alpha
            }
        }
        get() = Color(
            layer.fragmentDatas[0][0 + colorPointer],
            layer.fragmentDatas[0][1 + colorPointer],
            layer.fragmentDatas[0][2 + colorPointer],
            layer.fragmentDatas[0][3 + colorPointer]
        )

    // for antialiasing, this depends on a and b
    private val abPtr = layer.getFragmentPointer(coordsPointer, 2)
    private fun setAB(a: Float, b: Float) {
        var i = 0
        while (i < 2 * 6) { // 6 vertexes
            layer.fragmentDatas[2][abPtr + i++] = a
            layer.fragmentDatas[2][abPtr + i++] = b
        }
    }

    fun setParameters(center: Vector, a: Float, b: Float) {
        vertex1 = Vector(center.x - a, center.y + b)
        vertex2 = Vector(center.x + a, center.y + b)
        vertex3 = Vector(center.x + a, center.y - b)
        vertex4 = Vector(center.x - a, center.y - b)

        setAB(a, b)
    }

    init {
        setParameters(center, a, b)
        this.color = color

        val tCoordsPtr = layer.getFragmentPointer(coordsPointer, 1)
        layer.fragmentDatas[1][tCoordsPtr + 0] = -1f
        layer.fragmentDatas[1][tCoordsPtr + 1] = -1f
        layer.fragmentDatas[1][tCoordsPtr + 2] = 1f
        layer.fragmentDatas[1][tCoordsPtr + 3] = -1f
        layer.fragmentDatas[1][tCoordsPtr + 4] = 1f
        layer.fragmentDatas[1][tCoordsPtr + 5] = 1f
        layer.fragmentDatas[1][tCoordsPtr + 6] = -1f
        layer.fragmentDatas[1][tCoordsPtr + 7] = -1f
        layer.fragmentDatas[1][tCoordsPtr + 8] = -1f
        layer.fragmentDatas[1][tCoordsPtr + 9] = 1f
        layer.fragmentDatas[1][tCoordsPtr + 10] = 1f
        layer.fragmentDatas[1][tCoordsPtr + 11] = 1f
    }

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


    override var removed: Boolean = false
        private set
    override fun remove() {
        if (removed)
            throw RuntimeException("ellipse is already removed")
        val unusedVertex = Vector(UNUSED, UNUSED)
        vertex1 = unusedVertex
        vertex2 = unusedVertex
        vertex3 = unusedVertex
        vertex4 = unusedVertex
    }

}