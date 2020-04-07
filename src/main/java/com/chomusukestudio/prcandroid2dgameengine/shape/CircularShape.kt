package com.chomusukestudio.prcandroid2dgameengine.shape

import com.chomusukestudio.prcandroid2dgameengine.distance
import com.chomusukestudio.prcandroid2dgameengine.square
import java.lang.Math.PI
import java.lang.Math.abs
import java.lang.Math.acos

/**
 * Created by Shuang Li on 12/03/2018.
 */

class CircularShape(center: Vector, radius: Float, private val performanceIndex: Double, color: Color, private val buildShapeAttr: BuildShapeAttr) : Shape() {
    override var componentShapes: Array<Shape> = arrayOf(
        RegularPolygonalShape(
            getNumberOfEdges(radius, performanceIndex),
            center, radius, color, buildShapeAttr)
    )
    private var regularPolygonalShape
        set(value) { componentShapes[0] = value }
        get() = componentShapes[0] as RegularPolygonalShape

    override val overlapper: Overlapper
        get() = CircularOverlapper(center, radius)

    // parameters needed for isOverlapToOverride method.
    val center
        get() = regularPolygonalShape.center

    var radius
        set(value) {
            regularPolygonalShape.radius = value
            if (abs(radius) > abs(lastChangeOfNumberOfEdgesRadius * 1.25) || abs(radius) < abs(lastChangeOfNumberOfEdgesRadius * 0.8)
                    && getNumberOfEdges(radius, performanceIndex) != regularPolygonalShape.numberOfEdges) {
                lastChangeOfNumberOfEdgesRadius = radius
                val color = shapeColor
                regularPolygonalShape.remove()
                regularPolygonalShape = RegularPolygonalShape(
                    getNumberOfEdges(radius, performanceIndex),
                        center, radius, color, buildShapeAttr.newAttrWithNewVisibility(visibility)/*visibility might have changed*/)
            }
        }
        get() = regularPolygonalShape.radius


    constructor(center: Vector, radius: Float, color: Color, buildShapeAttr: BuildShapeAttr) : this(center, radius, 1.0, color, buildShapeAttr)

    private var lastChangeOfNumberOfEdgesRadius = radius
    fun resetParameter(center: Vector, radius: Float) {
        this.radius = radius
        regularPolygonalShape.resetCenter(center)
    }

    companion object {
        /**
         * preferably set this in mainActivity or something instead of using the default value
         */
        var pixelPerLength = 100

        fun getNumberOfEdges(radius: Float, dynamicPerformanceIndex: Double = 1.0): Int {
            val pixelOnRadius = pixelPerLength * radius // +0.5 for rounding
            val numberOfEdges = (PI / acos(1.0 - 0.2 / pixelOnRadius / dynamicPerformanceIndex) / 2.0 + 0.5).toInt() * 2 /*
         /2*2 to make it even +0.5 for rounding */
            return if (numberOfEdges > 64)
                64
            else if (numberOfEdges < 8 && pixelOnRadius > 4)
                8
            else if (numberOfEdges < 3)
                3
            else
                numberOfEdges
        }
    }
}

class CircularOverlapper(val center: Vector, val radius: Float): Overlapper() {
    override fun overlap(anotherOverlapper: Overlapper): Boolean {
        when (anotherOverlapper) {
            is CircularOverlapper -> return distance(center, anotherOverlapper.center) <= radius + anotherOverlapper.radius
            is TriangularOverlapper -> {
                val vertex1 = anotherOverlapper.vertex1
                val vertex2 = anotherOverlapper.vertex2
                val vertex3 = anotherOverlapper.vertex3
                //
                // TEST 1: Vertex within circle
                //
                if (distance(center, vertex1) <= radius ||
                        distance(center, vertex2) <= radius ||
                        distance(center, vertex3) <= radius)
                    return true
                //
                // TEST 2: Circle centre within triangle
                //
                if (anotherOverlapper overlap PointOverlapper(center))
                    return true
                //
                // TEST 3: Circle intersects edge
                //
                if (lineCutCircle(vertex1, vertex2) ||
                        lineCutCircle(vertex3, vertex2) ||
                        lineCutCircle(vertex1, vertex3))
                    return true
                // We're done, no intersection
                return false
            }
            is LineSegmentOverlapper -> {
                val p1 = anotherOverlapper.p1
                val p2 = anotherOverlapper.p2
                return if (lineCutCircle(p1, p2))
                    true
                else
                    distance(center, p1) <= radius || distance(center, p2) <= radius
            }
            is PointOverlapper -> {
                return distance(center, anotherOverlapper.point) <= radius
            }
            else -> return super.overlap(anotherOverlapper)
        }
    }
    private fun getArea(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float): Float {
        return abs((x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)) / 2)
    }
    private fun lineCutCircle(p1: Vector, p2: Vector): Boolean {
        val x1 = p1.x
        val y1 = p1.y
        val x2 = p2.x
        val y2 = p2.y
        // debugged, closed for modification
        return ((square(getArea(center.x, center.y, x1, y1, x2, y2) * 2) / (square(x1 - x2) // square of distance from center to the edge of triangle.
                + square(y1 - y2)) // calculated by divide the area by length of the edge of triangle.
                <=
                square(radius)) // is overlap if it's smaller than square of radius.
                &&
                ((square(x1 - x2) // the above will consider edge as a straight line without ends
                        + square(y1 - y2)) // that would be problematic as the edge of the triangle have ends.
                        >=
                        abs((square(center.x - x2) // this would determent if the angle between
                                + square(center.y - y2)) // the line from the center of circle to one of the end of the edge
                                - square(center.x - x1)// and the edge would be larger than 90 degree
                                - square(center.y - y1)
                        ))) // as pythagoras thingy.

        // another level of maintainability lol
        // this is pretty much the most unmaintainable code i ever wrote in this project
    }
}