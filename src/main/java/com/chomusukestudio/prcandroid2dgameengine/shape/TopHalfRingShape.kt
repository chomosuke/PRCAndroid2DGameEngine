package com.chomusukestudio.prcandroid2dgameengine.shape

import kotlin.math.*

class TopHalfRingShape(center: Vector, a: Float, b: Float, factor: Float, color: Color, buildShapeAttr: BuildShapeAttr) : Shape() {
    override lateinit var componentShapes: Array<Shape>

    init {

        val numberOfEdges = CircularShape.getNumberOfEdges(max(a, b)) / 2

        val componentShapes = arrayOfNulls<QuadrilateralShape>(numberOfEdges)

        var previousX2 = center.x + a
        var previousY2 = center.y
        var previousX3 = center.x + factor * a
        var previousY3 = center.y // start at left
        for (i in componentShapes.indices) {
            val theta = PI * (i + 1) / numberOfEdges
            val x2 = center.x + a * cos(theta).toFloat()
            val y2 = center.y + b * sin(theta).toFloat()
            val x3 = center.x + factor * a * cos(theta).toFloat()
            val y3 = center.y + factor * b * sin(theta).toFloat()
            componentShapes[i] = QuadrilateralShape(
                Vector(previousX2, previousY2), Vector(x2, y2), Vector(x3, y3), Vector(previousX3, previousY3),
                    color, buildShapeAttr)
            previousX2 = x2
            previousY2 = y2
            previousX3 = x3
            previousY3 = y3
        }

        this.componentShapes = componentShapes as Array<Shape>
    }
}