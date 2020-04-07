package com.chomusukestudio.prcandroidopenglengine.shape

import com.chomusukestudio.prcandroidopenglengine.distance
import kotlin.math.PI
import kotlin.math.pow

class CircleSegmentShape() : Shape() {
    override lateinit var componentShapes: Array<Shape>
    private fun generateComponents(angle: Float, center: Vector, startPoint: Vector, color: Color, buildShapeAttr: BuildShapeAttr): Array<Shape> {

        // set var for overlapper
        this.angle = angle; this.center = center; this.startPoint = startPoint

        var numberOfEdge = (CircularShape.getNumberOfEdges(distance(center, startPoint)) * angle / PI).toInt()
        if (numberOfEdge < 2)
            numberOfEdge = 2
        val dAngle = angle / numberOfEdge
        var v2 = startPoint.rotateVector(center, dAngle)
        val componentShapes = arrayOfNulls<Shape>(numberOfEdge - 1)
        for (i in 0 until numberOfEdge - 1) {
            val v3 = v2.rotateVector(center, dAngle)
            componentShapes[i] = TriangularShape(startPoint, v2, v3, color, buildShapeAttr)
            v2 = v3
        }
        return componentShapes as Array<Shape>
    }
    private fun findCenter(p1: Vector, p2: Vector, p3: Vector): Vector {
        val x1 = p1.x; val y1 = p1.y
        val x2 = p2.x; val y2 = p2.y
        val x3 = p3.x; val y3 = p3.y

        val x12 = x1 - x2
        val x13 = x1 - x3

        val y12 = y1 - y2
        val y13 = y1 - y3

        val y31 = y3 - y1
        val y21 = y2 - y1

        val x31 = x3 - x1
        val x21 = x2 - x1

        // x1^2 - x3^2
        val sx13 = x1.pow(2) - x3.pow(2)

        // y1^2 - y3^2
        val sy13 = y1.pow(2) - y3.pow(2)

        val sx21 = x2.pow(2) - x1.pow(2)

        val sy21 = y2.pow(2) - y1.pow(2)

        val f = (sx13 * x12
                + sy13 * x12
                + sx21 * x13
                + sy21 * x13) / (2 * (y31 * x12 - y21 * x13))
        val g = (sx13 * y12
                + sy13 * y12
                + sx21 * y13
                + sy21 * y13) / (2 * (x31 * y12 - x21 * y13))

        return Vector(-g, -f)

        // eqn of circle be x^2 + y^2 + 2*g*x + 2*f*y + c = 0
        // where centre is (h = -g, k = -f) and radius r
        // as r^2 = h^2 + k^2 - c
    }

    var angle = 0f; lateinit var center: Vector; lateinit var startPoint: Vector
    override val overlapper: Overlapper
        get() = CircleSegmentOverlapper(angle, center, startPoint)

    override fun move(displacement: Vector) {
        super.move(displacement)
        center += displacement
        startPoint += displacement
    }
    override fun rotate(centerOfRotation: Vector, angle: Float) {
        super.rotate(centerOfRotation, angle)
        center = center.rotateVector(centerOfRotation, angle)
        startPoint = startPoint.rotateVector(centerOfRotation, angle)
    }

    constructor(angle: Float, center: Vector, startPoint: Vector, color: Color, buildShapeAttr: BuildShapeAttr) : this() {
        componentShapes = generateComponents(angle, center, startPoint, color, buildShapeAttr)
    }

    constructor(startPoint: Vector, endPoint: Vector, middlePoint: Vector, color: Color, buildShapeAttr: BuildShapeAttr) : this() {
        val center = findCenter(startPoint, middlePoint, endPoint)
        val angle = (endPoint - center).direction - (startPoint - center).direction
        componentShapes = generateComponents(angle, center, startPoint, color, buildShapeAttr)
    }
}

class CircleSegmentOverlapper(val angle: Float, val center: Vector, val startPoint: Vector) : Overlapper() {
    override val components: Array<Overlapper> = run {
        val startingVertex = startPoint.scale(center, 2f)
        arrayOf(
            TriangularOverlapper(startingVertex, startingVertex.rotateVector(center, angle / 3), startingVertex.rotateVector(center, 2 * angle / 3)),
            TriangularOverlapper(startingVertex, startingVertex.rotateVector(center, 2 * angle / 3), startingVertex.rotateVector(center, angle)),
            CircularOverlapper(center, distance(center, startPoint))
        )
    }

    override fun overlap(anotherOverlapper: Overlapper): Boolean {
        return (components[0].overlap(anotherOverlapper) || components[1].overlap(anotherOverlapper)) &&
                components[2].overlap(anotherOverlapper)
    }
}