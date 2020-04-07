package com.chomusukestudio.prcandroidopenglengine.glRenderer

import android.util.Log
import com.chomusukestudio.prcandroidopenglengine.shape.BuildShapeAttr

class GLTriangle (buildShapeAttr: BuildShapeAttr) {

    val shapeLayer: ShapeLayer = getLayer(buildShapeAttr.z, buildShapeAttr.layers) // the layer this triangle belong

    val z: Float
        get() = shapeLayer.z

    private val coordPointer: Int = shapeLayer.getCoordPointer() // point to the first of the six layer.triangleCoords[] this triangle is isInUse
    val triangleCoords: TriangleCoords = object : TriangleCoords() {

        override var floatArray: FloatArray
            get() { return FloatArray(6) { i -> this[i] } }
            set(value) { System.arraycopy(shapeLayer.triangleCoords, coordPointer, value, 0, value.size) }

        override fun get(index: Int): Float {
            if (index < 6)
                return shapeLayer.triangleCoords[coordPointer + index]
            else
                throw IndexOutOfBoundsException("invalid index for getTriangleCoords: $index")
        }

        override fun set(index: Int, value: Float) {
            if (index < 6)
                shapeLayer.triangleCoords[coordPointer + index] = value
            else
                throw IndexOutOfBoundsException("invalid index for setTriangleCoords: $index")
        }
    }

    private val colorPointer = shapeLayer.getFragmentPointer(coordPointer)
    val RGBA: RGBAArray = object : RGBAArray() {

        override var floatArray: FloatArray
            get() { return FloatArray(4) { i -> this[i] } }
            set(value) { System.arraycopy(shapeLayer.triangleCoords, colorPointer, value, 0, value.size) }

        override fun get(index: Int): Float {
            if (index < 12)
                return shapeLayer.fragmentData[colorPointer + index]
            else
                throw IndexOutOfBoundsException("invalid index for getRGBAArray: $index")
        }
        override fun set(index: Int, value: Float) {
            if (index < 4) {
                shapeLayer.fragmentData[colorPointer + index] = value
                shapeLayer.fragmentData[colorPointer + index + 4] = value
                shapeLayer.fragmentData[colorPointer + index + 8] = value
            } else {
                throw IndexOutOfBoundsException("invalid index for setRGBAArray: $index")
            }
        }
    }

    private fun getLayer(z: Float, layers: Layers): ShapeLayer {
            for (layer in layers) {
                if (layer.z == z && layer is ShapeLayer) {
                    return layer // find the layer with that z
                }
            }

            // there is no layer with that z so create one and return index of that layer
            val newLayer = ShapeLayer(z)
            layers.insert(newLayer)
            return newLayer
    }

    constructor(x1: Float, y1: Float,
                x2: Float, y2: Float,
                x3: Float, y3: Float,
                red: Float, green: Float, blue: Float, alpha: Float, buildShapeAttr: BuildShapeAttr
    ) : this(buildShapeAttr) {
        setTriangleCoords(x1, y1, x2, y2, x3, y3)

        setTriangleRGBA(red, green, blue, alpha)
    }// as no special isOverlapToOverride method is provided.

    constructor(coords: FloatArray, red: Float, green: Float, blue: Float, alpha: Float, buildShapeAttr: BuildShapeAttr) : this(buildShapeAttr) {
        System.arraycopy(coords, 0, shapeLayer.triangleCoords, coordPointer, coords.size)
        setTriangleRGBA(red, green, blue, alpha)
    }

    constructor(coords: FloatArray, color: FloatArray, buildShapeAttr: BuildShapeAttr) : this(buildShapeAttr) {
        System.arraycopy(coords, 0, shapeLayer.triangleCoords, coordPointer, coords.size)
        System.arraycopy(color, 0, shapeLayer.fragmentData, colorPointer, color.size)
        System.arraycopy(color, 0, shapeLayer.fragmentData, colorPointer + 4, color.size)
        System.arraycopy(color, 0, shapeLayer.fragmentData, colorPointer + 8, color.size)
    }

    fun moveTriangle(dx: Float, dy: Float) {
        shapeLayer.triangleCoords[0 + coordPointer] += dx
        shapeLayer.triangleCoords[1 + coordPointer] += dy
        shapeLayer.triangleCoords[2 + coordPointer] += dx
        shapeLayer.triangleCoords[3 + coordPointer] += dy
        shapeLayer.triangleCoords[4 + coordPointer] += dx
        shapeLayer.triangleCoords[5 + coordPointer] += dy
    }

    fun setTriangleCoords(x1: Float, y1: Float,
                          x2: Float, y2: Float,
                          x3: Float, y3: Float) {
        shapeLayer.triangleCoords[0 + coordPointer] = x1
        shapeLayer.triangleCoords[1 + coordPointer] = y1
        shapeLayer.triangleCoords[2 + coordPointer] = x2
        shapeLayer.triangleCoords[3 + coordPointer] = y2
        shapeLayer.triangleCoords[4 + coordPointer] = x3
        shapeLayer.triangleCoords[5 + coordPointer] = y3
    }

    fun setTriangleRGBA(red: Float, green: Float, blue: Float, alpha: Float) {
        shapeLayer.fragmentData[0 + colorPointer] = red
        shapeLayer.fragmentData[1 + colorPointer] = green
        shapeLayer.fragmentData[2 + colorPointer] = blue
        shapeLayer.fragmentData[3 + colorPointer] = alpha
        shapeLayer.fragmentData[4 + colorPointer] = red
        shapeLayer.fragmentData[5 + colorPointer] = green
        shapeLayer.fragmentData[6 + colorPointer] = blue
        shapeLayer.fragmentData[7 + colorPointer] = alpha
        shapeLayer.fragmentData[8 + colorPointer] = red
        shapeLayer.fragmentData[9 + colorPointer] = green
        shapeLayer.fragmentData[10 + colorPointer] = blue
        shapeLayer.fragmentData[11 + colorPointer] = alpha
    }

    fun removeTriangle() {
        // mark coords as unused
        if (removed)
            throw RuntimeException("triangle is already removed")
        setTriangleCoords(UNUSED, UNUSED, UNUSED, UNUSED, UNUSED, UNUSED)
        removed = true
    }
    private var removed = false
    protected fun finalize() {
        if (!removed) {
            removeTriangle()
            Log.e("triangle finalizer", "triangle isn't removed")
        }
    }
    
    abstract class TriangleCoords {
        abstract operator fun get(index: Int): Float
        abstract operator fun set(index: Int, value: Float)
        abstract val floatArray : FloatArray
    }
    
    abstract class RGBAArray {
        abstract operator fun get(index: Int): Float
        abstract operator fun set(index: Int, value: Float)
        abstract val floatArray : FloatArray
    }
}
