package com.chomusukestudio.prcandroidopenglengine.shape

import java.lang.Math.PI

/**
 * Created by Shuang Li on 22/03/2018.
 */

class RegularPolygonalShape(val numberOfEdges: Int, center: Vector, radius: Float, color: Color, buildShapeAttr: BuildShapeAttr) : Shape() {
    override lateinit var componentShapes: Array<Shape>

    var center = center
        private set

    // as no special isOverlapToOverride method is provided.
    init {
    
        // just initialize it.
        val componentShapes = arrayOfNulls<TriangularShape>(numberOfEdges - 2)
    
        // generate components triangularShape for RegularPolygonalShape center and radius
        var previousDVector = Vector(0f, radius).rotateVector(2 * PI.toFloat() / numberOfEdges)
        for (i in 1 until numberOfEdges - 1) {
            val dVector = previousDVector.rotateVector(2 * PI.toFloat() / numberOfEdges)
            componentShapes[i - 1] = TriangularShape(center.offset(0f, radius),
                    center + previousDVector, center + dVector,
                    color, buildShapeAttr)
            previousDVector = dVector
        }
        
        this.componentShapes = componentShapes as Array<Shape>
    }

    fun resetParameter(center: Vector, radius: Float) {
        resetCenter(center)

        // and change radius
        this.radius = radius
    }

    fun resetCenter(center: Vector) {
        val vector = center.minus(this.center)
        move(vector)
        this.center = center
    }

    var radius = radius
        set(value) {
            if (value != field) {
                // reset components triangularShape for RegularPolygonalShape isInUse center and value
                if (field != 0f) { // if field is 0 factor will be infinity, therefore not possible to use this method
                    val factor = value / field

                    // previous are just this vertex2
                    var vertex2 = (componentShapes[0] as TriangularShape).vertex2.scale(center, factor)

                    // vertex1 doesn't change
                    val vertex1 = (componentShapes[0] as TriangularShape).vertex1.scale(center, factor)

                    for (i in 0 until componentShapes.size) {
                        // new vertex3 is old one change distance
                        val vertex3 = (componentShapes[i] as TriangularShape).vertex3.scale(center, factor)

                        (componentShapes[i] as TriangularShape).setTriangleCoords(vertex1, vertex2, vertex3)

                        // vertex2 is previous x3 and previous y3
                        vertex2 = vertex3

                        // i spend so long to write this to try to improve performance but only got ever so slightly...
                        // well now it doesn't matter cause the vertex thingy will slow it down again
                    }
                } else {
                    // because the above is not possible when field is zero, use sin and cos to recalculate
                    var previousDVector = Vector(0f, radius).rotateVector(2 * PI.toFloat() / numberOfEdges)
                    for (i in 1 until numberOfEdges - 1) {
                        val dVector = previousDVector.rotateVector(2 * PI.toFloat() / numberOfEdges)
                        (componentShapes[i - 1] as TriangularShape).setTriangleCoords(center.offset(0f, radius),
                                center + previousDVector, center + dVector)
                        previousDVector = dVector
                    }
                }
            }
            field = value
        }

    override fun move(displacement: Vector) {
        super.move(displacement)
        center += (displacement)
    }

    override fun rotate(centerOfRotation: Vector, angle: Float) {
        super.rotate(centerOfRotation, angle)
        center = center.rotateVector(centerOfRotation, angle)
    }
}