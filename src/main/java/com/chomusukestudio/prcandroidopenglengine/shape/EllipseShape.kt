package com.chomusukestudio.prcandroidopenglengine.shape

import com.chomusukestudio.prcandroidopenglengine.distance
import com.chomusukestudio.prcandroidopenglengine.square
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class EllipseShape(center: Vector, a: Float, b: Float, color: Color, buildShapeAttr: BuildShapeAttr) : Shape() {
    override lateinit var componentShapes: Array<Shape>

    init {
        val numberOfEdges = CircularShape.getNumberOfEdges((a + b) / 2)
        // just initialize it.
        val componentShapes = arrayOfNulls<TriangularShape>(numberOfEdges - 2)

        // generate components triangularShape for EllipseShape isInUse center and a and b
        val initialTheta = 2f * PI.toFloat() / numberOfEdges
        val vertex1 = Vector(center.x, center.y + b)
        var vertex2 = Vector(center.x + a * sin(initialTheta),
                center.y + b * cos(initialTheta))
        for (i in 1 until numberOfEdges - 1) {
            val theta = 2f * PI.toFloat() * (i + 1) / numberOfEdges
            val vertex3 = Vector(center.x + a * sin(theta),
                    center.y + b * cos(theta))
            componentShapes[i - 1] = TriangularShape(vertex1,
                    vertex2,
                    vertex3,
                    color, buildShapeAttr)
            vertex2 = vertex3
        }

        this.componentShapes = componentShapes as Array<Shape>
    }
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