package com.chomusukestudio.prcandroidopenglengine.shape

import java.lang.Math.PI
import kotlin.math.cos
import kotlin.math.sin

class NPointsStarShape(n: Int, center: Vector, radius: Float, color: Color, buildShapeAttr: BuildShapeAttr) : Shape() {
    override lateinit var componentShapes: Array<Shape>
    
    init {
        val componentShapes = arrayOfNulls<Shape>(n + 1)
        componentShapes[0] = RegularPolygonalShape(n, center,
                radius * (sin(PI / 2f / n) / sin(PI - PI / 2f / n - PI / n)).toFloat(),
               color, buildShapeAttr) // central RegularPolygonalShape
        componentShapes[0]!!.rotate(center, PI.toFloat() / n)

        // points
        val dx = (sin(PI/2f/n) / sin(PI - PI/2f/n - PI/n) * cos(PI/n) * radius).toFloat()
        val dy = (sin(PI/2f/n) / sin(PI - PI/2f/n - PI/n) * sin(PI/n) * radius).toFloat()
        for (i in 1 until componentShapes.size) {
            componentShapes[i] = TriangularShape(
                    Vector(center.x, center.y + radius), Vector(center.x + dx, center.y + dy), Vector(center.x - dx, center.y + dy),
                    color, buildShapeAttr)
            componentShapes[i]!!.rotate(center, 2f*PI.toFloat() / n * i)
        }
        
        this.componentShapes = componentShapes as Array<Shape>
    }
}