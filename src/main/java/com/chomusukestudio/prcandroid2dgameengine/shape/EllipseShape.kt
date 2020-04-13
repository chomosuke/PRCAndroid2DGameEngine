package com.chomusukestudio.prcandroid2dgameengine.shape

import com.chomusukestudio.prcandroid2dgameengine.distance
import com.chomusukestudio.prcandroid2dgameengine.glRenderer.GLEllipse
import com.chomusukestudio.prcandroid2dgameengine.square
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class EllipseShape(center: Vector, a: Float, b: Float, color: Color, private val buildShapeAttr: BuildShapeAttr) : Shape() {
    override var componentShapes: Array<Shape>
        get() = throw IllegalAccessException("Ellipse is base on texture not componentShapes")
        set(value) {
            throw IllegalAccessException("Ellipse is base on texture not componentShapes")
        }

    private var glEllipse = if (buildShapeAttr.visibility) GLEllipse(center, a, b, color, buildShapeAttr) else null

    override var shapeColor: Color = color
        set(value) {
            if (visibility)
                glEllipse!!.color = value
            field = value
        }

    override fun remove() {
        glEllipse?.remove()
        removed = true
    }

    override var visibility = buildShapeAttr.visibility
        set(value) {
            if (field != value) {
                if (value) {
                    glEllipse = GLEllipse(center, a, b, shapeColor ,buildShapeAttr)
                    glEllipse!!.rotate(center, rotation)
                } else {
                    glEllipse!!.remove()
                    glEllipse = null
                }
                field = value
            }
        }

    var rotation = 0f
        private set
    var center = center
        private set
    var a = a
        private set
    var b = b
        private set

    override fun rotate(centerOfRotation: Vector, angle: Float) {
        center = center.rotateVector(centerOfRotation, angle)
        rotation += angle
        if (visibility) {
            glEllipse!!.rotate(centerOfRotation, angle)
        }
    }

    override fun move(displacement: Vector) {
        center += displacement
        if (visibility) {
            glEllipse!!.move(displacement)
        }
    }

    fun resetParameter(center: Vector, a:Float, b: Float) {
        if (visibility) {
            glEllipse!!.setParameters(center, a, b)
            glEllipse!!.rotate(center, rotation)
        }
        this.center = center
        this.a = a
        this.b = b
    }

    override val overlapper: Overlapper
        get() = EllipseOverlapper(center, a, b, rotation)
}

class EllipseOverlapper(val center: Vector, val a: Float, val b: Float, val rotation: Float): Overlapper() {
    override fun overlap(anotherOverlapper: Overlapper): Boolean {
        if (a == b) // convert to circle
            return CircularOverlapper(center, a) overlap anotherOverlapper
        when (anotherOverlapper) {
            is PointOverlapper -> {
                val point = transformToEllipseCoordinate(anotherOverlapper.point)

                val focus1: Vector
                val focus2: Vector
                val d: Float
                if (a > b) {
                    d = 2 * a
                    val c = sqrt(square(a) - square(b))
                    focus1 = Vector(c, 0f)
                    focus2 = Vector(-c, 0f)
                } else {
                    d = 2 * b
                    val c = sqrt(square(b) - square(a))
                    focus1 = Vector(0f, c)
                    focus2 = Vector(0f, -c)
                }
                return distance(focus1, point) + distance(focus2, point) <= d
            }
            is TriangularOverlapper -> {
                if (this overlap PointOverlapper(anotherOverlapper.vertex1) ||
                        this overlap PointOverlapper(anotherOverlapper.vertex2) ||
                        this overlap PointOverlapper(anotherOverlapper.vertex3)
                )
                    return true // triangle's vertex within ellipse
                val edgePointOverlappers = getEdgePointOverlappers(CircularShape.getNumberOfEdges(a))
                for (edgePointOverlapper in edgePointOverlappers)
                    if (anotherOverlapper overlap edgePointOverlapper)
                        return true // a point on ellipse's edge with in triangle
                return false
            }
            is CircularOverlapper -> {
                val edgePointOverlappers = getEdgePointOverlappers(CircularShape.getNumberOfEdges(a))
                for (edgePointOverlapper in edgePointOverlappers)
                    if (anotherOverlapper overlap edgePointOverlapper)
                        return true
                // circle might be within ellipse
                return this overlap PointOverlapper(anotherOverlapper.center)
            }
            else -> return super.overlap(anotherOverlapper)
        }
    }
    private fun transformToEllipseCoordinate(coordinate: Vector) = (coordinate - center).rotateVector(-rotation)

    private fun getEdgePointOverlappers(numberOfEdges: Int) = Array(numberOfEdges) {
        val theta = 2f * PI.toFloat() * it / numberOfEdges
        PointOverlapper(
            Vector(center.x + a * sin(theta),
                center.y + b * cos(theta)).rotateVector(center, rotation))
    }

}