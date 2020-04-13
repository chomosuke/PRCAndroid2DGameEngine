package com.chomusukestudio.prcandroid2dgameengine.glRenderer

import android.util.Log
import com.chomusukestudio.prcandroid2dgameengine.shape.BuildShapeAttr

class GLTriangle (buildShapeAttr: BuildShapeAttr) {

    val shapeLayer: ShapeLayer = buildShapeAttr.drawData.getLayer(buildShapeAttr.z) { ShapeLayer(buildShapeAttr.z) } // the layer this triangle belong

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

    private val colorPointer = shapeLayer.getFragmentPointer(coordPointer, 0)
    val RGBA: RGBAArray = object : RGBAArray() {

        override var floatArray: FloatArray
            get() { return FloatArray(4) { i -> this[i] } }
            set(value) { System.arraycopy(shapeLayer.triangleCoords, colorPointer, value, 0, value.size) }

        override fun get(index: Int): Float {
            if (index < 12)
                return shapeLayer.fragmentDatas[0][colorPointer + index]
            else
                throw IndexOutOfBoundsException("invalid index for getRGBAArray: $index")
        }
        override fun set(index: Int, value: Float) {
            if (index < 4) {
                shapeLayer.fragmentDatas[0][colorPointer + index] = value
                shapeLayer.fragmentDatas[0][colorPointer + index + 4] = value
                shapeLayer.fragmentDatas[0][colorPointer + index + 8] = value
            } else {
                throw IndexOutOfBoundsException("invalid index for setRGBAArray: $index")
            }
        }
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
        System.arraycopy(color, 0, shapeLayer.fragmentDatas[0], colorPointer, color.size)
        System.arraycopy(color, 0, shapeLayer.fragmentDatas[0], colorPointer + 4, color.size)
        System.arraycopy(color, 0, shapeLayer.fragmentDatas[0], colorPointer + 8, color.size)
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
        shapeLayer.fragmentDatas[0][0 + colorPointer] = red
        shapeLayer.fragmentDatas[0][1 + colorPointer] = green
        shapeLayer.fragmentDatas[0][2 + colorPointer] = blue
        shapeLayer.fragmentDatas[0][3 + colorPointer] = alpha
        shapeLayer.fragmentDatas[0][4 + colorPointer] = red
        shapeLayer.fragmentDatas[0][5 + colorPointer] = green
        shapeLayer.fragmentDatas[0][6 + colorPointer] = blue
        shapeLayer.fragmentDatas[0][7 + colorPointer] = alpha
        shapeLayer.fragmentDatas[0][8 + colorPointer] = red
        shapeLayer.fragmentDatas[0][9 + colorPointer] = green
        shapeLayer.fragmentDatas[0][10 + colorPointer] = blue
        shapeLayer.fragmentDatas[0][11 + colorPointer] = alpha
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
