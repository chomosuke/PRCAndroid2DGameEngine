package com.chomusukestudio.prcandroid2dgameengine.shape

import com.chomusukestudio.prcandroid2dgameengine.glRenderer.GLEllipse
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

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

    override fun resetAlpha(alpha: Float) {
        shapeColor = Color(shapeColor.red, shapeColor.green, shapeColor.blue, alpha)
    }

    override fun changeShapeColor(dRed: Float, dGreen: Float, dBlue: Float, dAlpha: Float) {
        shapeColor = Color(shapeColor.red + dRed, shapeColor.green + dGreen,
                shapeColor.blue + dBlue, shapeColor.alpha + dAlpha)
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
    override fun overlapToOverride(anotherOverlapper: Overlapper): Boolean? {
        if (a == b) // convert to circle
            return CircularOverlapper(center, a) overlap anotherOverlapper
        when (anotherOverlapper) {
            is PointOverlapper -> {
                return CircularOverlapper(Vector(0f, 0f), 1f) overlap
                        PointOverlapper(normalize(anotherOverlapper.point))
            }
            is TriangularOverlapper -> {
                return CircularOverlapper(Vector(0f, 0f), 1f) overlap
                        TriangularOverlapper(
                                normalize(anotherOverlapper.vertex1),
                                normalize(anotherOverlapper.vertex2),
                                normalize(anotherOverlapper.vertex3))
            }
            is CircularOverlapper -> {
                return this overlap EllipseOverlapper(anotherOverlapper.center,
                        anotherOverlapper.radius, anotherOverlapper.radius, 0f)
            }
            is EllipseOverlapper -> {
                val thisEdgePointOverlappers = getEdgePointOverlappers(CircularShape.getNumberOfEdges(max(a, b)))
                val otherEdgePointOverlappers = anotherOverlapper.getEdgePointOverlappers(CircularShape.getNumberOfEdges(max(a, b)))
                for (thisEdgePointOverlapper in thisEdgePointOverlappers)
                    if (anotherOverlapper.overlap(thisEdgePointOverlapper))
                        return true
                for (otherEdgePointOverlapper in otherEdgePointOverlappers)
                    if (this.overlap(otherEdgePointOverlapper))
                        return true
                return false
            }
            else -> return null
        }
    }
    private fun normalize(coordinate: Vector) = (coordinate - center).rotateVector(-rotation) / Vector(a, b)

    private fun getEdgePointOverlappers(numberOfEdges: Int) = Array(numberOfEdges) {
        val theta = 2f * PI.toFloat() * it / numberOfEdges
        PointOverlapper(
            Vector(center.x + a * sin(theta),
                center.y + b * cos(theta)).rotateVector(center, rotation))
    }

}