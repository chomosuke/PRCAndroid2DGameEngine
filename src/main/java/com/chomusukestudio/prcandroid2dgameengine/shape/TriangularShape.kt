package com.chomusukestudio.prcandroid2dgameengine.shape

import com.chomusukestudio.prcandroid2dgameengine.glRenderer.COORDS_PER_VERTEX
import com.chomusukestudio.prcandroid2dgameengine.glRenderer.CPT
import com.chomusukestudio.prcandroid2dgameengine.glRenderer.GLTriangle
import com.chomusukestudio.prcandroid2dgameengine.glRenderer.UNUSED
import kotlin.math.sign

class TriangularShape(vertex1: Vector, vertex2: Vector, vertex3: Vector,
                      color: Color, private val buildShapeAttr: BuildShapeAttr
) : Shape() {
    
    private var triangle: GLTriangle? = if (buildShapeAttr.visibility) GLTriangle(vertex1.x, vertex1.y, vertex2.x, vertex2.y, vertex3.x, vertex3.y,
            color.red, color.green, color.blue, color.alpha, buildShapeAttr) else null
    // nullable because sometimes invisible
    
    private var triangleCoords = /*if (visibility) FloatArray(6) else */floatArrayOf(vertex1.x, vertex1.y, vertex2.x, vertex2.y, vertex3.x, vertex3.y)
    private var RGBA = /*if (visibility) FloatArray(4) else */floatArrayOf(color.red, color.green, color.blue, color.alpha)

    override var shapeColor: Color
        get() =
            if (visibility)
                Color(triangle!!.RGBA[0], triangle!!.RGBA[1], triangle!!.RGBA[2], triangle!!.RGBA[3])
            else
                Color(RGBA[0], RGBA[1], RGBA[2], RGBA[3])
        set(value) {
            if (visibility) {
                triangle!!.setTriangleRGBA(value.red, value.green, value.blue, value.alpha)
            } else {
                RGBA[0] = value.red
                RGBA[1] = value.green
                RGBA[2] = value.blue
                RGBA[3] = value.alpha
            }
        }

    override fun resetAlpha(alpha: Float) {
        if (visibility)
            triangle!!.RGBA[3] = alpha
        else
            RGBA[3] = alpha
    }

    override fun changeShapeColor(dRed: Float, dGreen: Float, dBlue: Float, dAlpha: Float) {
        if (visibility) {
            triangle!!.RGBA[0] += dRed
            triangle!!.RGBA[1] += dGreen
            triangle!!.RGBA[2] += dBlue
            triangle!!.RGBA[3] += dAlpha
        } else {
            RGBA[0] += dRed
            RGBA[1] += dGreen
            RGBA[2] += dBlue
            RGBA[3] += dAlpha
        }
    }

    override var componentShapes: Array<Shape>
        get() = throw IllegalAccessException("TriangularShape itself is the componentShape of itself")
        set(value) {
            throw IllegalAccessException("TriangularShape itself is the componentShape of itself")
        }
    
    override val overlapper: Overlapper
        get() = TriangularOverlapper(vertex1, vertex2, vertex3)

    fun getTriangularShapeCoords() = if (visibility) {
        triangle!!.triangleCoords.floatArray
    } else {
        triangleCoords
    }

    override var visibility: Boolean = buildShapeAttr.visibility // backing field because null doesn't necessarily mean invisible
        set(value) {
            if (field != value) {
                if (value) {
                    triangle = GLTriangle(triangleCoords, RGBA, buildShapeAttr.newAttrWithNewVisibility(value))
                } else {
                    triangleCoords = triangle!!.triangleCoords.floatArray
                    RGBA = triangle!!.RGBA.floatArray
                    triangle!!.removeTriangle()
                    triangle = null
                }
                field = value
            }
        }

    val z: Float = buildShapeAttr.z
        get() = if (visibility) triangle!!.z else field
    
    val vertex1 get() = getTriangularShapeCoords(0)
    val vertex2 get() = getTriangularShapeCoords(1)
    val vertex3 get() = getTriangularShapeCoords(2)
    private fun getTriangularShapeCoords(coord: Int): Vector {
        return if (visibility)
            Vector(triangle!!.triangleCoords[coord * 2], triangle!!.triangleCoords[coord * 2 + 1])
        else {
            Vector(triangleCoords[coord * 2], triangleCoords[coord * 2 + 1])
        }
    }

    fun setTriangleCoords(vertex1: Vector, vertex2: Vector, vertex3: Vector) {
        if (visibility) {
            triangle!!.setTriangleCoords(vertex1.x, vertex1.y, vertex2.x, vertex2.y, vertex3.x, vertex3.y)
        }
        else {
            triangleCoords[0] = vertex1.x
            triangleCoords[1] = vertex1.y
            triangleCoords[2] = vertex2.x
            triangleCoords[3] = vertex2.y
            triangleCoords[4] = vertex3.x
            triangleCoords[5] = vertex3.y
        }
    }

    override fun move(displacement: Vector) {
        val dx = displacement.x
        val dy = displacement.y
        if (visibility) {
            triangle!!.moveTriangle(dx, dy)
        }
        else {
            triangleCoords[0] += dx
            triangleCoords[1] += dy
            triangleCoords[2] += dx
            triangleCoords[3] += dy
            triangleCoords[4] += dx
            triangleCoords[5] += dy
        }
    }

    override fun rotate(centerOfRotation: Vector, angle: Float) {
        if (visibility) {
            var i = 0
            while (i < CPT) {
                // rotate score
                val result = Vector(triangle!!.triangleCoords[i], triangle!!.triangleCoords[i + 1]).rotateVector(centerOfRotation, angle)
                triangle!!.triangleCoords[i] = result.x
                triangle!!.triangleCoords[i + 1] = result.y
                i += COORDS_PER_VERTEX
            }
        }
        else {
            var i = 0
            while (i < CPT) {
                // rotate point
                val result = Vector(triangleCoords[i], triangleCoords[i + 1]).rotateVector(centerOfRotation, angle)
                triangleCoords[i] = result.x
                triangleCoords[i + 1] = result.y
                i += COORDS_PER_VERTEX
            }
        }
    }

    override fun remove() {
        if (visibility) {
            triangle!!.removeTriangle()
            triangle = null
        } else {
            triangleCoords[0] = UNUSED
            triangleCoords[1] = UNUSED
            triangleCoords[2] = UNUSED
            triangleCoords[3] = UNUSED
            triangleCoords[4] = UNUSED
            triangleCoords[5] = UNUSED
        }
        removed = true
    }
}

class TriangularOverlapper(val vertex1: Vector, val vertex2: Vector, val vertex3: Vector): Overlapper() {
    override fun overlap(anotherOverlapper: Overlapper): Boolean {
        when (anotherOverlapper) {
            is PointOverlapper -> {
                val point = anotherOverlapper.point
                val areaA1 = getArea(point.x, point.y,
                        vertex2.x, vertex2.y,
                        vertex3.x, vertex3.y)
                val areaA2 = getArea(vertex1.x, vertex1.y,
                        point.x, point.y,
                        vertex3.x, vertex3.y)
                val areaA3 = getArea(vertex1.x, vertex1.y,
                        vertex2.x, vertex2.y,
                        point.x, point.y)
                return sign(areaA1) == sign(areaA2) && sign(areaA1) == sign(areaA3)
            }
            is TriangularOverlapper -> {
                // check if line overlap but no vertex is inside each other
                if (LineSegmentOverlapper(vertex1, vertex2) overlap LineSegmentOverlapper(anotherOverlapper.vertex1, anotherOverlapper.vertex2) ||
                        LineSegmentOverlapper(vertex1, vertex3) overlap LineSegmentOverlapper(anotherOverlapper.vertex1, anotherOverlapper.vertex2) ||
                        LineSegmentOverlapper(vertex1, vertex2) overlap LineSegmentOverlapper(anotherOverlapper.vertex1, anotherOverlapper.vertex3) ||
                        LineSegmentOverlapper(vertex1, vertex3) overlap LineSegmentOverlapper(anotherOverlapper.vertex1, anotherOverlapper.vertex3)
                ) return true
                // only possibility left is vertex inside each other
                    return this overlap PointOverlapper(anotherOverlapper.vertex1) ||
                            this overlap PointOverlapper(anotherOverlapper.vertex2) ||
                            this overlap PointOverlapper(anotherOverlapper.vertex3) ||
                            anotherOverlapper overlap PointOverlapper(vertex1) ||
                            anotherOverlapper overlap PointOverlapper(vertex2) ||
                            anotherOverlapper overlap PointOverlapper(vertex3)
            }
            is LineSegmentOverlapper -> {
                return anotherOverlapper overlap LineSegmentOverlapper(vertex1, vertex2) ||
                        anotherOverlapper overlap LineSegmentOverlapper(vertex1, vertex3) ||
                        anotherOverlapper overlap LineSegmentOverlapper(vertex2, vertex3) ||
                        // segment crosses triangle's segment
                        this overlap PointOverlapper(anotherOverlapper.p1) // segment within triangle
            }
            else -> return super.overlap(anotherOverlapper)
        }
    }
    private fun getArea(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float): Float {
        return (x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)) / 2
    }
}